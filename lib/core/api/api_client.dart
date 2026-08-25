import 'dart:convert';

import 'package:dio/dio.dart';

import '../config.dart';
import '../storage/token_storage.dart';

class ApiException implements Exception {
  ApiException(this.message, {this.statusCode});

  final String message;
  final int? statusCode;

  bool get isUnauthorized => statusCode == 401;

  @override
  String toString() => message;
}

class ApiClient {
  ApiClient(this._tokens) {
    _dio = Dio(
      BaseOptions(
        baseUrl: AppConfig.apiUrl,
        connectTimeout: const Duration(seconds: 20),
        receiveTimeout: const Duration(seconds: 30),
        headers: {
          'x-platform': 'MOBILE',
          'x-device-type': 'phone',
          'x-os': 'Android',
          'x-app-version': AppConfig.appVersion,
        },
      ),
    );
    _dio.interceptors.add(
      InterceptorsWrapper(onRequest: _onRequest, onError: _onError),
    );
  }

  final TokenStorage _tokens;
  late final Dio _dio;
  String? _accessToken;

  /// Called when the session can no longer be restored (refresh failed).
  void Function()? onSessionExpired;

  Dio get raw => _dio;

  /// Raw bearer token for third-party clients (e.g. Pusher auth endpoint).
  String? get accessToken => _accessToken;

  /// Current authenticated user id, decoded from the access token's
  /// `sub` claim (no signature check; identity is server-verified).
  String? get userId {
    final token = _accessToken;
    if (token == null) return null;
    try {
      final parts = token.split('.');
      if (parts.length != 3) return null;
      final payload = utf8.decode(
        base64Url.decode(base64Url.normalize(parts[1])),
      );
      final map = jsonDecode(payload);
      final sub = map['sub'];
      return sub?.toString();
    } catch (_) {
      return null;
    }
  }

  Future<void> loadStoredToken() async {
    _accessToken = await _tokens.readAccessToken();
  }

  Future<void> persistTokens({
    required String accessToken,
    required String refreshToken,
  }) async {
    await _tokens.saveTokens(
      accessToken: accessToken,
      refreshToken: refreshToken,
    );
    _accessToken = accessToken;
  }

  Future<String?> readStoredRefreshToken() =>
      _tokens.readRefreshToken();

  Future<void> clearTokens() async {
    await _tokens.clear();
    _accessToken = null;
  }

  Future<Response<T>> get<T>(
    String path, {
    Map<String, dynamic>? query,
    bool authenticated = true,
  }) =>
      _send<T>(
        () => _dio.get<T>(
          path,
          queryParameters: query,
          options: Options(extra: {'auth': authenticated}),
        ),
      );

  Future<Response<T>> post<T>(
    String path, {
    Object? body,
    bool authenticated = true,
  }) =>
      _send<T>(
        () => _dio.post<T>(
          path,
          data: body,
          options: Options(extra: {'auth': authenticated}),
        ),
      );

  Future<Response<T>> patch<T>(
    String path, {
    Object? body,
    bool authenticated = true,
  }) =>
      _send<T>(
        () => _dio.patch<T>(
          path,
          data: body,
          options: Options(extra: {'auth': authenticated}),
        ),
      );

  /// GET that unwraps the backend envelope `{ success, timestamp, data }`.
  Future<Map<String, dynamic>> getJson(
    String path, {
    Map<String, dynamic>? query,
    bool authenticated = true,
  }) async {
    final res = await get<Map<String, dynamic>>(
      path,
      query: query,
      authenticated: authenticated,
    );
    return _unwrap(res.data);
  }

  /// POST that unwraps the backend envelope `{ success, timestamp, data }`.
  Future<Map<String, dynamic>> postJson(
    String path, {
    Object? body,
    bool authenticated = true,
  }) async {
    final res = await post<Map<String, dynamic>>(
      path,
      body: body,
      authenticated: authenticated,
    );
    return _unwrap(res.data);
  }

  Future<dynamic> postMultipart(
    String path, {
    Map<String, dynamic>? fields,
    Map<String, String>? query,
    required MultipartFile file,
    bool authenticated = true,
  }) async {
    final form = FormData.fromMap(<String, dynamic>{...?fields, 'file': file});
    final res = await _send<dynamic>(
      () => _dio.post<dynamic>(
        path,
        data: form,
        queryParameters: query,
        options: Options(extra: {'auth': authenticated}),
      ),
    );
    return res.data;
  }

  Map<String, dynamic> _unwrap(Map<String, dynamic>? body) {
    final inner = body?['data'];
    if (inner is Map<String, dynamic>) return inner;
    return body ?? <String, dynamic>{};
  }

  /// PATCH that unwraps the backend envelope `{ success, timestamp, data }`.
  Future<Map<String, dynamic>> patchJson(
    String path, {
    Object? body,
    bool authenticated = true,
  }) async {
    final res = await patch<Map<String, dynamic>>(
      path,
      body: body,
      authenticated: authenticated,
    );
    return _unwrap(res.data);
  }

  /// DELETE that unwraps the backend envelope `{ success, timestamp, data }`.
  Future<Map<String, dynamic>> deleteJson(
    String path, {
    bool authenticated = true,
  }) async {
    final res = await _send<Map<String, dynamic>>(
      () => _dio.delete<Map<String, dynamic>>(
        path,
        options: Options(extra: {'auth': authenticated}),
      ),
    );
    return _unwrap(res.data);
  }

  /// GET that unwraps paginated responses `{ data: [...], meta: {...} }`.
  /// Returns `(items, meta)`.
  Future<(List<dynamic>, Map<String, dynamic>?)> getJsonPage(
    String path, {
    Map<String, dynamic>? query,
    bool authenticated = true,
  }) async {
    final res = await get<Map<String, dynamic>>(
      path,
      query: query,
      authenticated: authenticated,
    );
    final body = res.data ?? <String, dynamic>{};
    final data = body['data'];
    if (data is List) {
      final meta = body['meta'];
      return (
        data,
        meta is Map<String, dynamic> ? meta : null,
      );
    }
    if (data is Map<String, dynamic>) return ([data], null);
    if (data is List) return (data, null);
    return (<dynamic>[].cast<dynamic>(), null);
  }

  Future<Response<T>> _send<T>(Future<Response<T>> Function() request) async {
    try {
      return await request();
    } on DioException catch (e) {
      throw _toApiException(e);
    }
  }

  void _onRequest(RequestOptions options, RequestInterceptorHandler handler) {
    final requiresAuth = options.extra['auth'] != false;
    if (requiresAuth && _accessToken != null) {
      options.headers['Authorization'] = 'Bearer $_accessToken';
    }
    handler.next(options);
  }

  Future<void> _onError(
    DioException err,
    ErrorInterceptorHandler handler,
  ) async {
    final options = err.requestOptions;
    final requiresAuth = options.extra['auth'] != false;
    final alreadyRetried = options.extra['retried'] == true;

    if (err.response?.statusCode != 401 || !requiresAuth || alreadyRetried) {
      return handler.next(err);
    }

    try {
      final newToken = await _refreshTokens();
      options.extra['retried'] = true;
      options.headers['Authorization'] = 'Bearer $newToken';
      final response = await _dio.fetch<dynamic>(options);
      return handler.resolve(response);
    } on ApiException catch (e) {
      if (!e.isUnauthorized) {
        // Transient failure (offline / server restarting): keep the stored
        // session and surface the connectivity error instead of logging out.
        return handler.reject(
          DioException(
            requestOptions: options,
            type: err.type,
            error: err.error,
            message: e.message,
          ),
        );
      }
      await _tokens.clear();
      _accessToken = null;
      onSessionExpired?.call();
      return handler.next(err);
    }
  }

  /// Single-flight guard: when several parallel requests hit 401 at once,
  /// they must share ONE refresh round-trip instead of racing each other
  /// (the backend rotates refresh tokens, so the losers of a race would
  /// otherwise invalidate the fresh token and kill the session).
  Future<String>? _refreshing;

  Future<String> _refreshTokens() =>
      _refreshing ??= _doRefreshTokens().whenComplete(() => _refreshing = null);

  Future<String> _doRefreshTokens() async {
    final refreshToken = await _tokens.readRefreshToken();
    if (refreshToken == null) {
      throw ApiException('No refresh token', statusCode: 401);
    }

    try {
      final data = await _dio
          .post<Map<String, dynamic>>(
        '/auth/refresh',
        data: {'refreshToken': refreshToken},
        options: Options(extra: {'auth': false}),
      )
          .then((r) => _unwrap(r.data));
      final access = data['accessToken'] as String?;
      final refresh = data['refreshToken'] as String?;
      if (access == null || refresh == null) {
        throw ApiException('Malformed refresh response', statusCode: 401);
      }
      await _tokens.saveTokens(accessToken: access, refreshToken: refresh);
      _accessToken = access;
      return access;
    } on DioException catch (e) {
      final status = e.response?.statusCode;
      if (status == 401 || status == 403) {
        // The server explicitly rejected our refresh token.
        throw ApiException('Session expired', statusCode: 401);
      }
      // Unreachable server / timeout: keep the session alive for retry.
      throw ApiException(_toApiException(e).message);
    }
  }

  ApiException _toApiException(DioException e) {
    final data = e.response?.data;
    String message = e.message ?? 'Unexpected network error';
    if (data is Map<String, dynamic>) {
      final msg = data['message'];
      if (msg is String && msg.isNotEmpty) message = msg;
      if (msg is List && msg.isNotEmpty) message = msg.first.toString();
    }
    switch (e.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.receiveTimeout:
        message = 'Connection timed out. Check your internet.';
      case DioExceptionType.connectionError:
        message = 'Cannot reach the server. Check your connection.';
      default:
        break;
    }
    return ApiException(message, statusCode: e.response?.statusCode);
  }
}

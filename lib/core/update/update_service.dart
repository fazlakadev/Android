import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:path_provider/path_provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../features/auth/controllers/auth_controller.dart' show apiClientProvider;
import '../config.dart';
import 'app_version_info.dart';

enum UpdatePhase {
  idle,
  checking,
  upToDate,
  available,
  downloading,
  readyToInstall,
  error,
}

class UpdateState {
  const UpdateState({
    this.phase = UpdatePhase.idle,
    this.info,
    this.progress = 0,
    this.totalBytes = 0,
    this.receivedBytes = 0,
    this.downloadedPath,
    this.errorMessage,
  });

  final UpdatePhase phase;
  final AppVersionInfo? info;
  final double progress;
  final int totalBytes;
  final int receivedBytes;
  final String? downloadedPath;
  final String? errorMessage;

  UpdateState copyWith({
    UpdatePhase? phase,
    AppVersionInfo? info,
    double? progress,
    int? totalBytes,
    int? receivedBytes,
    String? downloadedPath,
    String? errorMessage,
    bool clearError = false,
  }) {
    return UpdateState(
      phase: phase ?? this.phase,
      info: info ?? this.info,
      progress: progress ?? this.progress,
      totalBytes: totalBytes ?? this.totalBytes,
      receivedBytes: receivedBytes ?? this.receivedBytes,
      downloadedPath: downloadedPath ?? this.downloadedPath,
      errorMessage: clearError ? null : (errorMessage ?? this.errorMessage),
    );
  }
}

const _channel = MethodChannel('fazlaka/installer');
const _deferredKey = 'update_deferred_version';

class UpdateController extends Notifier<UpdateState> {
  CancelToken? _cancelToken;

  @override
  UpdateState build() => const UpdateState();

  /// Fetch latest version info. Backend first, GitHub Releases as fallback.
  Future<AppVersionInfo?> fetchLatest() async {
    try {
      final res = await ref.read(apiClientProvider).raw.get(
            '/app-version/latest',
            queryParameters: {'platform': 'MOBILE'},
          );
      final data = res.data;
      if (data is Map && data['downloadUrl'] != null) {
        return AppVersionInfo.fromBackendJson(
          Map<String, dynamic>.from(data),
        );
      }
    } catch (_) {
      // fall through to GitHub.
    }
    try {
      final gh = Dio(BaseOptions(
        connectTimeout: const Duration(seconds: 15),
        receiveTimeout: const Duration(seconds: 15),
      ));
      final res = await gh.get<Map<String, dynamic>>(
        'https://api.github.com/repos/fazlakadev/Android/releases/latest',
      );
      return AppVersionInfo.fromGithubJson(res.data!);
    } catch (_) {
      return null;
    }
  }

  /// Silent check used at app start / FCM ping.
  /// Returns true when an actionable update was found.
  Future<bool> check({bool silent = true}) async {
    state = state.copyWith(phase: UpdatePhase.checking, clearError: true);
    final info = await fetchLatest();

    if (info == null ||
        !info.isDownloadable ||
        !info.isNewerThan(AppConfig.appVersion)) {
      state = state.copyWith(phase: silent ? UpdatePhase.idle : UpdatePhase.upToDate);
      return false;
    }

    final prefs = await SharedPreferences.getInstance();
    final deferredByUser =
        !info.forceUpdate && prefs.getString(_deferredKey) == info.version;

    state = state.copyWith(
      phase: deferredByUser ? UpdatePhase.idle : UpdatePhase.available,
      info: info,
    );
    return !deferredByUser;
  }

  Future<void> download() async {
    final info = state.info;
    if (info == null || state.phase == UpdatePhase.downloading) return;

    state = state.copyWith(
      phase: UpdatePhase.downloading,
      progress: 0,
      receivedBytes: 0,
      totalBytes: 0,
      clearError: true,
    );

    try {
      final base = await getExternalStorageDirectories();
      final dir = Directory('${base!.first.path}${Platform.pathSeparator}updates');
      await dir.create(recursive: true);
      final fileName =
          'fazlaka-${info.version}.apk';
      final savePath =
          '${dir.path}${Platform.pathSeparator}$fileName';

      _cancelToken = CancelToken();
      await Dio(BaseOptions(
        connectTimeout: const Duration(seconds: 30),
        receiveTimeout: const Duration(minutes: 15),
      )).download(
        info.downloadUrl,
        savePath,
        cancelToken: _cancelToken,
        onReceiveProgress: (received, total) {
          if (total > 0) {
            state = state.copyWith(
              receivedBytes: received,
              totalBytes: total,
              progress: received / total,
            );
          }
        },
      );

      state = state.copyWith(
        phase: UpdatePhase.readyToInstall,
        downloadedPath: savePath,
        progress: 1,
      );
      await install();
    } on DioException catch (e) {
      if (e.type == DioExceptionType.cancel) {
        state = state.copyWith(phase: UpdatePhase.available);
      } else {
        state = state.copyWith(
          phase: UpdatePhase.error,
          errorMessage: e.message,
        );
      }
    } catch (e) {
      state = state.copyWith(phase: UpdatePhase.error, errorMessage: '$e');
    } finally {
      _cancelToken = null;
    }
  }

  Future<void> cancelDownload() async {
    _cancelToken?.cancel();
  }

  /// Hands the APK to the Android package installer.
  Future<void> install() async {
    final path = state.downloadedPath;
    if (path == null) return;
    try {
      final allowed = await _channel.invokeMethod<bool>('canInstall');
      if (allowed != true) {
        await _channel.invokeMethod('openInstallPermissionSettings');
        return;
      }
      await _channel.invokeMethod('installApk', {'path': path});
    } on PlatformException catch (e) {
      state = state.copyWith(phase: UpdatePhase.error, errorMessage: e.message);
    }
  }

  /// "Later" — remember so we don't nag until the next version ships.
  Future<void> defer() async {
    final v = state.info?.version;
    if (v != null) {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString(_deferredKey, v);
    }
    state = state.copyWith(phase: UpdatePhase.idle);
  }

  void reset() {
    _cancelToken?.cancel();
    state = const UpdateState();
  }
}

final updateProvider =
    NotifierProvider<UpdateController, UpdateState>(UpdateController.new);

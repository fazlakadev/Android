import '../../../core/api/api_client.dart';
import '../models/user.dart';

class AuthRepository {
  AuthRepository(this._client);

  final ApiClient _client;

  /// Exchanges a Google ID token for the app's own JWT pair.
  Future<User> googleNativeLogin(String idToken) async {
    final data = await _client.postJson(
      '/auth/google/native',
      body: {'idToken': idToken},
      authenticated: false,
    );
    final access = data['accessToken'] as String?;
    final refresh = data['refreshToken'] as String?;
    if (access == null || refresh == null) {
      throw ApiException('Malformed login response');
    }
    await _client.persistTokens(
      accessToken: access,
      refreshToken: refresh,
    );
    return User.fromJson((data['user'] ?? <String, dynamic>{}) as Map<String, dynamic>);
  }

  /// Restores the session from the stored refresh token (auto-login).
  Future<User?> restoreSession() async {
    await _client.loadStoredToken();
    final refresh = await _client.readStoredRefreshToken();
    if (refresh == null) return null;
    try {
      final user = await me();
      return user;
    } on ApiException catch (e) {
      if (!e.isUnauthorized) rethrow;
      await _client.clearTokens();
      return null;
    }
  }

  Future<User> me() async => User.fromJson(await _client.getJson('/users/me'));

  Future<void> acceptTerms() async {
    await _client.postJson(
      '/auth/terms-accept',
      body: {'termsAccepted': true},
    );
  }

  /// Invalidates the refresh token server-side and wipes local tokens.
  Future<void> signOut() async {
    final refresh = await _client.readStoredRefreshToken();
    try {
      if (refresh != null) {
        await _client.postJson(
          '/auth/logout',
          body: {'refreshToken': refresh},
          authenticated: false,
        );
      }
    } on ApiException {
      // Best-effort: local sign-out must always succeed.
    } finally {
      await _client.clearTokens();
    }
  }
}

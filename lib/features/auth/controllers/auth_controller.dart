import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_sign_in/google_sign_in.dart';

import '../../../core/api/api_client.dart';
import '../../../core/config.dart';
import '../../../core/storage/token_storage.dart';
import '../data/auth_repository.dart';
import '../models/user.dart';

final tokenStorageProvider =
    Provider<TokenStorage>((ref) => TokenStorage());

/// Lightweight session-expired event bus.
class SessionEvents {
  final _listeners = <void Function()>[];

  void addListener(void Function() listener) => _listeners.add(listener);

  void removeListener(void Function() listener) => _listeners.remove(listener);

  void notify() {
    for (final listener in List.of(_listeners)) {
      listener();
    }
  }
}

final sessionEventsProvider =
    Provider<SessionEvents>((_) => SessionEvents());

final apiClientProvider = Provider<ApiClient>((ref) {
  final client = ApiClient(ref.watch(tokenStorageProvider));
  return client;
});

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  final client = ref.watch(apiClientProvider);
  final events = ref.watch(sessionEventsProvider);

  // When the refresh flow finally fails, drop back to the login screen.
  client.onSessionExpired = events.notify;
  return AuthRepository(client);
});

enum AuthStatus { initializing, unauthenticated, authenticated }

class AuthState {
  const AuthState({
    this.status = AuthStatus.initializing,
    this.user,
    this.error,
    this.busy = false,
  });

  final AuthStatus status;
  final User? user;
  final String? error;
  final bool busy;

  AuthState copyWith({
    AuthStatus? status,
    User? user,
    bool clearUser = false,
    String? error,
    bool? busy,
    bool clearError = false,
  }) =>
      AuthState(
        status: status ?? this.status,
        user: clearUser ? null : (user ?? this.user),
        error: clearError ? null : (error ?? this.error),
        busy: busy ?? this.busy,
      );
}

class AuthController extends Notifier<AuthState> {
  GoogleSignIn get _googleSignIn => GoogleSignIn.instance;

  bool _googleInitialized = false;

  @override
  AuthState build() {
    // Restore any previous session once on startup.
    Future<void>.microtask(bootstrap);
    final events = ref.watch(sessionEventsProvider);
    events.addListener(_onSessionExpired);
    ref.onDispose(() => events.removeListener(_onSessionExpired));
    return const AuthState();
  }

  void _onSessionExpired() {
    if (state.status == AuthStatus.authenticated) {
      state = const AuthState(status: AuthStatus.unauthenticated);
    }
  }

  /// Restores the session from stored JWTs on app start (auto-login).
  Future<void> bootstrap() async {
    try {
      final user = await ref.read(authRepositoryProvider).restoreSession();
      state = AuthState(
        status: user == null
            ? AuthStatus.unauthenticated
            : AuthStatus.authenticated,
        user: user,
      );
    } on ApiException catch (e) {
      state = AuthState(
        status: AuthStatus.unauthenticated,
        error: e.message,
      );
    } catch (_) {
      state = const AuthState(status: AuthStatus.unauthenticated);
    }
  }

  Future<void> _ensureGoogleInitialized() async {
    if (_googleInitialized) return;
    await _googleSignIn.initialize(
      serverClientId: AppConfig.googleServerClientId,
    );
    _googleInitialized = true;
  }

  /// Native Google account picker (Credential Manager) followed by a token
  /// exchange against the NestJS backend. No WebView involved.
  Future<bool> signInWithGoogle() async {
    if (state.busy) return false;
    state = state.copyWith(busy: true, clearError: true);

    try {
      await _ensureGoogleInitialized();
      final account = await _googleSignIn.authenticate();
      final idToken = account.authentication.idToken;
      if (idToken == null) {
        throw ApiException(
          'Google did not return an ID token. Check the OAuth client setup.',
        );
      }

      final user =
          await ref.read(authRepositoryProvider).googleNativeLogin(idToken);

      // Clear the native session copy; our own JWT pair is what matters now.
      unawaited(_googleSignIn.signOut().catchError((_) {}));

      state = AuthState(status: AuthStatus.authenticated, user: user);
      return true;
    } on GoogleSignInException catch (e) {
      if (e.code == GoogleSignInExceptionCode.canceled) {
        state = state.copyWith(busy: false, clearError: true);
        return false;
      }
      state = state.copyWith(
        busy: false,
        error: 'Google sign-in failed (${e.description ?? e.code.name}).',
      );
      return false;
    } on ApiException catch (e) {
      state = state.copyWith(busy: false, error: e.message);
      return false;
    } catch (e) {
      state = state.copyWith(busy: false, error: 'Sign-in failed: $e');
      return false;
    }
  }

  /// Replaces the cached user after a profile update.
  void applyUpdatedUser(User updated) {
    if (state.status == AuthStatus.authenticated) {
      state = state.copyWith(user: updated);
    }
  }

  Future<void> acceptTerms() async {
    try {
      await ref.read(authRepositoryProvider).acceptTerms();
      final user = state.user;
      if (user != null) {
        state = state.copyWith(
          user: user.copyWithLocal(termsAccepted: true),
          clearError: true,
        );
      }
    } on ApiException catch (e) {
      state = state.copyWith(error: e.message);
    }
  }

  Future<void> signOut() async {
    try {
      await _ensureGoogleInitialized();
      await _googleSignIn.signOut();
    } catch (_) {}
    try {
      await ref.read(authRepositoryProvider).signOut();
    } finally {
      state = const AuthState(status: AuthStatus.unauthenticated);
    }
  }
}

final authControllerProvider =
    NotifierProvider<AuthController, AuthState>(AuthController.new);

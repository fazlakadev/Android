class AppConfig {
  AppConfig._();

  static const appVersion = '1.0.0';

  /// Backend base URL (cloud-hosted).
  ///
  /// Production:   https://back-end-hq0is.faable.link
  /// Local debug:  flutter run --dart-define=API_BASE_URL=http://10.0.2.2:3001
  static const apiBaseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'https://back-end-hq0is.faable.link',
  );

  static const apiUrl = '$apiBaseUrl/api/v1';

  /// Google **Web** client id (server client id). The ID token returned by
  /// native Google Sign-In is issued with this audience and the NestJS
  /// backend verifies it against GOOGLE_CLIENT_ID.
  ///
  /// The *Android* OAuth client (package com.fazlaka.app + SHA-1) must be
  /// registered in the same Google Cloud project to authorize the app.
  static const googleServerClientId = String.fromEnvironment(
    'GOOGLE_SERVER_CLIENT_ID',
    defaultValue:
        '919871876990-hqb49huhl0gg2osdcg7jv7e39adf9fo1.apps.googleusercontent.com',
  );
}

import 'package:flutter_test/flutter_test.dart';

import 'package:fazlaka/core/config.dart';

void main() {
  test('api url is derived from base url', () {
    expect(AppConfig.apiUrl, endsWith('/api/v1'));
  });

  test('google server client id is configured', () {
    expect(AppConfig.googleServerClientId, isNotEmpty);
    expect(AppConfig.googleServerClientId, contains('.apps.googleusercontent.com'));
  });
}

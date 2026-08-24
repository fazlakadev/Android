import 'dart:io';

import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../features/auth/controllers/auth_controller.dart';
import '../config.dart';
import '../update/update_service.dart';

const _generalChannel = AndroidNotificationChannel(
  'fazlaka_general',
  'Fazlaka notifications',
  description: 'General announcements and updates',
  importance: Importance.high,
);

final FlutterLocalNotificationsPlugin _localNotifications =
    FlutterLocalNotificationsPlugin();

bool _notificationsReady = false;

Future<void> _ensureNotificationChannel() async {
  if (_notificationsReady) return;
  await _localNotifications
      .resolvePlatformSpecificImplementation<
          AndroidFlutterLocalNotificationsPlugin>()
      ?.createNotificationChannel(_generalChannel);
  await _localNotifications.initialize(
    settings: const InitializationSettings(
      android: AndroidInitializationSettings('@mipmap/ic_launcher'),
    ),
  );
  _notificationsReady = true;
}

void _showLocalNotification(String? title, String? body) {
  if (title == null && body == null) return;
  _localNotifications.show(
    id: DateTime.now().millisecondsSinceEpoch ~/ 1000 % 0x7fffffff,
    title: title ?? 'Fazlaka',
    body: body,
    notificationDetails: NotificationDetails(
      android: AndroidNotificationDetails(
        _generalChannel.id,
        _generalChannel.name,
        channelDescription: _generalChannel.description,
        importance: Importance.high,
        priority: Priority.high,
      ),
    ),
  );
}

@pragma('vm:entry-point')
Future<void> firebaseBackgroundHandler(RemoteMessage message) async {
  await Firebase.initializeApp();
}

class PushController extends Notifier<bool> {
  @override
  bool build() => false;

  Future<void> init() async {
    if (state) return;

    await Firebase.initializeApp();
    FirebaseMessaging.onBackgroundMessage(firebaseBackgroundHandler);
    await _ensureNotificationChannel();

    final messaging = FirebaseMessaging.instance;

    // Android 13+ runtime permission.
    await messaging.requestPermission(provisional: true);

    // Broadcast channel so console announcements reach everyone.
    try {
      await messaging.subscribeToTopic('all');
    } catch (_) {}

    // Foreground messages -> local notification.
    FirebaseMessaging.onMessage.listen((message) {
      _showLocalNotification(
        message.notification?.title,
        message.notification?.body,
      );
      _handleData(message.data);
    });

    // Tapped while backgrounded.
    FirebaseMessaging.onMessageOpenedApp.listen((message) {
      _handleData(message.data);
    });

    // Cold start from a terminated notification.
    final initial = await messaging.getInitialMessage();
    if (initial != null) {
      _handleData(initial.data);
    }

    messaging.onTokenRefresh.listen(_registerToken);

    state = true;
  }

  /// Called once auth is (possibly) restored to register this device.
  Future<void> registerAfterLogin() async {
    try {
      final token = await FirebaseMessaging.instance.getToken();
      if (token != null) await _registerToken(token);
    } catch (_) {}
  }

  void _handleData(Map<String, dynamic> data) {
    if (data['type'] == 'update') {
      ref.read(updateProvider.notifier).check(silent: true);
    }
  }

  Future<void> _registerToken(String token) async {
    final authed =
        ref.read(authControllerProvider).status == AuthStatus.authenticated;
    if (!authed) return;
    try {
      await ref.read(apiClientProvider).raw.post('/push/devices', data: {
        'token': token,
        'platform': 'android',
        'os': Platform.operatingSystemVersion,
        'appVersion': AppConfig.appVersion,
      });
    } catch (_) {
      // Best effort — topic broadcasts still work without registration.
    }
  }
}

final pushInitProvider =
    NotifierProvider<PushController, bool>(PushController.new);

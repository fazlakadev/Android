import 'dart:async';
import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:pusher_channels_flutter/pusher_channels_flutter.dart';

import '../config.dart';
import '../../features/auth/controllers/auth_controller.dart';

/// A live event delivered on the user's private channel.
class RealtimeEvent {
  const RealtimeEvent({required this.event, required this.data});

  final String event;
  final Map<String, dynamic> data;
}

/// Pusher websocket client bound to `private-user-<id>` so chats update
/// instantly instead of waiting for the next poll cycle.
class RealtimeController extends Notifier<bool> {
  @override
  bool build() {
    ref.onDispose(_events.close);
    return false;
  }

  String? _boundUserId;

  final StreamController<RealtimeEvent> _events =
      StreamController<RealtimeEvent>.broadcast();

  /// Live events for the current user (message:new …).
  Stream<RealtimeEvent> get events => _events.stream;

  Future<void> start() async {
    final myId = ref.read(apiClientProvider).userId ?? '';
    if (myId.isEmpty) return;
    if (_boundUserId == myId && state) return;

    _boundUserId = myId;

    try {
      final token = ref.read(apiClientProvider).accessToken ?? '';
      final pusher = PusherChannelsFlutter.getInstance();
      await pusher.init(
        apiKey: AppConfig.pusherKey,
        cluster: AppConfig.pusherCluster,
        onEvent: _onPusherEvent,
        onAuthorizer: (channelName, socketId, _) async =>
            _authorize(channelName, socketId, token),
      );
      if (pusher.connectionState != 'CONNECTED') {
        await pusher.connect();
      }
      await pusher.subscribe(channelName: 'private-user-$myId');
      state = true;
    } catch (_) {
      // Realtime is best-effort; polling remains the safety net.
      state = false;
    }
  }

  void _onPusherEvent(PusherEvent event) {
    if (event.eventName != 'message:new') return;
    var raw = event.data;
    if (raw is String) {
      try {
        raw = jsonDecode(raw);
      } catch (_) {
        return;
      }
    }
    if (raw is Map) {
      _events.add(RealtimeEvent(
        event: 'message:new',
        data: Map<String, dynamic>.from(raw),
      ));
    }
  }

  Future<Map<String, dynamic>> _authorize(
    String channelName,
    String socketId,
    String token,
  ) async {
    try {
      final res = await ref.read(apiClientProvider).raw.post<dynamic>(
            '/realtime/pusher/auth',
            data: <String, dynamic>{
              'socket_id': socketId,
              'channel_name': channelName,
            },
            options: Options(
              headers: {'Authorization': 'Bearer $token'},
            ),
          );
      final body = res.data;
      if (body is Map) {
        final inner = body['data'];
        final map = inner is Map ? inner : body;
        return Map<String, dynamic>.from(map);
      }
    } catch (_) {}
    return <String, dynamic>{};
  }

  Future<void> stop() async {
    try {
      await PusherChannelsFlutter.getInstance().disconnect();
    } catch (_) {}
    state = false;
  }
}

final realtimeProvider =
    NotifierProvider<RealtimeController, bool>(RealtimeController.new);

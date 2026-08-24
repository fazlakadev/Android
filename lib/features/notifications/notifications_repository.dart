import '../../../core/api/api_client.dart';

class NotificationItem {
  const NotificationItem({
    required this.id,
    required this.title,
    this.body,
    this.read = false,
    this.createdAt,
  });

  final String id;
  final String title;
  final String? body;
  final bool read;
  final DateTime? createdAt;

  /// The backend may store unresolved i18n keys; translate the known ones.
  static const _keyTranslations = {
    'common.supportReplyTitle': 'رد على تذكرة الدعم',
    'common.supportReplyBody': 'فريق الدعم قام بالرد على تذكرتك',
    'common.newEpisodeTitle': 'حلقة جديدة',
    'common.newEpisodeBody': 'تم نشر حلقة جديدة، شاهدها الآن',
    'notifications.friendRequest': 'طلب صداقة جديد',
    'notifications.friendAccepted': 'تم قبول طلب الصداقة',
    'notifications.newMessage': 'رسالة جديدة',
  };

  static String _t(String? raw) {
    if (raw == null || raw.isEmpty) return '';
    return _keyTranslations[raw] ?? raw;
  }

  factory NotificationItem.fromJson(Map<String, dynamic> j) {
    DateTime? created;
    final raw = j['createdAt']?.toString();
    if (raw != null) created = DateTime.tryParse(raw)?.toLocal();
    return NotificationItem(
      id: (j['id'] ?? '').toString(),
      title: _t((j['title'] ?? j['key'] ?? '').toString()),
      body: () {
        final b = j['body']?.toString();
        if (b != null && b.isNotEmpty && !b.startsWith('{')) return _t(b);
        return null;
      }(),
      read: j['readAt'] != null || j['read'] == true,
      createdAt: created,
    );
  }
}

class NotificationsRepository {
  NotificationsRepository(this._client);

  final ApiClient _client;

  Future<(List<NotificationItem>, int)> list({int page = 1}) async {
    final body = await _client.getJsonPage(
      '/notifications',
      query: {'page': page, 'limit': 30},
    );
    return (
      body.$1.whereType<Map<String, dynamic>>().map(NotificationItem.fromJson).toList(),
      body.$2?['unreadCount'] as int? ?? 0,
    );
  }

  Future<int> unreadCount() async {
    final data = await _client.getJson('/notifications/unread-count');
    return (data['count'] ?? data['unreadCount'] ?? 0) as int;
  }

  Future<void> markRead(String id) =>
      _client.patchJson('/notifications/read', body: {'id': id});

  Future<void> remove(String id) => _client.deleteJson('/notifications/$id');
}

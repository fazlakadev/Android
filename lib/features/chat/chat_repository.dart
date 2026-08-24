import 'package:dio/dio.dart';

import '../../../core/api/api_client.dart';
import '../content/models.dart';

class ConversationDetail {
  const ConversationDetail({
    required this.id,
    required this.kind,
    required this.title,
    this.avatarUrl,
    required this.messages,
  });

  final String id;
  final String kind;
  final String title;
  final String? avatarUrl;
  final List<ChatMessage> messages;

  factory ConversationDetail.fromJson(
    Map<String, dynamic> j,
    String myUserId,
  ) {
    final conversation = j['conversation'] as Map<String, dynamic>? ?? j;
    final other = conversation['other'] as Map<String, dynamic>?;
    final group = conversation['group'] as Map<String, dynamic>?;
    final groupName = group?['name']?.toString();
    final rawMessages = j['messages'];
    return ConversationDetail(
      id: (conversation['id'] ?? '').toString(),
      kind: (conversation['kind'] ?? 'direct').toString(),
      title: (groupName != null && groupName.isNotEmpty)
          ? groupName
          : (other?['name']?.toString().isNotEmpty == true
              ? other!['name'].toString()
              : '@${other?['username'] ?? ''}'),
      avatarUrl: (group?['avatarUrl'] ?? other?['avatarUrl']) as String?,
      messages: rawMessages is List
          ? rawMessages
              .whereType<Map<String, dynamic>>()
              .map(ChatMessage.fromJson)
              .toList()
          : <ChatMessage>[],
    );
  }
}

class ChatRepository {
  ChatRepository(this._client);

  final ApiClient _client;

  Future<List<ConversationItem>> conversations({int limit = 50}) async {
    final (items, _) = await _client.getJsonPage('/messages/conversations',
        query: {'page': 1, 'limit': limit});
    return items
        .whereType<Map<String, dynamic>>()
        .map(ConversationItem.fromListJson)
        .toList();
  }

  Future<ConversationDetail> detail(String conversationId) async {
    final data = await _client.getJson('/messages/conversations/$conversationId',
        query: {'page': 1, 'limit': 50});
    return ConversationDetail.fromJson(data, _client.userId ?? '');
  }

  /// Creates (or returns) a direct conversation with [userId].
  Future<ConversationDetail> openWith(String userId) async {
    final data = await _client.postJson('/messages/conversations',
        body: {'userId': userId});
    return ConversationDetail.fromJson(data, _client.userId ?? '');
  }

  Future<void> send(String conversationId, String body) =>
      _client.postJson('/messages/conversations/$conversationId/messages',
          body: {'type': 'text', 'body': body});

  /// Uploads [bytes] to `/upload/chat?kind=` then sends a media message.
  Future<void> sendAttachment(
    String conversationId, {
    required String kind,
    required String fileName,
    required List<int> bytes,
    int? durationSec,
  }) async {
    final uploaded = await _client.postMultipart(
      '/upload/chat',
      query: {'kind': kind},
      file: MultipartFile.fromBytes(bytes, filename: fileName),
    ) as Map<String, dynamic>? ??
        <String, dynamic>{};
    final url = (uploaded['url'] ?? '').toString();
    if (url.isEmpty) {
      throw Exception('Upload failed');
    }
    await _client.postJson(
      '/messages/conversations/$conversationId/messages',
      body: {
        'type': kind,
        'attachmentUrl': url,
        if (uploaded['mime'] != null) 'attachmentMime': uploaded['mime'],
        'attachmentName': fileName,
        if (uploaded['size'] is num)
          'attachmentSize': (uploaded['size'] as num).toInt(),
        'durationSec': ?durationSec,
      },
    );
  }

  Future<void> markRead(String conversationId) =>
      _client.patchJson('/messages/conversations/$conversationId/read');
}

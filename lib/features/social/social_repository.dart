import '../../../core/api/api_client.dart';
import '../content/models.dart';

class SocialRepository {
  SocialRepository(this._client);

  final ApiClient _client;

  Future<List<CommentItem>> comments(
    String contentType,
    String contentId, {
    int page = 1,
    int limit = 20,
  }) async {
    final (items, _) = await _client.getJsonPage(
      '/comments/$contentType/$contentId',
      query: {'page': page, 'limit': limit},
    );
    return items
        .whereType<Map<String, dynamic>>()
        .map(CommentItem.fromJson)
        .toList();
  }

  Future<List<CommentItem>> replies(
    String commentId, {
    int page = 1,
    int limit = 20,
  }) async {
    final (items, _) = await _client.getJsonPage(
      '/comments/replies/$commentId',
      query: {'page': page, 'limit': limit},
    );
    return items
        .whereType<Map<String, dynamic>>()
        .map(CommentItem.fromJson)
        .toList();
  }

  Future<CommentItem> addComment({
    required String contentType,
    required String contentId,
    required String body,
    String? parentId,
  }) async {
    final data = await _client.postJson('/comments', body: {
      'contentType': contentType,
      'contentId': contentId,
      'body': body,
      'parentId': ?parentId,
    });
    return CommentItem.fromJson(data);
  }

  Future<void> updateComment(String id, String body) =>
      _client.patchJson('/comments/$id', body: {'body': body});

  Future<void> deleteComment(String id) => _client.deleteJson('/comments/$id');

  Future<({bool liked, String? type})> toggleLike(
    String contentType,
    String contentId,
  ) async {
    final data =
        await _client.postJson('/likes/$contentType/$contentId');
    return (
      liked: data['liked'] == true,
      type: data['type']?.toString(),
    );
  }

  Future<({bool liked, String? type})> likeStatus(
    String contentType,
    String contentId,
  ) async {
    final data =
        await _client.getJson('/likes/$contentType/$contentId/status');
    return (
      liked: data['liked'] == true,
      type: data['type']?.toString(),
    );
  }

  Future<int> likeCount(String contentType, String contentId) async {
    final data = await _client
        .getJson('/likes/$contentType/$contentId', authenticated: false);
    dynamic v = data.containsKey('data') ? data['data'] : data;
    if (v is Map) v = v['count'] ?? v['likesCount'] ?? v['total'];
    return _asInt(v) ?? 0;
  }

  // ---------- Ratings ----------

  Future<({double average, int count})> ratingSummary(
    String contentType,
    String contentId,
  ) async {
    final data = await _client.getJson(
      '/ratings/content/$contentType/$contentId/summary',
      authenticated: false,
    );
    dynamic v = data.containsKey('data') ? data['data'] : data;
    if (v is! Map) return (average: 0.0, count: 0);
    return (
      average: (double.tryParse(v['average']?.toString() ?? '') ?? 0),
      count: _asInt(v['count']) ?? 0,
    );
  }

  Future<int> myRating(String contentType, String contentId) async {
    try {
      final data =
          await _client.getJson('/ratings/mine?contentType=$contentType&contentId=$contentId');
      dynamic v = data.containsKey('data') ? data['data'] : data;
      if (v is List) v = v.isEmpty ? null : v.first;
      if (v is Map) return _asInt(v['value']) ?? 0;
      return 0;
    } on ApiException catch (e) {
      if (e.statusCode == 404) return 0;
      rethrow;
    }
  }

  Future<void> rateContent({
    required String contentType,
    required String contentId,
    required int value,
  }) =>
      _client.postJson('/ratings', body: {
        'contentType': contentType,
        'contentId': contentId,
        'value': value.clamp(1, 5),
      });

  // ---------- Views / Progress ----------

  Future<void> trackView(String contentType, String contentId) async {
    try {
      await _client.postJson(
        '/views/track',
        body: {'contentType': contentType, 'contentId': contentId},
      );
    } catch (_) {}
  }

  Future<int> savedProgress(String episodeId) async {
    try {
      final data = await _client.getJson('/progress/$episodeId');
      dynamic v = data.containsKey('data') ? data['data'] : data;
      if (v is Map) return _asInt(v['positionSeconds']) ?? 0;
      return 0;
    } on ApiException {
      return 0;
    }
  }

  Future<void> saveProgress(
    String episodeId, {
    required int positionSeconds,
    int? durationSeconds,
  }) async {
    try {
      await _client.patchJson('/progress/$episodeId', body: {
        'positionSeconds': positionSeconds,
        'durationSeconds': ?durationSeconds,
      });
    } catch (_) {}
  }

  static int? _asInt(dynamic v) =>
      v == null ? null : int.tryParse(v.toString());
}

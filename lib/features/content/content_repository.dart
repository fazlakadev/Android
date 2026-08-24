import '../../core/i18n/app_i18n.dart';
import '../../../core/api/api_client.dart';
import '../auth/models/user.dart';
import 'models.dart';

class ContentRepository {
  ContentRepository(this._client);

  final ApiClient _client;

  Future<List<BannerItem>> banners() async {
    final (items, _) = await _client.getJsonPage(
      '/banners',
      query: {'locale': i18nRuntime.name, 'position': 'hero'},
    );
    return items
        .whereType<Map<String, dynamic>>()
        .map(BannerItem.fromJson)
        .where((b) => b.imageUrl.isNotEmpty)
        .toList();
  }

  /// Returns `(items, hasNextPage)` straight from the server's pagination meta.
  Future<(List<ArticleItem>, bool)> articles({int page = 1, int limit = 10}) async {
    final (items, meta) = await _client.getJsonPage(
      '/articles',
      query: {'page': page, 'limit': limit, 'locale': i18nRuntime.name},
      authenticated: false,
    );
    return (
      items.whereType<Map<String, dynamic>>().map(ArticleItem.fromJson).toList(),
      meta?['hasNextPage'] == true,
    );
  }

  Future<ArticleItem> articleDetail(String idOrSlug) async {
    final data = await _client.getJson('/articles/$idOrSlug',
        authenticated: false);
    return ArticleItem.fromJson(data);
  }

  /// Returns `(items, hasNextPage)` straight from the server's pagination meta.
  Future<(List<EpisodeItem>, bool)> episodes({
    int page = 1,
    int limit = 12,
    String? search,
  }) async {
    final (items, meta) = await _client.getJsonPage(
      '/episodes',
      query: {
        'page': page,
        'limit': limit,
        'locale': i18nRuntime.name,
        if (search != null && search.trim().isNotEmpty) 'search': search.trim(),
      },
      authenticated: false,
    );
    return (
      items.whereType<Map<String, dynamic>>().map(EpisodeItem.fromJson).toList(),
      meta?['hasNextPage'] == true,
    );
  }

  Future<EpisodeItem> episodeDetail(String idOrSlug) async {
    final data =
        await _client.getJson('/episodes/$idOrSlug', authenticated: false);
    return EpisodeItem.fromJson(data);
  }

  /// Returns `(items, hasNextPage)` straight from the server's pagination meta.
  Future<(List<SeasonItem>, bool)> seasons({int page = 1, int limit = 20}) async {
    final (items, meta) = await _client.getJsonPage(
      '/seasons',
      query: {'page': page, 'limit': limit, 'locale': i18nRuntime.name},
      authenticated: false,
    );
    return (
      items.whereType<Map<String, dynamic>>().map(SeasonItem.fromJson).toList(),
      meta?['hasNextPage'] == true,
    );
  }

  Future<SeasonItem> seasonDetail(String idOrSlug) async {
    final data =
        await _client.getJson('/seasons/$idOrSlug', authenticated: false);
    return SeasonItem.fromJson(data);
  }

  Future<List<PlaylistSummary>> playlists({int page = 1, int limit = 50}) async {
    final (items, _) = await _client.getJsonPage(
      '/playlists',
      query: {'page': page, 'limit': limit, 'locale': i18nRuntime.name},
    );
    return items
        .whereType<Map<String, dynamic>>()
        .map(PlaylistSummary.fromJson)
        .toList();
  }

  Future<(PlaylistSummary, List<EpisodeItem>)> playlistDetail(
    String idOrSlug,
  ) async {
    final data = await _client.getJson('/playlists/$idOrSlug');
    final rawItems = data['items'];
    final episodes = rawItems is List
        ? rawItems
            .map((i) => i is Map<String, dynamic> ? i['episode'] : null)
            .whereType<Map<String, dynamic>>()
            .where((e) => (e['id'] ?? '').toString().isNotEmpty)
            .map(EpisodeItem.fromJson)
            .toList()
        : <EpisodeItem>[];
    return (PlaylistSummary.fromDetailJson(data), episodes);
  }

  String _slugify(String title) {
    final base = title
        .toLowerCase()
        .replaceAll(RegExp(r'[^a-z0-9\u0600-\u06FF]+'), '-')
        .replaceAll(RegExp(r'^-+|-+$'), '');
    final stamp = DateTime.now().millisecondsSinceEpoch.toRadixString(36);
    var slug = base.isEmpty ? 'playlist' : base;
    if (slug.length > 180) slug = slug.substring(0, 180);
    return '$slug-$stamp';
  }

  Future<PlaylistSummary> createPlaylist({
    required String title,
    String? description,
  }) async {
    final data = await _client.postJson('/playlists', body: {
      'slug': _slugify(title),
      'platform': 'MOBILE',
      'isPublic': true,
      'translations': [
        {
          'locale': i18nRuntime.name,
          'title': title,
          if (description != null && description.isNotEmpty)
            'description': description,
        },
      ],
    });
    return PlaylistSummary.fromDetailJson(data);
  }

  Future<void> deletePlaylist(String id) => _client.deleteJson('/playlists/$id');

  Future<void> addToPlaylist(String playlistId, String episodeId) =>
      _client.postJson('/playlists/$playlistId/items',
          body: {'episodeId': episodeId});

  Future<void> removeFromPlaylist(String playlistId, String episodeId) =>
      _client.deleteJson('/playlists/$playlistId/items/$episodeId');

  Future<List<FriendItem>> friends() async {
    final (items, _) = await _client.getJsonPage('/friends',
        query: {'limit': 100});
    return items
        .whereType<Map<String, dynamic>>()
        .map(FriendItem.fromJson)
        .toList();
  }

  Future<List<FriendItem>> incomingRequests() async {
    final (items, _) = await _client.getJsonPage('/friends/requests/incoming');
    return items
        .whereType<Map<String, dynamic>>()
        .map(FriendItem.fromJson)
        .toList();
  }

  Future<List<FriendItem>> suggestions() async {
    final (items, _) = await _client.getJsonPage('/friends/suggestions');
    return items
        .whereType<Map<String, dynamic>>()
        .map(FriendItem.fromJson)
        .toList();
  }

  Future<void> sendFriendRequest(String userId) =>
      _client.postJson('/friends/request/$userId');

  Future<void> acceptRequest(String requestId) =>
      _client.postJson('/friends/requests/$requestId/accept');

  Future<void> rejectRequest(String requestId) =>
      _client.postJson('/friends/requests/$requestId/reject');

  Future<void> removeFriend(String friendUserId) =>
      _client.deleteJson('/friends/$friendUserId');

  Future<User> updateProfile({
    String? name,
    String? username,
    String? bio,
  }) async {
    final body = <String, dynamic>{
      'name': ?name,
      'username': ?username,
      'bio': ?bio,
    };
    final data = await _client.patchJson(
      '/users/me',
      body: body,
    );
    return User.fromJson(data);
  }

  Future<Preferences> preferences() async =>
      Preferences.fromJson(await _client.getJson('/users/me/preferences'));

  Future<Preferences> updatePreferences(Map<String, bool> values) async =>
      Preferences.fromJson(
        await _client.patchJson('/users/me/preferences', body: values),
      );

  Future<void> changePassword({
    required String currentPassword,
    required String newPassword,
  }) =>
      _client.postJson(
        '/auth/change-password',
        body: {
          'currentPassword': currentPassword,
          'newPassword': newPassword,
        },
      );
}

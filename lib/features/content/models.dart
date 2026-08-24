import '../../core/i18n/app_i18n.dart';

class ContentModels {
  ContentModels._();
}

String pickTranslation(
  dynamic translations,
  String key, {
  String? locale,
}) {
  final loc = (locale ?? i18nRuntime.name).toLowerCase();
  if (translations is! List || translations.isEmpty) return '';
  final matching = translations.whereType<Map<String, dynamic>>().toList();
  Map<String, dynamic>? best;
  for (final t in matching) {
    if ((t['locale'] ?? '').toString().toLowerCase() == loc) {
      best = t;
      break;
    }
  }
  best ??= matching.firstWhere(
    (t) => (t['locale'] ?? '').toString().toLowerCase() == 'en',
    orElse: () => matching.first,
  );
  return (best[key] ?? '').toString();
}

DateTime? _date(dynamic v) =>
    v == null ? null : DateTime.tryParse(v.toString());

int? _int(dynamic v) => v == null ? null : int.tryParse(v.toString());

class BannerItem {
  const BannerItem({required this.id, required this.imageUrl, this.linkUrl});

  final String id;
  final String imageUrl;
  final String? linkUrl;

  factory BannerItem.fromJson(Map<String, dynamic> j) => BannerItem(
        id: (j['id'] ?? '').toString(),
        imageUrl: (j['imageUrl'] ?? j['image'] ?? '').toString(),
        linkUrl: j['linkUrl'] as String?,
      );
}

class ArticleItem {
  const ArticleItem({
    required this.id,
    required this.slug,
    required this.title,
    this.excerpt,
    this.coverImage,
    this.category,
    this.publishedAt,
    this.viewsCount,
    this.body = '',
  });

  final String id;
  final String slug;
  final String title;
  final String? excerpt;
  final String? coverImage;
  final String? category;
  final DateTime? publishedAt;
  final int? viewsCount;
  final String body;

  factory ArticleItem.fromJson(Map<String, dynamic> j) {
    final t = j['translations'];
    return ArticleItem(
      id: (j['id'] ?? '').toString(),
      slug: (j['slug'] ?? '').toString(),
      title: pickTranslation(t, 'title').isNotEmpty
          ? pickTranslation(t, 'title')
          : pickTranslation(t, 'title'),
      excerpt: pickTranslation(t, 'excerpt'),
      coverImage: j['coverImage'] as String?,
      category: j['category'] as String?,
      publishedAt: _date(j['publishedAt']),
      viewsCount: _int(j['viewsCount']),
      body: pickTranslation(t, 'body'),
    );
  }
}

class EpisodeItem {
  const EpisodeItem({
    required this.id,
    required this.slug,
    required this.title,
    this.excerpt,
    this.coverImage,
    this.category,
    this.episodeNumber,
    this.durationSeconds,
    this.releaseYear,
    this.videoUrl,
    this.audioUrl,
  });

  final String id;
  final String slug;
  final String title;
  final String? excerpt;
  final String? coverImage;
  final String? category;
  final int? episodeNumber;
  final int? durationSeconds;
  final int? releaseYear;
  final String? videoUrl;
  final String? audioUrl;

  String get durationLabel {
    final d = durationSeconds;
    if (d == null || d <= 0) return '';
    final m = d ~/ 60;
    final s = d % 60;
    if (m >= 60) {
      final h = m ~/ 60;
      return '${h}h ${(m % 60).toString().padLeft(2, '0')}m';
    }
    return '${m}m ${s.toString().padLeft(2, '0')}s';
  }

  factory EpisodeItem.fromJson(Map<String, dynamic> j) {
    final t = j['translations'];
    return EpisodeItem(
      id: (j['id'] ?? '').toString(),
      slug: (j['slug'] ?? '').toString(),
      title: pickTranslation(t, 'title').isNotEmpty
          ? pickTranslation(t, 'title')
          : pickTranslation(t, 'title'),
      excerpt: pickTranslation(t, 'excerpt'),
      coverImage: j['coverImage'] as String?,
      category: j['category'] as String?,
      episodeNumber: _int(j['episodeNumber']),
      durationSeconds: _int(j['duration']),
      releaseYear: _int(j['releaseYear']),
      videoUrl: j['videoUrl'] as String?,
      audioUrl: j['audioUrl'] as String?,
    );
  }
}

class FriendItem {
  const FriendItem({
    required this.userId,
    this.name,
    this.username,
    this.avatarUrl,
    this.requestId,
  });

  /// User id of the friend / sender / suggested user.
  final String userId;

  /// Only set for incoming friend requests.
  final String? requestId;
  final String? name;
  final String? username;
  final String? avatarUrl;

  String get displayName =>
      (name != null && name!.trim().isNotEmpty) ? name! : '@${username ?? userId}';

  static FriendItem fromUser(Map<String, dynamic> u, {String? requestId}) =>
      FriendItem(
        userId: (u['id'] ?? u['userId'] ?? '').toString(),
        name: u['name'] as String?,
        username: u['username'] as String?,
        avatarUrl: u['avatarUrl'] as String?,
        requestId: requestId,
      );

  static FriendItem fromJson(Map<String, dynamic> j) {
    final inner = (j['friend'] ?? j['user'] ?? j['sender']) as Map<String, dynamic>?;
    if (inner != null) {
      return FriendItem.fromUser(inner, requestId: j['id']?.toString());
    }
    return FriendItem.fromUser(j);
  }
}

class Preferences {
  const Preferences({
    this.notificationsEnabled = true,
    this.emailNotifications = true,
    this.loginAlerts = true,
    this.raw = const {},
  });

  final bool notificationsEnabled;
  final bool emailNotifications;
  final bool loginAlerts;
  final Map<String, dynamic> raw;

  factory Preferences.fromJson(Map<String, dynamic> j) {
    bool b(String k, [bool def = true]) => j[k] is bool ? j[k] as bool : def;
    return Preferences(
      notificationsEnabled: b('notificationsEnabled'),
      emailNotifications: b('emailNotifications'),
      loginAlerts: b('loginAlerts'),
      raw: j,
    );
  }

  Map<String, bool> toggles() => {
        'notificationsEnabled': notificationsEnabled,
        'emailNotifications': emailNotifications,
        'loginAlerts': loginAlerts,
      };
}

class SeasonItem {
  const SeasonItem({
    required this.id,
    required this.slug,
    required this.title,
    this.description,
    this.coverImage,
    this.episodesCount = 0,
    this.episodes = const [],
  });

  final String id;
  final String slug;
  final String title;
  final String? description;
  final String? coverImage;
  final int episodesCount;
  final List<EpisodeItem> episodes;

  factory SeasonItem.fromJson(Map<String, dynamic> j) {
    final t = j['translations'];
    final count = j['_count'] as Map<String, dynamic>?;
    final rawEpisodes = j['episodes'];
    return SeasonItem(
      id: (j['id'] ?? '').toString(),
      slug: (j['slug'] ?? '').toString(),
      title: pickTranslation(t, 'title').isNotEmpty
          ? pickTranslation(t, 'title')
          : pickTranslation(t, 'title'),
      description: pickTranslation(t, 'description'),
      coverImage: j['coverImage'] as String?,
      episodesCount: _int(count?['episodes']) ?? 0,
      episodes: rawEpisodes is List
          ? rawEpisodes
              .whereType<Map<String, dynamic>>()
              .map(EpisodeItem.fromJson)
              .toList()
          : const [],
    );
  }
}

class PlaylistSummary {
  const PlaylistSummary({
    required this.id,
    required this.slug,
    required this.title,
    this.description,
    this.coverImage,
    this.kind = 'user',
    this.ownerName,
    this.itemsCount = 0,
  });

  final String id;
  final String slug;
  final String title;
  final String? description;
  final String? coverImage;
  final String kind;
  final String? ownerName;
  final int itemsCount;

  factory PlaylistSummary.fromJson(Map<String, dynamic> j) {
    final t = j['translations'];
    final count = j['_count'] as Map<String, dynamic>?;
    final owner = j['owner'] as Map<String, dynamic>?;
    return PlaylistSummary(
      id: (j['id'] ?? '').toString(),
      slug: (j['slug'] ?? '').toString(),
      title: pickTranslation(t, 'title').isNotEmpty
          ? pickTranslation(t, 'title')
          : pickTranslation(t, 'title'),
      description: pickTranslation(t, 'description'),
      coverImage: j['coverImage'] as String?,
      kind: (j['kind'] ?? 'user').toString(),
      ownerName: (owner?['name'] ?? owner?['username'])?.toString(),
      itemsCount: _int(count?['items']) ?? 0,
    );
  }

  static PlaylistSummary fromDetailJson(Map<String, dynamic> j) {
    final items = j['items'];
    final base = PlaylistSummary.fromJson(j);
    if (items is! List) return base;
    return PlaylistSummary(
      id: base.id,
      slug: base.slug,
      title: base.title,
      description: base.description,
      coverImage: base.coverImage,
      kind: base.kind,
      ownerName: base.ownerName,
      itemsCount: items.length,
    );
  }
}

class CommentAuthor {
  const CommentAuthor({
    required this.id,
    this.name,
    this.username,
    this.avatarUrl,
  });

  final String id;
  final String? name;
  final String? username;
  final String? avatarUrl;

  String get displayName =>
      (name != null && name!.trim().isNotEmpty) ? name! : '@${username ?? id}';

  factory CommentAuthor.fromJson(Map<String, dynamic>? j) => CommentAuthor(
        id: (j?['id'] ?? '').toString(),
        name: j?['name'] as String?,
        username: j?['username'] as String?,
        avatarUrl: j?['avatarUrl'] as String?,
      );
}

class CommentItem {
  const CommentItem({
    required this.id,
    required this.body,
    required this.author,
    this.createdAt,
    this.likesCount = 0,
    this.likedByMe = false,
    this.repliesCount = 0,
    this.parentId,
  });

  final String id;
  final String body;
  final CommentAuthor author;
  final DateTime? createdAt;
  final int likesCount;
  final bool likedByMe;
  final int repliesCount;
  final String? parentId;

  factory CommentItem.fromJson(Map<String, dynamic> j) {
    final count = j['_count'] as Map<String, dynamic>?;
    return CommentItem(
      id: (j['id'] ?? '').toString(),
      body: (j['body'] ?? '').toString(),
      author: CommentAuthor.fromJson(j['user'] as Map<String, dynamic>?),
      createdAt: _date(j['createdAt']),
      likesCount: _int(j['likesCount']) ?? 0,
      likedByMe: j['likedByMe'] == true,
      repliesCount: _int(count?['replies']) ?? 0,
      parentId: j['parentId'] as String?,
    );
  }
}

class ChatMessage {
  const ChatMessage({
    required this.id,
    required this.senderId,
    required this.sender,
    required this.body,
    this.type = 'text',
    this.attachmentUrl,
    this.attachmentMime,
    this.attachmentName,
    this.attachmentSize,
    this.durationSec,
    this.createdAt,
    this.readAt,
  });

  final String id;
  final String senderId;
  final CommentAuthor sender;
  final String body;
  final String type;
  final String? attachmentUrl;
  final String? attachmentMime;
  final String? attachmentName;
  final int? attachmentSize;
  final int? durationSec;
  final DateTime? createdAt;
  final DateTime? readAt;

  factory ChatMessage.fromJson(Map<String, dynamic> j) => ChatMessage(
        id: (j['id'] ?? '').toString(),
        senderId: ((j['sender'] as Map<String, dynamic>?)?['id'] ?? '')
            .toString(),
        sender:
            CommentAuthor.fromJson(j['sender'] as Map<String, dynamic>?),
        body: (j['body'] ?? '').toString(),
        type: (j['type'] ?? 'text').toString(),
        attachmentUrl: j['attachmentUrl']?.toString(),
        attachmentMime: j['attachmentMime']?.toString(),
        attachmentName: j['attachmentName']?.toString(),
        attachmentSize: (j['attachmentSize'] is num)
            ? (j['attachmentSize'] as num).toInt()
            : null,
        durationSec: (j['durationSec'] is num)
            ? (j['durationSec'] as num).toInt()
            : null,
        createdAt: _date(j['createdAt']),
        readAt: _date(j['readAt']),
      );
}

class ConversationItem {
  const ConversationItem({
    required this.id,
    required this.kind,
    required this.title,
    this.avatarUrl,
    this.lastMessageBody,
    this.lastMessageAt,
    this.unreadCount = 0,
  });

  final String id;
  final String kind;
  final String title;
  final String? avatarUrl;
  final String? lastMessageBody;
  final DateTime? lastMessageAt;
  final int unreadCount;

  factory ConversationItem.fromListJson(Map<String, dynamic> j) {
    final last = j['lastMessage'] as Map<String, dynamic>?;
    final other = j['other'] as Map<String, dynamic>?;
    final group = j['group'] as Map<String, dynamic>?;
    final title = group?['name']?.toString();
    return ConversationItem(
      id: (j['id'] ?? '').toString(),
      kind: (j['kind'] ?? 'direct').toString(),
      title: (title != null && title.isNotEmpty)
          ? title
          : (other?['name']?.toString().isNotEmpty == true
              ? other!['name'].toString()
              : '@${other?['username'] ?? ''}'),
      avatarUrl: (group?['avatarUrl'] ?? other?['avatarUrl']) as String?,
      lastMessageBody: last?['body'] as String?,
      lastMessageAt: _date(j['updatedAt']),
      unreadCount: _int(j['unreadCount']) ?? 0,
    );
  }
}

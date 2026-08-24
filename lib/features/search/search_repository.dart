import '../../core/i18n/app_i18n.dart';
import '../../../core/api/api_client.dart';

class SearchRow {
  const SearchRow({
    required this.type,
    required this.id,
    required this.title,
    this.description,
    this.coverImage,
  });

  final String type;
  final String id;
  final String title;
  final String? description;
  final String? coverImage;

  factory SearchRow.fromJson(Map<String, dynamic> j) => SearchRow(
        type: (j['type'] ?? 'episode').toString(),
        id: (j['id'] ?? '').toString(),
        title: (j['title'] ?? '').toString(),
        description: j['description']?.toString(),
        coverImage: j['coverImage']?.toString(),
      );
}

class SearchRepository {
  SearchRepository(this._client);

  final ApiClient _client;

  Future<List<SearchRow>> search(String query, {String? locale}) async {
    if (query.trim().isEmpty) return const [];
    final data = await _client.getJson(
      '/search',
      query: {'q': query.trim(), 'locale': locale ?? i18nRuntime.name, 'limit': 30},
      authenticated: false,
    );
    final rows = data['results'];
    if (rows is! List) return const [];
    return rows
        .whereType<Map<String, dynamic>>()
        .map(SearchRow.fromJson)
        .toList();
  }
}

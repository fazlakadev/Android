import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../articles/article_detail_screen.dart';
import '../../core/widgets/skeleton.dart';
import '../../core/widgets/transitions.dart';
import '../content/providers.dart';
import '../episodes/episode_detail_screen.dart';
import '../playlists/playlist_detail_screen.dart';
import '../search/search_repository.dart';
import '../seasons/season_detail_screen.dart';

class SearchScreen extends ConsumerStatefulWidget {
  const SearchScreen({super.key});

  @override
  ConsumerState<SearchScreen> createState() => _SearchScreenState();
}

class _SearchScreenState extends ConsumerState<SearchScreen> {
  final _controller = TextEditingController();
  final _focus = FocusNode();
  Timer? _debounce;
  List<SearchRow>? _results;
  bool _busy = false;

  static const _typeLabels = {
    'episode': 'حلقات',
    'article': 'مقالات',
    'season': 'مواسم',
    'playlist': 'قوائم',
  };

  @override
  void initState() {
    super.initState();
    _focus.requestFocus();
  }

  @override
  void dispose() {
    _debounce?.cancel();
    _controller.dispose();
    _focus.dispose();
    super.dispose();
  }

  void _onChanged(String q) {
    _debounce?.cancel();
    if (q.trim().isEmpty) {
      setState(() => _results = null);
      return;
    }
    _debounce = Timer(const Duration(milliseconds: 450), () => _run(q));
  }

  Future<void> _run(String q) async {
    setState(() => _busy = true);
    try {
      final rows =
          await ref.read(searchRepositoryProvider).search(q);
      if (!mounted) return;
      setState(() => _results = rows);
    } catch (_) {
      if (!mounted) return;
      setState(() => _results = const []);
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  void _open(SearchRow row) {
    final repo = ref.read(contentRepositoryProvider);
    switch (row.type) {
      case 'season':
        Navigator.of(context).push(MaterialPageRoute(
          builder: (_) => FutureBuilder(
            future: repo.seasonDetail(row.id),
            builder: (context, snap) {
              if (!snap.hasData) {
                return const Scaffold(
                    body: Center(child: CircularProgressIndicator()));
              }
              return SeasonDetailScreen(season: snap.data!);
            },
          ),
        ));
      case 'episode':
        Navigator.of(context).push(FadeSlideRoute(
          EpisodeDetailScreen(episodeId: row.id),
        ));
      case 'article':
        Navigator.of(context).push(FadeSlideRoute(
          ArticleDetailScreen(slugOrId: row.id),
        ));
      default:
        Navigator.of(context).push(MaterialPageRoute(
          builder: (_) => FutureBuilder(
            future: repo.playlistDetail(row.id),
            builder: (context, snap) {
              if (!snap.hasData) {
                return const Scaffold(
                    body: Center(child: CircularProgressIndicator()));
              }
              return PlaylistDetailScreen(playlist: snap.data!.$1);
            },
          ),
        ));
    }
  }

  IconData _icon(String type) => switch (type) {
        'episode' => Icons.podcasts_rounded,
        'article' => Icons.article_rounded,
        'season' => Icons.video_library_rounded,
        _ => Icons.playlist_play_rounded,
      };

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final grouped = <String, List<SearchRow>>{};
    for (final r in _results ?? const <SearchRow>[]) {
      (grouped[r.type] ??= []).add(r);
    }

    return Scaffold(
      appBar: AppBar(
        title: TextField(
          controller: _controller,
          focusNode: _focus,
          onChanged: _onChanged,
          textInputAction: TextInputAction.search,
          onSubmitted: (_) => _run(_controller.text),
          decoration: const InputDecoration(
            hintText: 'ابحث عن حلقة، مقال، موسم…',
            border: InputBorder.none,
          ),
        ),
        actions: [
          if (_controller.text.isNotEmpty)
            IconButton(
              icon: const Icon(Icons.close_rounded),
              onPressed: () {
                _controller.clear();
                setState(() => _results = null);
              },
            ),
        ],
      ),
      body: _busy && _results == null
          ? const SearchSkeleton()
          : _results == null
              ? Center(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(Icons.search_rounded,
                          size: 56, color: theme.colorScheme.outlineVariant),
                      const SizedBox(height: 10),
                      Text('اكتب للبحث في كل المحتوى',
                          style: theme.textTheme.bodyMedium
                              ?.copyWith(color: theme.colorScheme.outline)),
                    ],
                  ),
                )
              : _results!.isEmpty
                  ? Center(
                      child: Text('لا توجد نتائج',
                          style: TextStyle(color: theme.colorScheme.outline)))
                  : ListView(
                      padding: const EdgeInsets.only(bottom: 24),
                      children: [
                        for (final entry in grouped.entries) ...[
                          Padding(
                            padding: const EdgeInsets.fromLTRB(16, 16, 16, 6),
                            child: Text(_typeLabels[entry.key] ?? entry.key,
                                style: theme.textTheme.titleSmall?.copyWith(
                                    color: theme.colorScheme.primary,
                                    fontWeight: FontWeight.w800)),
                          ),
                          for (final row in entry.value)
                            ListTile(
                              leading: Icon(_icon(row.type)),
                              title: Text(row.title,
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis),
                              subtitle: row.description == null ||
                                      row.description!.isEmpty
                                  ? null
                                  : Text(row.description!,
                                      maxLines: 1,
                                      overflow: TextOverflow.ellipsis),
                              trailing:
                                  const Icon(Icons.chevron_right_rounded),
                              onTap: () => _open(row),
                            ),
                        ],
                      ],
                    ),
    );
  }
}

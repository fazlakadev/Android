import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../content/models.dart';
import '../content/providers.dart';
import '../episodes/episode_detail_screen.dart';

class SeasonDetailScreen extends ConsumerStatefulWidget {
  const SeasonDetailScreen({super.key, required this.season});

  final SeasonItem season;

  @override
  ConsumerState<SeasonDetailScreen> createState() =>
      _SeasonDetailScreenState();
}

class _SeasonDetailScreenState extends ConsumerState<SeasonDetailScreen> {
  late Future<SeasonItem> _future;

  @override
  void initState() {
    super.initState();
    _future = ref
        .read(contentRepositoryProvider)
        .seasonDetail(widget.season.id)
        .catchError((_) => widget.season);
  }

  Future<void> _refresh() async {
    setState(() {
      _future = ref
          .read(contentRepositoryProvider)
          .seasonDetail(widget.season.id)
          .catchError((_) => widget.season);
    });
    await _future;
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      body: CustomScrollView(
        slivers: [
          SliverAppBar(
            expandedHeight: 220,
            pinned: true,
            flexibleSpace: FlexibleSpaceBar(
              title: Text(
                widget.season.title,
                style: const TextStyle(
                  fontWeight: FontWeight.w700,
                  shadows: [Shadow(blurRadius: 8, color: Colors.black54)],
                ),
              ),
              background: _cover(theme),
            ),
          ),
          SliverToBoxAdapter(
            child: RefreshIndicator(
              onRefresh: _refresh,
              child: FutureBuilder<SeasonItem>(
                future: _future,
                builder: (context, snap) {
                  final season =
                      (snap.hasData ? snap.data : null) ?? widget.season;
                  return Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      if (season.description != null &&
                          season.description!.isNotEmpty)
                        Padding(
                          padding: const EdgeInsets.fromLTRB(16, 16, 16, 4),
                          child: Text(
                            season.description!,
                            style: theme.textTheme.bodyMedium?.copyWith(
                              color: theme.colorScheme.onSurfaceVariant,
                            ),
                          ),
                        ),
                      Padding(
                        padding: const EdgeInsets.fromLTRB(16, 12, 16, 8),
                        child: Text(
                          '${season.episodes.length} Episodes',
                          style: theme.textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.w800,
                          ),
                        ),
                      ),
                      if (snap.connectionState == ConnectionState.waiting &&
                          season.episodes.isEmpty)
                        const Padding(
                          padding: EdgeInsets.all(24),
                          child: Center(child: CircularProgressIndicator()),
                        )
                      else if (season.episodes.isEmpty)
                        Padding(
                          padding: const EdgeInsets.all(24),
                          child: Center(
                            child: Text(
                              'No episodes in this season yet',
                              style: TextStyle(
                                  color: theme.colorScheme.outline),
                            ),
                          ),
                        )
                      else
                        ...season.episodes.map(
                          (e) => ListTile(
                            leading: ClipRRect(
                              borderRadius: BorderRadius.circular(10),
                              child: _thumb(theme, e),
                            ),
                            title: Text(
                              e.title,
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                              style:
                                  const TextStyle(fontWeight: FontWeight.w700),
                            ),
                            subtitle: Text(
                              [
                                if (e.episodeNumber != null)
                                  'EP ${e.episodeNumber}',
                                e.durationLabel,
                                if (e.category != null) e.category!,
                              ].where((s) => s.isNotEmpty).join(' • '),
                            ),
                            trailing: const Icon(Icons.play_circle_outline_rounded),
                            onTap: () => Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) =>
                                    EpisodeDetailScreen(episodeId: e.id),
                              ),
                            ),
                          ),
                        ),
                      const SizedBox(height: 24),
                    ],
                  );
                },
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _cover(ThemeData theme) {
    if (widget.season.coverImage != null) {
      return Hero(
        tag: 'season-cover-${widget.season.id}',
        child: Image.network(
          widget.season.coverImage!,
          fit: BoxFit.cover,
          errorBuilder: (_, _, _) => _fallback(theme),
        ),
      );
    }
    return _fallback(theme);
  }

  Widget _fallback(ThemeData theme) => Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [
              theme.colorScheme.primary,
              theme.colorScheme.tertiary,
            ],
          ),
        ),
        child: Center(
          child: Icon(Icons.video_library_rounded,
              size: 64, color: Colors.white.withValues(alpha: .85)),
        ),
      );

  Widget _thumb(ThemeData theme, EpisodeItem e) => SizedBox(
        width: 64,
        height: 44,
        child: e.coverImage != null
            ? Image.network(
                e.coverImage!,
                fit: BoxFit.cover,
                errorBuilder: (_, _, _) => Container(
                  color: theme.colorScheme.surfaceContainerHighest,
                  child: Icon(Icons.play_circle_outline_rounded,
                      size: 20, color: theme.colorScheme.outline),
                ),
              )
            : Container(
                color: theme.colorScheme.surfaceContainerHighest,
                child: Icon(Icons.play_circle_outline_rounded,
                    size: 20, color: theme.colorScheme.outline),
              ),
      );
}

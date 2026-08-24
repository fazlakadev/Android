import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/widgets/skeleton.dart';
import '../../core/widgets/transitions.dart';
import '../content/models.dart';
import '../content/providers.dart';
import '../episodes/episode_detail_screen.dart';
import 'season_detail_screen.dart';

class SeasonsTab extends ConsumerStatefulWidget {
  const SeasonsTab({super.key});

  @override
  ConsumerState<SeasonsTab> createState() => _SeasonsTabState();
}

class _SeasonsTabState extends ConsumerState<SeasonsTab>
    with AutomaticKeepAliveClientMixin {
  final _scroll = ScrollController();
  final _items = <SeasonItem>[];
  int _page = 0;
  bool _loading = false;
  bool _done = false;
  Object? _error;

  @override
  bool get wantKeepAlive => true;

  @override
  void initState() {
    super.initState();
    _loadMore();
    _scroll.addListener(() {
      if (_scroll.position.pixels >
          _scroll.position.maxScrollExtent - 400) {
        _loadMore();
      }
    });
  }

  @override
  void dispose() {
    _scroll.dispose();
    super.dispose();
  }

  Future<void> _loadMore() async {
    if (_loading || _done) return;
    setState(() => _loading = true);
    try {
      final (next, hasNext) = await ref
          .read(contentRepositoryProvider)
          .seasons(page: _page + 1, limit: 20);
      if (!mounted) return;
      setState(() {
        _items.addAll(next);
        _page += 1;
        _done = !hasNext;
        _error = null;
      });
    } catch (e) {
      if (mounted) setState(() => _error = e);
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    super.build(context);
    final theme = Theme.of(context);
    if (_items.isEmpty && _error != null && !_loading) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.cloud_off_rounded,
                size: 52, color: theme.colorScheme.outline),
            const SizedBox(height: 12),
            FilledButton.icon(
              onPressed: () {
                setState(() => _error = null);
                _loadMore();
              },
              icon: const Icon(Icons.refresh_rounded),
              label: const Text('Retry'),
            ),
          ],
        ),
      );
    }
    if (_items.isEmpty && !_done && !_loading) {
      return const SeasonGridSkeleton();
    }
    return RefreshIndicator(
      onRefresh: () async {
        _items.clear();
        _page = 0;
        _done = false;
        await _loadMore();
      },
      child: GridView.builder(
        controller: _scroll,
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.fromLTRB(16, 12, 16, 24),
        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
          crossAxisCount: 2,
          mainAxisSpacing: 12,
          crossAxisSpacing: 12,
          childAspectRatio: .78,
        ),
        itemCount: _items.length + (_done ? 0 : 2),
        itemBuilder: (context, i) {
          if (i >= _items.length) {
            return Container(
              decoration: BoxDecoration(
                color: theme.colorScheme.surfaceContainerHighest
                    .withValues(alpha: .4),
                borderRadius: BorderRadius.circular(18),
              ),
            );
          }
          return SeasonCard(season: _items[i]);
        },
      ),
    );
  }
}

class SeasonCard extends StatelessWidget {
  const SeasonCard({super.key, required this.season});

  final SeasonItem season;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Material(
      color: Colors.transparent,
      child: InkWell(
        borderRadius: BorderRadius.circular(18),
        onTap: () => Navigator.of(context).push(
          FadeSlideRoute(SeasonDetailScreen(season: season)),
        ),
        child: Container(
          clipBehavior: Clip.antiAlias,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(18),
            border: Border.all(
              color: theme.colorScheme.outlineVariant.withValues(alpha: .4),
            ),
          ),
          child: Stack(
            fit: StackFit.expand,
            children: [
              if (season.coverImage != null)
                Hero(
                  tag: 'season-cover-${season.id}',
                  child: CachedNetworkImage(
                    imageUrl: season.coverImage!,
                    fit: BoxFit.cover,
                    placeholder: (_, _) =>
                        const ColoredBox(color: Color(0xFFEDE9F7)),
                    errorWidget: (_, _, _) =>
                        const ColoredBox(color: Color(0xFFEDE9F7)),
                  ),
                )
              else
                ColoredBox(
                  color: theme.colorScheme.surfaceContainerHighest,
                  child: Icon(Icons.video_library_rounded,
                      size: 44, color: theme.colorScheme.outline),
                ),
              Positioned(
                top: 8,
                right: 8,
                child: Container(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                  decoration: BoxDecoration(
                    color: Colors.black87,
                    borderRadius: BorderRadius.circular(100),
                  ),
                  child: Text(
                    '${season.episodesCount > 0 ? season.episodesCount : season.episodes.length} EP',
                    style: const TextStyle(color: Colors.white, fontSize: 11),
                  ),
                ),
              ),
              Align(
                alignment: Alignment.bottomCenter,
                child: Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(10),
                  decoration: const BoxDecoration(
                    gradient: LinearGradient(
                      begin: Alignment.bottomCenter,
                      end: Alignment.topCenter,
                      colors: [Colors.black87, Colors.transparent],
                    ),
                  ),
                  child: Text(
                    season.title,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.w700,
                      fontSize: 14,
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

void openEpisodeDetail(BuildContext context, EpisodeItem episode) {
  Navigator.of(context).push(
    MaterialPageRoute(builder: (_) => EpisodeDetailScreen(episodeId: episode.id)),
  );
}

import '../../core/i18n/app_i18n.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/widgets/skeleton.dart';
import '../../core/widgets/transitions.dart';
import '../articles/articles_tab.dart' show ArticleListTile;
import '../content/providers.dart';
import '../content/models.dart';
import '../episodes/episode_detail_screen.dart';
import '../seasons/season_detail_screen.dart';
import '../seasons/seasons_tab.dart' show SeasonCard;
import 'widgets/section_header.dart';

class HomeTab extends ConsumerStatefulWidget {
  const HomeTab({super.key, this.onGoToTab});

  /// Lets "View all" buttons jump to the matching tab.
  final ValueChanged<int>? onGoToTab;

  @override
  ConsumerState<HomeTab> createState() => _HomeTabState();
}

class _HomeTabState extends ConsumerState<HomeTab> {
  late Future<_HomeData> _future;

  @override
  void initState() {
    super.initState();
    _future = _load();
  }

  Future<_HomeData> _load() async {
    final repo = ref.read(contentRepositoryProvider);
    final results = await Future.wait([
      repo.banners().catchError((_) => <BannerItem>[]),
      repo
          .seasons(limit: 10)
          .then((r) => r.$1)
          .catchError((_) => <SeasonItem>[]),
      repo
          .episodes(limit: 10)
          .then((r) => r.$1)
          .catchError((_) => <EpisodeItem>[]),
      repo
          .articles(limit: 5)
          .then((r) => r.$1)
          .catchError((_) => <ArticleItem>[]),
    ]);
    return _HomeData(
      banners: results[0] as List<BannerItem>,
      seasons: results[1] as List<SeasonItem>,
      episodes: results[2] as List<EpisodeItem>,
      articles: results[3] as List<ArticleItem>,
    );
  }

  Future<void> _refresh() async {
    setState(() => _future = _load());
    await _future;
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return RefreshIndicator(
      onRefresh: _refresh,
      child: FutureBuilder<_HomeData>(
        future: _future,
        builder: (context, snap) {
          final s = ref.watch(sProvider);
          if (snap.connectionState == ConnectionState.waiting) {
            return const HomeSkeleton();
          }
          if (snap.hasError) {
            return ListView(
              physics: const AlwaysScrollableScrollPhysics(),
              children: [
                const SizedBox(height: 80),
                Icon(Icons.cloud_off_rounded,
                    size: 56, color: theme.colorScheme.outline),
                const SizedBox(height: 12),
                Center(
                  child: Text(
                    'Could not load content',
                    style: theme.textTheme.titleMedium,
                  ),
                ),
                const SizedBox(height: 16),
                Center(
                  child: FilledButton.icon(
                    onPressed: _refresh,
                    icon: const Icon(Icons.refresh_rounded),
                    label: const Text('Retry'),
                  ),
                ),
              ],
            );
          }
          final data = snap.data!;
          return ListView(
            physics: const AlwaysScrollableScrollPhysics(),
            padding: const EdgeInsets.only(bottom: 24),
            children: [
              if (data.banners.isNotEmpty)
                _BannerCarousel(banners: data.banners),

              SectionHeader(
                title: s.seasons,
                viewAllLabel: s.viewAll,
                onMore: data.seasons.isEmpty
                    ? null
                    : () => widget.onGoToTab?.call(1),
              ),
              if (data.seasons.isEmpty)
                _EmptyHint(text: s.noSeasonsYet)
              else
                SizedBox(
                  height: 190,
                  child: ListView.separated(
                    scrollDirection: Axis.horizontal,
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    itemCount: data.seasons.length,
                    separatorBuilder: (_, _) => const SizedBox(width: 12),
                    itemBuilder: (_, i) {
                      final s = data.seasons[i];
                      return GestureDetector(
                        onTap: () => Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) =>
                                SeasonDetailScreen(season: s),
                          ),
                        ),
                        child: SizedBox(
                          width: 250,
                          child: SeasonCard(season: s),
                        ),
                      );
                    },
                  ),
                ),

              SectionHeader(
                title: s.latestEpisodes,
                viewAllLabel: s.viewAll,
                onMore: data.episodes.isEmpty
                    ? null
                    : () => widget.onGoToTab?.call(2),
              ),
              if (data.episodes.isEmpty)
                _EmptyHint(text: s.nothingFound)
              else
                SizedBox(
                  height: 210,
                  child: ListView.separated(
                    scrollDirection: Axis.horizontal,
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    itemCount: data.episodes.length,
                    separatorBuilder: (_, _) => const SizedBox(width: 12),
                    itemBuilder: (_, i) =>
                        _EpisodeCard(episode: data.episodes[i]),
                  ),
                ),

              SectionHeader(
                title: s.latestArticles,
                viewAllLabel: s.viewAll,
                onMore: data.articles.isEmpty
                    ? null
                    : () => widget.onGoToTab?.call(3),
              ),
              for (final a in data.articles)
                Padding(
                  padding:
                      const EdgeInsets.fromLTRB(16, 0, 16, 12),
                  child: ArticleListTile(article: a),
                ),
              if (data.articles.isEmpty) _EmptyHint(text: s.noArticlesYet),
            ],
          );
        },
      ),
    );
  }
}

class _HomeData {
  const _HomeData({
    required this.banners,
    required this.seasons,
    required this.episodes,
    required this.articles,
  });

  final List<BannerItem> banners;
  final List<SeasonItem> seasons;
  final List<EpisodeItem> episodes;
  final List<ArticleItem> articles;
}

class _BannerCarousel extends StatefulWidget {
  const _BannerCarousel({required this.banners});

  final List<BannerItem> banners;

  @override
  State<_BannerCarousel> createState() => _BannerCarouselState();
}

class _BannerCarouselState extends State<_BannerCarousel> {
  final _controller = PageController(viewportFraction: .92);
  int _page = 0;

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        SizedBox(
          height: 170,
          child: PageView.builder(
            controller: _controller,
            itemCount: widget.banners.length,
            onPageChanged: (i) => setState(() => _page = i),
            itemBuilder: (_, i) {
              final b = widget.banners[i];
              return Padding(
                padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 8),
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(18),
                  child: CachedNetworkImage(
                    imageUrl: b.imageUrl,
                    fit: BoxFit.cover,
                    placeholder: (_, _) => const _ShimmerBox(height: 154),
                    errorWidget: (_, _, _) =>
                        Container(color: Colors.grey.shade300),
                  ),
                ),
              );
            },
          ),
        ),
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            for (var i = 0; i < widget.banners.length; i++)
              AnimatedContainer(
                duration: const Duration(milliseconds: 250),
                margin: const EdgeInsets.symmetric(horizontal: 3),
                width: i == _page ? 18 : 6,
                height: 6,
                decoration: BoxDecoration(
                  color: i == _page
                      ? Theme.of(context).colorScheme.primary
                      : Theme.of(context).colorScheme.outlineVariant,
                  borderRadius: BorderRadius.circular(100),
                ),
              ),
          ],
        ),
      ],
    );
  }
}

class _EpisodeCard extends StatelessWidget {
  const _EpisodeCard({required this.episode});

  final EpisodeItem episode;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return GestureDetector(
      onTap: () => Navigator.of(context).push(
        FadeSlideRoute(
          EpisodeDetailScreen(episodeId: episode.id, seed: episode),
        ),
      ),
      child: Container(
      width: 160,
      clipBehavior: Clip.antiAlias,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: theme.colorScheme.outlineVariant.withValues(alpha: .4)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(
            child: Stack(
              fit: StackFit.expand,
              children: [
                if (episode.coverImage != null)
                  CachedNetworkImage(
                    imageUrl: episode.coverImage!,
                    fit: BoxFit.cover,
                    placeholder: (_, _) => const ColoredBox(
                        color: Color(0xFFEDE9F7)),
                    errorWidget: (_, _, _) =>
                        const ColoredBox(color: Color(0xFFEDE9F7)),
                  )
                else
                  Container(
                    color: theme.colorScheme.surfaceContainerHighest,
                    child: Icon(Icons.play_circle_outline_rounded,
                        size: 40, color: theme.colorScheme.outline),
                  ),
                Positioned(
                  bottom: 6,
                  right: 6,
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 7, vertical: 3),
                    decoration: BoxDecoration(
                      color: Colors.black87,
                      borderRadius: BorderRadius.circular(100),
                    ),
                    child: Text(
                      episode.durationLabel.isNotEmpty
                          ? episode.durationLabel
                          : '#${episode.episodeNumber ?? '?'}',
                      style: const TextStyle(color: Colors.white, fontSize: 11),
                    ),
                  ),
                ),
              ],
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(10),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  episode.title,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(fontWeight: FontWeight.w700),
                ),
                if (episode.category != null)
                  Text(
                    episode.category!,
                    style: TextStyle(
                        fontSize: 12, color: theme.colorScheme.primary),
                  ),
              ],
            ),
          ),
        ],
      ),
      ),
    );
  }
}

class _EmptyHint extends StatelessWidget {
  const _EmptyHint({required this.text});

  final String text;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(20),
      child: Center(
        child: Text(
          text,
          style: Theme.of(context)
              .textTheme
              .bodyMedium
              ?.copyWith(color: Theme.of(context).colorScheme.outline),
        ),
      ),
    );
  }
}

class _ShimmerBox extends StatelessWidget {
  const _ShimmerBox({required this.height});

  final double height;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: height,
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surfaceContainerHighest
            .withValues(alpha: .5),
        borderRadius: BorderRadius.circular(18),
      ),
    );
  }
}

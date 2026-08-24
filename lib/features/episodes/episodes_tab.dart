import '../../core/i18n/app_i18n.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'dart:async';

import '../../core/api/api_client.dart';
import '../../core/widgets/skeleton.dart';
import '../content/providers.dart';
import '../content/models.dart';
import '../../core/widgets/transitions.dart';
import 'episode_detail_screen.dart';

class EpisodesTab extends ConsumerStatefulWidget {
  const EpisodesTab({super.key});

  @override
  ConsumerState<EpisodesTab> createState() => _EpisodesTabState();
}

class _EpisodesTabState extends ConsumerState<EpisodesTab>
    with AutomaticKeepAliveClientMixin {
  final _scroll = ScrollController();
  final _searchCtrl = TextEditingController();
  final _items = <EpisodeItem>[];
  Timer? _debounce;
  int _page = 1;
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
      if (_scroll.position.pixels > _scroll.position.maxScrollExtent - 500) {
        _loadMore();
      }
    });
  }

  @override
  void dispose() {
    _scroll.dispose();
    _searchCtrl.dispose();
    _debounce?.cancel();
    super.dispose();
  }

  void _onSearchChanged(String value) {
    _debounce?.cancel();
    _debounce = Timer(const Duration(milliseconds: 450), () => _refresh());
  }

  Future<void> _loadMore() async {
    if (_loading || _done) return;
    setState(() => _loading = true);
    try {
      final (next, hasNext) = await ref
          .read(contentRepositoryProvider)
          .episodes(page: _page, search: _searchCtrl.text);
      setState(() {
        _items.addAll(next);
        _page++;
        _done = !hasNext;
        _error = null;
      });
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } catch (_) {
      setState(() => _error = 'Something went wrong');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _refresh() async {
    setState(() {
      _items.clear();
      _page = 1;
      _done = false;
    });
    await _loadMore();
  }

  @override
  Widget build(BuildContext context) {
    super.build(context);
    final theme = Theme.of(context);
    final s = ref.watch(sProvider);

    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 10, 16, 4),
          child: TextField(
            controller: _searchCtrl,
            onChanged: _onSearchChanged,
            textInputAction: TextInputAction.search,
            decoration: InputDecoration(
              hintText: s.searchHint,
              prefixIcon: const Icon(Icons.search_rounded),
              isDense: true,
              filled: true,
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(14),
                borderSide: BorderSide.none,
              ),
            ),
          ),
        ),
        Expanded(
          child: _error != null && _items.isEmpty
              ? Center(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(Icons.cloud_off_rounded,
                          size: 56, color: theme.colorScheme.outline),
                      const SizedBox(height: 12),
                      Text('$_error'),
                      const SizedBox(height: 12),
                      FilledButton.icon(
                        onPressed: _refresh,
                        icon: const Icon(Icons.refresh_rounded),
                        label: const Text('Retry'),
                      ),
                    ],
                  ),
                )
              : _items.isEmpty && !_done
                  ? const EpisodeGridSkeleton()
                  : _items.isEmpty
                      ? Center(
                          child: Column(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Icon(Icons.movie_filter_outlined,
                                  size: 46, color: theme.colorScheme.outline),
                              const SizedBox(height: 8),
                              Text(s.nothingFound,
                                  style: TextStyle(
                                      color: theme.colorScheme.outline)),
                            ],
                          ),
                        )
                      : RefreshIndicator(
                  onRefresh: _refresh,
                  child: GridView.builder(
                    controller: _scroll,
                    physics: const AlwaysScrollableScrollPhysics(),
                    padding: const EdgeInsets.fromLTRB(16, 10, 16, 24),
                    gridDelegate:
                        const SliverGridDelegateWithFixedCrossAxisCount(
                      crossAxisCount: 2,
                      mainAxisSpacing: 12,
                      crossAxisSpacing: 12,
                      childAspectRatio: .78,
                    ),
                    itemCount: _items.length + (!_done ? 1 : 0),
                    itemBuilder: (_, i) {
                      if (i < _items.length) {
                        return _EpisodeGridCard(episode: _items[i]);
                      }
                      return const Padding(
                        padding: EdgeInsets.all(18),
                        child: Center(child: CircularProgressIndicator()),
                      );
                    },
                  ),
                ),
        ),
      ],
    );
  }
}

class _EpisodeGridCard extends StatelessWidget {
  const _EpisodeGridCard({required this.episode});

  final EpisodeItem episode;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return InkWell(
      borderRadius: BorderRadius.circular(16),
      onTap: () => Navigator.of(context).push(FadeSlideRoute(
        EpisodeDetailScreen(episodeId: episode.id, seed: episode),
      )),
      child: Container(
        clipBehavior: Clip.antiAlias,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(16),
          border:
              Border.all(color: theme.colorScheme.outlineVariant.withValues(alpha: .4)),
        ),
        child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(
            child: Stack(
              fit: StackFit.expand,
              children: [
                if (episode.coverImage != null)
                  Hero(
                    tag: 'episode-cover-${episode.id}',
                    child: CachedNetworkImage(
                      imageUrl: episode.coverImage!,
                      fit: BoxFit.cover,
                      placeholder: (_, _) => ColoredBox(
                          color: theme.colorScheme.surfaceContainerHighest),
                      errorWidget: (_, _, _) => ColoredBox(
                          color: theme.colorScheme.surfaceContainerHighest),
                    ),
                  )
                else
                  Container(
                    color: theme.colorScheme.surfaceContainerHighest,
                    child: Icon(Icons.play_circle_outline_rounded,
                        size: 42, color: theme.colorScheme.outline),
                  ),
                Positioned(
                  bottom: 6,
                  right: 6,
                  child: Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 7, vertical: 3),
                    decoration: BoxDecoration(
                      color: Colors.black87,
                      borderRadius: BorderRadius.circular(100),
                    ),
                    child: Text(
                      episode.durationLabel.isNotEmpty
                          ? episode.durationLabel
                          : '#${episode.episodeNumber ?? '?'}',
                      style:
                          const TextStyle(color: Colors.white, fontSize: 11),
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
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style:
                      const TextStyle(fontWeight: FontWeight.w700, height: 1.25),
                ),
                const SizedBox(height: 4),
                if (episode.category?.isNotEmpty == true)
                  Text(episode.category!,
                      style: TextStyle(
                          fontSize: 11.5, color: theme.colorScheme.primary)),
              ],
            ),
          ),
        ],
        ),
      ),
    );
  }
}

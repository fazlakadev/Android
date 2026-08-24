import '../../core/i18n/app_i18n.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api/api_client.dart';
import '../../core/widgets/skeleton.dart';
import '../../core/widgets/transitions.dart';
import '../content/providers.dart';
import '../content/models.dart';
import 'article_detail_screen.dart';

class ArticlesTab extends ConsumerStatefulWidget {
  const ArticlesTab({super.key});

  @override
  ConsumerState<ArticlesTab> createState() => _ArticlesTabState();
}

class _ArticlesTabState extends ConsumerState<ArticlesTab>
    with AutomaticKeepAliveClientMixin {
  final _scroll = ScrollController();
  final _items = <ArticleItem>[];
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
      final (next, hasNext) =
          await ref.read(contentRepositoryProvider).articles(page: _page);
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

    if (_error != null && _items.isEmpty) {
      return Center(
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
              label: Text(s.retry),
            ),
          ],
        ),
      );
    }

    if (_items.isEmpty && !_done) {
      return const ArticleListSkeleton();
    }

    if (_items.isEmpty && _done) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.article_outlined,
                size: 52, color: theme.colorScheme.outline),
            const SizedBox(height: 10),
            Text(s.noArticlesYet,
                style: TextStyle(color: theme.colorScheme.outline)),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: _refresh,
      child: ListView.separated(
        controller: _scroll,
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.fromLTRB(16, 12, 16, 24),
        itemCount: _items.length + (!_done ? 1 : 0),
        separatorBuilder: (_, _) => const SizedBox(height: 12),
        itemBuilder: (_, i) {
          if (i < _items.length) {
            return ArticleListTile(article: _items[i]);
          }
          return const Padding(
            padding: EdgeInsets.all(14),
            child: Center(child: CircularProgressIndicator()),
          );
        },
      ),
    );
  }
}

class ArticleListTile extends StatelessWidget {
  const ArticleListTile({super.key, required this.article});

  final ArticleItem article;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      clipBehavior: Clip.antiAlias,
      margin: EdgeInsets.zero,
      child: InkWell(
        onTap: () {
          Navigator.of(context).push(
            FadeSlideRoute(
              ArticleDetailScreen(
                slugOrId:
                    article.slug.isEmpty ? article.id : article.slug,
                seed: article,
              ),
            ),
          );
        },
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(
              width: 110,
              height: 96,
              child: article.coverImage != null
                  ? Hero(
                      tag: 'article-cover-${article.id}',
                      child: CachedNetworkImage(
                        imageUrl: article.coverImage!,
                        fit: BoxFit.cover,
                        placeholder: (_, _) => ColoredBox(
                            color:
                                theme.colorScheme.surfaceContainerHighest),
                        errorWidget: (_, _, _) => ColoredBox(
                            color:
                                theme.colorScheme.surfaceContainerHighest),
                      ),
                    )
                  : Container(
                      color: theme.colorScheme.primaryContainer,
                      child: Icon(Icons.article_rounded,
                          color: theme.colorScheme.onPrimaryContainer),
                    ),
            ),
            Expanded(
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      article.title,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                          fontWeight: FontWeight.w700, height: 1.25),
                    ),
                    const SizedBox(height: 5),
                    if (article.excerpt?.isNotEmpty == true)
                      Text(
                        article.excerpt!,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                        style: TextStyle(
                          fontSize: 12.5,
                          color: theme.colorScheme.onSurfaceVariant,
                          height: 1.3,
                        ),
                      ),
                    const SizedBox(height: 6),
                    Row(
                      children: [
                        if (article.category?.isNotEmpty == true)
                          Container(
                            padding: const EdgeInsets.symmetric(
                                horizontal: 8, vertical: 2),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondaryContainer,
                              borderRadius: BorderRadius.circular(100),
                            ),
                            child: Text(
                              article.category!,
                              style: TextStyle(
                                fontSize: 11,
                                fontWeight: FontWeight.w600,
                                color: theme.colorScheme.onSecondaryContainer,
                              ),
                            ),
                          ),
                        const Spacer(),
                        if (article.viewsCount != null)
                          Row(
                            children: [
                              Icon(Icons.visibility_outlined,
                                  size: 13, color: theme.colorScheme.outline),
                              const SizedBox(width: 3),
                              Text('${article.viewsCount}',
                                  style: TextStyle(
                                      fontSize: 11,
                                      color: theme.colorScheme.outline)),
                            ],
                          ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

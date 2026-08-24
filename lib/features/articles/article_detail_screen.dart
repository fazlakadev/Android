import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_html/flutter_html.dart';

import '../content/content_repository.dart';
import '../content/models.dart';
import '../content/providers.dart';
import '../social/comments_sheet.dart';

class ArticleDetailScreen extends ConsumerStatefulWidget {
  const ArticleDetailScreen({super.key, required this.slugOrId, this.seed});

  final String slugOrId;

  /// Pre-loaded article (from list screens) for instant render + Hero.
  final ArticleItem? seed;

  @override
  ConsumerState<ArticleDetailScreen> createState() =>
      _ArticleDetailScreenState();
}

class _ArticleDetailScreenState extends ConsumerState<ArticleDetailScreen> {
  late Future<ArticleItem> _future;
  late final ContentRepository _repo = ref.read(contentRepositoryProvider);
  bool _liked = false;
  int _likes = 0;
  bool _likeBusy = false;

  @override
  void initState() {
    super.initState();
    _future = _repo.articleDetail(widget.slugOrId);
    _loadLikeState();
    _trackView();
  }

  Future<void> _trackView() async {
    ArticleItem a;
    try {
      a = await _future;
    } catch (_) {
      return;
    }
    unawaited(
      ref.read(socialRepositoryProvider).trackView('article', a.id),
    );
  }

  Future<void> _loadLikeState() async {
    final social = ref.read(socialRepositoryProvider);
    ArticleItem a;
    try {
      a = await _future;
    } catch (_) {
      return;
    }
    try {
      final status = await social.likeStatus('article', a.id);
      if (!mounted) return;
      setState(() => _liked = status.liked);
    } catch (_) {}
    try {
      final count = await social.likeCount('article', a.id);
      if (!mounted) return;
      setState(() => _likes = count);
    } catch (_) {}
  }

  Future<void> _toggleLike(String articleId) async {
    if (_likeBusy) return;
    setState(() {
      _likeBusy = true;
      _liked = !_liked;
      _likes += _liked ? 1 : -1;
    });
    try {
      await ref
          .read(socialRepositoryProvider)
          .toggleLike('article', articleId);
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(e.toString())),
        );
        setState(() {
          _liked = !_liked;
          _likes += _liked ? 1 : -1;
        });
      }
    } finally {
      if (mounted) setState(() => _likeBusy = false);
    }
  }

  Future<void> _openComments(ArticleItem a) async {
    await showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      useSafeArea: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (_) => CommentsSheet(
        contentType: 'article',
        contentId: a.id,
        title: a.title,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      body: FutureBuilder<ArticleItem>(
        future: _future,
        initialData: widget.seed,
        builder: (context, snap) {
          if (snap.connectionState == ConnectionState.waiting &&
              !snap.hasData) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snap.hasError) {
            return SafeArea(
              child: Column(
                children: [
                  const BackButtonRow(),
                  Expanded(
                    child: Center(
                      child: Text('Failed to load article',
                          style: TextStyle(color: theme.colorScheme.outline)),
                    ),
                  ),
                ],
              ),
            );
          }
          final a = snap.data!;
          return CustomScrollView(
            slivers: [
              SliverAppBar(
                expandedHeight: 230,
                pinned: true,
                flexibleSpace: FlexibleSpaceBar(
                  background: Stack(
                    fit: StackFit.expand,
                    children: [
                      if (a.coverImage != null)
                        Hero(
                          tag: 'article-cover-${a.id}',
                          child: Image.network(a.coverImage!, fit: BoxFit.cover),
                        )
                      else
                        Container(color: theme.colorScheme.primaryContainer),
                      const DecoratedBox(
                        decoration: BoxDecoration(
                          gradient: LinearGradient(
                            begin: Alignment.bottomCenter,
                            end: Alignment.topCenter,
                            colors: [Colors.black54, Colors.transparent],
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              SliverPadding(
                padding: const EdgeInsets.all(20),
                sliver: SliverList(
                  delegate: SliverChildListDelegate([
                    Text(
                      a.title,
                      style: theme.textTheme.headlineSmall
                          ?.copyWith(fontWeight: FontWeight.w800, height: 1.3),
                    ),
                    const SizedBox(height: 10),
                    Row(
                      children: [
                        if (a.category?.isNotEmpty == true) ...[
                          Chip(
                            label: Text(a.category!),
                            labelStyle:
                                const TextStyle(fontSize: 11.5),
                            visualDensity: VisualDensity.compact,
                          ),
                          const SizedBox(width: 8),
                        ],
                        if (a.publishedAt != null)
                          Text(
                            '${a.publishedAt!.day}/${a.publishedAt!.month}/${a.publishedAt!.year}',
                            style: TextStyle(
                                fontSize: 12.5,
                                color: theme.colorScheme.outline),
                          ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    Row(
                      children: [
                        Material(
                          color: Colors.transparent,
                          child: InkWell(
                            borderRadius: BorderRadius.circular(100),
                            onTap: _likeBusy
                                ? null
                                : () => _toggleLike(a.id),
                            child: Container(
                              padding: const EdgeInsets.symmetric(
                                  horizontal: 14, vertical: 8),
                              decoration: BoxDecoration(
                                borderRadius: BorderRadius.circular(100),
                                border: Border.all(
                                  color: theme.colorScheme.outlineVariant
                                      .withValues(alpha: .7),
                                ),
                              ),
                              child: Row(
                                children: [
                                  Icon(
                                    _liked
                                        ? Icons.favorite_rounded
                                        : Icons.favorite_border_rounded,
                                    size: 18,
                                    color: _liked
                                        ? theme.colorScheme.error
                                        : theme.colorScheme.primary,
                                  ),
                                  const SizedBox(width: 6),
                                  Text('$_likes',
                                      style: const TextStyle(
                                          fontWeight: FontWeight.w700)),
                                ],
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(width: 10),
                        Material(
                          color: Colors.transparent,
                          child: InkWell(
                            borderRadius: BorderRadius.circular(100),
                            onTap: () => _openComments(a),
                            child: Container(
                              padding: const EdgeInsets.symmetric(
                                  horizontal: 14, vertical: 8),
                              decoration: BoxDecoration(
                                borderRadius: BorderRadius.circular(100),
                                border: Border.all(
                                  color: theme.colorScheme.outlineVariant
                                      .withValues(alpha: .7),
                                ),
                              ),
                              child: Row(
                                children: [
                                  Icon(Icons.comment_rounded,
                                      size: 18,
                                      color: theme.colorScheme.primary),
                                  const SizedBox(width: 6),
                                  const Text('Comments',
                                      style: TextStyle(
                                          fontWeight: FontWeight.w700)),
                                ],
                              ),
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    if (a.body.isNotEmpty)
                      Html(data: a.body)
                    else
                      Text('No content available',
                          style:
                              TextStyle(color: theme.colorScheme.outline)),
                    const SizedBox(height: 40),
                  ]),
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}

class BackButtonRow extends StatelessWidget {
  const BackButtonRow({super.key});

  @override
  Widget build(BuildContext context) {
    return const Padding(
      padding: EdgeInsets.only(top: 6),
      child: Align(alignment: Alignment.centerLeft, child: BackButton()),
    );
  }
}

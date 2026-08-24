import '../../core/i18n/app_i18n.dart';
import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../content/models.dart';
import '../content/providers.dart';
import '../playlists/playlist_picker_sheet.dart';
import '../player/audio_player_service.dart';
import '../social/comments_sheet.dart';

class EpisodeDetailScreen extends ConsumerStatefulWidget {
  const EpisodeDetailScreen({
    super.key,
    required this.episodeId,
    this.seed,
  });

  final String episodeId;

  /// Optional pre-loaded episode so the UI renders instantly (used by
  /// Hero flights from list screens).
  final EpisodeItem? seed;

  @override
  ConsumerState<EpisodeDetailScreen> createState() =>
      _EpisodeDetailScreenState();
}

class _EpisodeDetailScreenState extends ConsumerState<EpisodeDetailScreen> {
  late Future<EpisodeItem> _future;
  bool _liked = false;
  int _likes = 0;
  bool _likeBusy = false;
  double _avgRating = 0;
  int _ratingCount = 0;
  int _myRating = 0;

  @override
  void initState() {
    super.initState();
    _future = ref.read(contentRepositoryProvider).episodeDetail(widget.episodeId);
    _loadLikeState();
    _loadRatings();
    // Fire-and-forget analytics.
    unawaited(
      ref.read(socialRepositoryProvider).trackView('episode', widget.episodeId),
    );
  }

  Future<void> _loadRatings() async {
    final social = ref.read(socialRepositoryProvider);
    try {
      final s = await social.ratingSummary('episode', widget.episodeId);
      if (!mounted) return;
      setState(() {
        _avgRating = s.average;
        _ratingCount = s.count;
      });
    } catch (_) {}
    try {
      final mine = await social.myRating('episode', widget.episodeId);
      if (!mounted || mine == 0) return;
      setState(() => _myRating = mine);
    } catch (_) {}
  }

  Future<void> _rate(int value) async {
    setState(() => _myRating = value);
    try {
      await ref
          .read(socialRepositoryProvider)
          .rateContent(contentType: 'episode', contentId: widget.episodeId, value: value);
      final s = await ref
          .read(socialRepositoryProvider)
          .ratingSummary('episode', widget.episodeId);
      if (!mounted) return;
      setState(() {
        _avgRating = s.average;
        _ratingCount = s.count;
      });
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text(e.toString())));
      }
    }
  }

  Future<void> _loadLikeState() async {
    final social = ref.read(socialRepositoryProvider);
    try {
      final status = await social.likeStatus('episode', widget.episodeId);
      if (!mounted) return;
      setState(() => _liked = status.liked);
    } catch (_) {}
    try {
      final count = await social.likeCount('episode', widget.episodeId);
      if (!mounted) return;
      setState(() => _likes = count);
    } catch (_) {}
  }

  Future<void> _toggleLike() async {
    if (_likeBusy) return;
    setState(() => _likeBusy = true);
    // Optimistic update.
    setState(() {
      _liked = !_liked;
      _likes += _liked ? 1 : -1;
    });
    try {
      await ref
          .read(socialRepositoryProvider)
          .toggleLike('episode', widget.episodeId);
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

  Future<void> _openComments(EpisodeItem episode) async {
    await showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      useSafeArea: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (_) => CommentsSheet(
        contentType: 'episode',
        contentId: episode.id,
        title: episode.title,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(title: const Text('Episode')),
      body: FutureBuilder<EpisodeItem>(
        future: _future,
        initialData: widget.seed,
        builder: (context, snap) {
          if (snap.connectionState == ConnectionState.waiting &&
              !snap.hasData) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snap.hasError || !snap.hasData) {
            return Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(Icons.error_outline_rounded,
                      size: 48, color: theme.colorScheme.outline),
                  const SizedBox(height: 12),
                  FilledButton.icon(
                    onPressed: () =>
                        setState(() => _future = ref
                            .read(contentRepositoryProvider)
                            .episodeDetail(widget.episodeId)),
                    icon: const Icon(Icons.refresh_rounded),
                    label: const Text('Retry'),
                  ),
                ],
              ),
            );
          }
          final e = snap.data!;
          return ListView(
            padding: const EdgeInsets.only(bottom: 32),
            children: [
              if (e.coverImage != null)
                AspectRatio(
                  aspectRatio: 16 / 9,
                  child: Hero(
                    tag: 'episode-cover-${e.id}',
                    child: Image.network(
                      e.coverImage!,
                      fit: BoxFit.cover,
                      errorBuilder: (_, _, _) => const SizedBox.shrink(),
                    ),
                  ),
                ),
              Padding(
                padding: const EdgeInsets.fromLTRB(16, 16, 16, 0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      e.title,
                      style: theme.textTheme.headlineSmall
                          ?.copyWith(fontWeight: FontWeight.w800),
                    ),
                    const SizedBox(height: 6),
                    Wrap(
                      spacing: 8,
                      runSpacing: 6,
                      children: [
                        if (e.episodeNumber != null)
                          _MetaChip(icon: Icons.tag_rounded, label: 'EP ${e.episodeNumber}'),
                        if (e.durationLabel.isNotEmpty)
                          _MetaChip(icon: Icons.schedule_rounded, label: e.durationLabel),
                        if (e.category != null)
                          _MetaChip(icon: Icons.category_rounded, label: e.category!),
                        if (e.releaseYear != null)
                          _MetaChip(icon: Icons.calendar_month_rounded, label: '${e.releaseYear}'),
                      ],
                    ),
                    if (e.excerpt != null && e.excerpt!.isNotEmpty) ...[
                      const SizedBox(height: 14),
                      Text(
                        e.excerpt!,
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                          height: 1.5,
                        ),
                      ),
                    ],
                    const SizedBox(height: 14),
                    _RatingRow(
                      myRating: _myRating,
                      average: _avgRating,
                      count: _ratingCount,
                      onRate: _rate,
                    ),
                    const SizedBox(height: 18),
                    if ((e.audioUrl ?? '').isNotEmpty)
                      Padding(
                        padding: const EdgeInsets.only(bottom: 14),
                        child: SizedBox(
                          width: double.infinity,
                          child: FilledButton.icon(
                            onPressed: () => ref
                                .read(audioPlayerProvider.notifier)
                                .open(e),
                            icon: const Icon(Icons.play_arrow_rounded),
                            label: Text(
                              ref.watch(audioPlayerProvider).episode?.id == e.id
                                  ? (ref.watch(audioPlayerProvider).playing
                                      ? 'قيد التشغيل الآن'
                                      : 'متابعة الاستماع')
                                  : 'استمع للحلقة',
                            ),
                          ),
                        ),
                      ),
                    Row(
                      children: [
                        _ActionPill(
                          icon: _liked
                              ? Icons.favorite_rounded
                              : Icons.favorite_border_rounded,
                          color:
                              _liked ? theme.colorScheme.error : null,
                          label: '$_likes',
                          onTap: _toggleLike,
                        ),
                        const SizedBox(width: 10),
                        _ActionPill(
                          icon: Icons.comment_rounded,
                          label: 'Comments',
                          onTap: () => _openComments(e),
                        ),
                        const Spacer(),
                        IconButton.filledTonal(
                          tooltip: 'Add to playlist',
                          onPressed: () => showPlaylistPicker(
                            context,
                            ref,
                            episodeId: e.id,
                          ),
                          icon: const Icon(Icons.playlist_add_rounded),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}

class _RatingRow extends ConsumerWidget {
  const _RatingRow({
    required this.myRating,
    required this.average,
    required this.count,
    required this.onRate,
  });

  final int myRating;
  final double average;
  final int count;
  final void Function(int) onRate;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final s = ref.watch(sProvider);
    return Row(
      children: [
        for (var i = 1; i <= 5; i++)
          InkWell(
            borderRadius: BorderRadius.circular(20),
            onTap: () => onRate(i),
            child: Padding(
              padding: const EdgeInsets.all(2),
              child: Icon(
                i <= myRating
                    ? Icons.star_rounded
                    : Icons.star_border_rounded,
                size: 28,
                color: theme.colorScheme.secondary,
              ),
            ),
          ),
        const SizedBox(width: 8),
        Text(
          average > 0
              ? s.ratingLabel(average, count)
              : s.rateEpisode,
          style: theme.textTheme.labelMedium
              ?.copyWith(color: theme.colorScheme.outline),
        ),
      ],
    );
  }
}

class _MetaChip extends StatelessWidget {
  const _MetaChip({required this.icon, required this.label});

  final IconData icon;
  final String label;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceContainerHighest.withValues(alpha: .55),
        borderRadius: BorderRadius.circular(100),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 14, color: theme.colorScheme.primary),
          const SizedBox(width: 4),
          Text(label,
              style: const TextStyle(fontSize: 12.5, fontWeight: FontWeight.w600)),
        ],
      ),
    );
  }
}

class _ActionPill extends StatelessWidget {
  const _ActionPill({
    required this.icon,
    required this.label,
    this.onTap,
    this.color,
  });

  final IconData icon;
  final String label;
  final VoidCallback? onTap;
  final Color? color;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Material(
      color: Colors.transparent,
      child: InkWell(
        borderRadius: BorderRadius.circular(100),
        onTap: onTap,
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(100),
            border: Border.all(
              color: theme.colorScheme.outlineVariant.withValues(alpha: .7),
            ),
          ),
          child: Row(
            children: [
              Icon(icon, size: 18, color: color ?? theme.colorScheme.primary),
              const SizedBox(width: 6),
              Text(label,
                  style: const TextStyle(fontWeight: FontWeight.w700)),
            ],
          ),
        ),
      ),
    );
  }
}

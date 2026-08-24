import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../content/models.dart';
import '../content/providers.dart';
import '../episodes/episode_detail_screen.dart';

class PlaylistDetailScreen extends ConsumerStatefulWidget {
  const PlaylistDetailScreen({super.key, required this.playlist});

  final PlaylistSummary playlist;

  @override
  ConsumerState<PlaylistDetailScreen> createState() =>
      _PlaylistDetailScreenState();
}

class _PlaylistDetailScreenState extends ConsumerState<PlaylistDetailScreen> {
  late Future<(PlaylistSummary, List<EpisodeItem>)> _future;
  String? _busyEpisodeId;

  @override
  void initState() {
    super.initState();
    _future = ref.read(contentRepositoryProvider).playlistDetail(
          widget.playlist.id,
        );
  }

  Future<void> _reload() async {
    setState(() {
      _future = ref
          .read(contentRepositoryProvider)
          .playlistDetail(widget.playlist.id);
    });
    await _future;
  }

  Future<void> _remove(EpisodeItem e) async {
    if (_busyEpisodeId != null) return;
    setState(() => _busyEpisodeId = e.id);
    try {
      await ref
          .read(contentRepositoryProvider)
          .removeFromPlaylist(widget.playlist.id, e.id);
      await _reload();
    } catch (err) {
      if (mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text(err.toString())));
      }
    } finally {
      if (mounted) setState(() => _busyEpisodeId = null);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(title: const Text('Playlist')),
      body: FutureBuilder<(PlaylistSummary, List<EpisodeItem>)>(
        future: _future,
        builder: (context, snap) {
          if (snap.connectionState == ConnectionState.waiting &&
              !snap.hasData) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snap.hasError || !snap.hasData) {
            return Center(
              child: FilledButton.icon(
                onPressed: () => setState(
                  () => _future = ref
                      .read(contentRepositoryProvider)
                      .playlistDetail(widget.playlist.id),
                ),
                icon: const Icon(Icons.refresh_rounded),
                label: const Text('Retry'),
              ),
            );
          }
          final (playlist, episodes) = snap.data!;
          return RefreshIndicator(
            onRefresh: _reload,
            child: ListView(
              physics: const AlwaysScrollableScrollPhysics(),
              padding: const EdgeInsets.only(bottom: 24),
              children: [
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(20),
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                      colors: [
                        theme.colorScheme.primaryContainer,
                        theme.colorScheme.tertiaryContainer,
                      ],
                    ),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Icon(
                            playlist.kind == 'platform'
                                ? Icons.star_rounded
                                : Icons.playlist_play_rounded,
                            color: theme.colorScheme.primary,
                            size: 28,
                          ),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Text(
                              playlist.title,
                              style: theme.textTheme.titleLarge?.copyWith(
                                fontWeight: FontWeight.w800,
                              ),
                            ),
                          ),
                        ],
                      ),
                      if (playlist.description != null &&
                          playlist.description!.isNotEmpty) ...[
                        const SizedBox(height: 6),
                        Text(playlist.description!),
                      ],
                      const SizedBox(height: 8),
                      Text(
                        '${episodes.length} episodes'
                        '${playlist.ownerName != null ? ' • by ${playlist.ownerName}' : ''}',
                        style: TextStyle(color: theme.colorScheme.outline),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 8),
                if (episodes.isEmpty)
                  Padding(
                    padding: const EdgeInsets.all(24),
                    child: Center(
                      child: Text(
                        'This playlist is empty.\nAdd episodes from any episode page.',
                        textAlign: TextAlign.center,
                        style: TextStyle(color: theme.colorScheme.outline),
                      ),
                    ),
                  )
                else
                  ...episodes.map((e) => ListTile(
                        leading: ClipRRect(
                          borderRadius: BorderRadius.circular(10),
                          child: e.coverImage != null
                              ? Image.network(
                                  e.coverImage!,
                                  width: 64,
                                  height: 44,
                                  fit: BoxFit.cover,
                                  errorBuilder: (_, _, _) => Container(
                                    width: 64,
                                    height: 44,
                                    color: theme
                                        .colorScheme.surfaceContainerHighest,
                                    child: Icon(Icons.play_circle_outline_rounded,
                                        size: 20,
                                        color: theme.colorScheme.outline),
                                  ),
                                )
                              : Container(
                                  width: 64,
                                  height: 44,
                                  color: theme
                                      .colorScheme.surfaceContainerHighest,
                                  child: Icon(Icons.play_circle_outline_rounded,
                                      size: 20, color: theme.colorScheme.outline),
                                ),
                        ),
                        title: Text(e.title,
                            maxLines: 1, overflow: TextOverflow.ellipsis),
                        subtitle: Text([
                          if (e.episodeNumber != null) 'EP ${e.episodeNumber}',
                          e.durationLabel,
                        ].where((s) => s.isNotEmpty).join(' • ')),
                        trailing: _busyEpisodeId == e.id
                            ? const SizedBox(
                                width: 18,
                                height: 18,
                                child:
                                    CircularProgressIndicator(strokeWidth: 2))
                            : IconButton(
                                tooltip: 'Remove from playlist',
                                icon: const Icon(
                                    Icons.remove_circle_outline_rounded),
                                onPressed: () => _remove(e),
                              ),
                        onTap: () => Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) => EpisodeDetailScreen(episodeId: e.id),
                          ),
                        ),
                      )),
              ],
            ),
          );
        },
      ),
    );
  }
}

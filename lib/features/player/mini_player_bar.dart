import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'audio_player_service.dart';

String _fmt(Duration d) {
  final m = d.inMinutes.remainder(60).toString().padLeft(2, '0');
  final s = d.inSeconds.remainder(60).toString().padLeft(2, '0');
  final h = d.inHours;
  return h > 0 ? '$h:$m:$s' : '$m:$s';
}

/// Compact docked player shown while an episode is loaded.
class MiniPlayerBar extends ConsumerWidget {
  const MiniPlayerBar({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final player = ref.watch(audioPlayerProvider);
    final episode = player.episode;
    if (episode == null) return const SizedBox.shrink();

    final duration = player.duration;
    final position = player.position;
    final progress =
        duration > Duration.zero ? position.inMilliseconds / duration.inMilliseconds : 0.0;

    return Material(
      color: theme.colorScheme.surfaceContainerHighest,
      elevation: 3,
      child: SafeArea(
        top: false,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            SizedBox(
              height: 64,
              child: Row(
                children: [
                  IconButton(
                    tooltip: 'Close',
                    onPressed: () => ref.read(audioPlayerProvider.notifier).close(),
                    icon: const Icon(Icons.close_rounded, size: 20),
                  ),
                  Expanded(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'EP ${episode.episodeNumber ?? ''} · ${episode.title}',
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: theme.textTheme.bodyMedium
                              ?.copyWith(fontWeight: FontWeight.w700),
                        ),
                        const SizedBox(height: 2),
                        Text(
                          player.buffering
                              ? 'جارٍ التحميل…'
                              : '${_fmt(position)} / ${_fmt(duration)}',
                          style: theme.textTheme.labelSmall
                              ?.copyWith(color: theme.colorScheme.outline),
                        ),
                      ],
                    ),
                  ),
                  IconButton(
                    tooltip: '-10s',
                    onPressed: () => ref.read(audioPlayerProvider.notifier).skipBy(-10),
                    icon: const Icon(Icons.replay_10_rounded),
                  ),
                  IconButton.filledTonal(
                    tooltip: 'Play/Pause',
                    onPressed: player.buffering
                        ? null
                        : () => ref.read(audioPlayerProvider.notifier).toggle(),
                    icon: player.buffering
                        ? const SizedBox(
                            width: 18,
                            height: 18,
                            child: CircularProgressIndicator(strokeWidth: 2))
                        : Icon(player.playing
                            ? Icons.pause_rounded
                            : Icons.play_arrow_rounded),
                  ),
                  IconButton(
                    tooltip: '+10s',
                    onPressed: () => ref.read(audioPlayerProvider.notifier).skipBy(10),
                    icon: const Icon(Icons.forward_10_rounded),
                  ),
                ],
              ),
            ),
            LinearProgressIndicator(
              value: progress.clamp(0.0, 1.0),
              minHeight: 3,
            ),
          ],
        ),
      ),
    );
  }
}

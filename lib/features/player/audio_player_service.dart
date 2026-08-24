import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';import 'package:just_audio/just_audio.dart';

import '../content/models.dart';
import '../content/providers.dart';

/// Global audio playback state for episode streaming.
class PlayerState {
  const PlayerState({
    this.episode,
    this.playing = false,
    this.buffering = false,
    this.position = Duration.zero,
    this.duration = Duration.zero,
    this.error,
  });

  final EpisodeItem? episode;
  final bool playing;
  final bool buffering;
  final Duration position;
  final Duration duration;
  final String? error;

  bool get hasEpisode => episode != null;

  PlayerState copyWith({
    EpisodeItem? episode,
    bool clearEpisode = false,
    bool? playing,
    bool? buffering,
    Duration? position,
    Duration? duration,
    String? error,
    bool clearError = false,
  }) =>
      PlayerState(
        episode: clearEpisode ? null : (episode ?? this.episode),
        playing: playing ?? this.playing,
        buffering: buffering ?? this.buffering,
        position: position ?? this.position,
        duration: duration ?? this.duration,
        error: clearError ? null : (error ?? this.error),
      );
}

class AudioPlayerService extends Notifier<PlayerState> {
  AudioPlayer? _player;
  bool _disposed = false;
  Timer? _syncTimer;

  @override
  PlayerState build() {
    ref.onDispose(() {
      _disposed = true;
      _syncTimer?.cancel();
      _player?.dispose();
    });
    return const PlayerState();
  }

  /// Starts (or restarts) playback of [episode], resuming from the last
  /// saved server-side listening position when available.
  Future<void> open(EpisodeItem episode) async {
    final url = episode.audioUrl;
    if (url == null || url.isEmpty) return;
    state = PlayerState(episode: episode, buffering: true);

    try {
      final player = _player ??= AudioPlayer();
      await player.setUrl(url);
      state = state.copyWith(
        episode: episode,
        duration: player.duration ?? Duration.zero,
        clearError: true,
      );

      // Resume from the previously saved position, if any.
      try {
        final saved = await ref
            .read(socialRepositoryProvider)
            .savedProgress(episode.id);
        if (saved > 3 && !_disposed && state.episode?.id == episode.id) {
          final max = player.duration ?? Duration.zero;
          final target = Duration(seconds: saved);
          if (max <= Duration.zero || target < max - const Duration(seconds: 5)) {
            await player.seek(target);
            state = state.copyWith(position: target);
          }
        }
      } catch (_) {}

      _listen(player);
      _startSync(player);
      await player.play();
    } catch (_) {
      state = state.copyWith(buffering: false, error: 'تعذر تشغيل الصوت');
    }
  }

  void _startSync(AudioPlayer player) {
    _syncTimer?.cancel();
    _syncTimer = Timer.periodic(const Duration(seconds: 5), (_) {
      if (_disposed || !player.playing || state.episode == null) return;
      final ep = state.episode!;
      ref.read(socialRepositoryProvider).saveProgress(
            ep.id,
            positionSeconds: player.position.inSeconds,
            durationSeconds: player.duration?.inSeconds,
          );
    });
  }

  Future<void> flushProgress() async {
    final player = _player;
    final ep = state.episode;
    if (player == null || ep == null) return;
    await ref.read(socialRepositoryProvider).saveProgress(
          ep.id,
          positionSeconds: player.position.inSeconds,
          durationSeconds: player.duration?.inSeconds,
        );
  }

  void _listen(AudioPlayer player) {
    player.playerStateStream.listen((s) {
      if (_disposed) return;
      state = state.copyWith(
        playing: s.playing,
        buffering: s.processingState == ProcessingState.loading ||
            s.processingState == ProcessingState.buffering,
      );
      if (s.processingState == ProcessingState.completed) {
        state = state.copyWith(playing: false, position: Duration.zero);
        player.seek(Duration.zero);
        player.pause();
      }
    });
    player.positionStream.listen((p) {
      if (_disposed) return;
      state = state.copyWith(position: p);
    });
    player.durationStream.listen((d) {
      if (_disposed || d == null) return;
      state = state.copyWith(duration: d);
    });
  }

  Future<void> toggle() async {
    final player = _player;
    if (player == null || state.episode == null) return;
    if (state.playing) {
      await player.pause();
    } else {
      await player.play();
    }
  }

  Future<void> seekTo(double fraction) async {
    final player = _player;
    if (player == null || state.duration <= Duration.zero) return;
    await player.seek(state.duration * fraction.clamp(0.0, 1.0));
  }

  Future<void> skipBy(int seconds) async {
    final player = _player;
    if (player == null) return;
    final target = player.position + Duration(seconds: seconds);
    final max = state.duration;
    await player.seek(
      target < Duration.zero
          ? Duration.zero
          : (max > Duration.zero && target > max ? max : target),
    );
  }

  Future<void> close() async {
    await flushProgress();
    _syncTimer?.cancel();
    await _player?.stop();
    state = const PlayerState();
  }
}

final audioPlayerProvider =
    NotifierProvider<AudioPlayerService, PlayerState>(AudioPlayerService.new);

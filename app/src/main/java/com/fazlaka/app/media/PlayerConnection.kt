package com.fazlaka.app.media

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerConnection @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var controller: MediaController? = null
    private var future: ListenableFuture<MediaController>? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _subtitle = MutableStateFlow("")
    val subtitle: StateFlow<String> = _subtitle.asStateFlow()

    private val _episodeSlug = MutableStateFlow<String?>(null)
    val episodeSlug: StateFlow<String?> = _episodeSlug.asStateFlow()

    private val _hasMedia = MutableStateFlow(false)
    val hasMedia: StateFlow<Boolean> = _hasMedia.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val player = controller ?: return
            _duration.value = if (player.duration != C.TIME_UNSET) player.duration else 0L
            _currentPosition.value = player.currentPosition
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateMediaMetadata(mediaItem)
        }

        override fun onPlayerError(error: PlaybackException) {
            _hasMedia.value = false
        }
    }

    fun connect() {
        if (controller != null) return
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java),
        )
        future = MediaController.Builder(context, sessionToken).buildAsync()
        future?.addListener({
            try {
                controller = future?.get()?.also { player ->
                    player.addListener(listener)
                    _isPlaying.value = player.isPlaying
                    _duration.value = if (player.duration != C.TIME_UNSET) player.duration else 0L
                    _currentPosition.value = player.currentPosition
                    _hasMedia.value = player.currentMediaItem != null
                    updateMediaMetadata(player.currentMediaItem)
                }
            } catch (_: Exception) {
            }
        }, java.util.concurrent.Executors.newSingleThreadExecutor())
    }

    fun disconnect() {
        controller?.removeListener(listener)
        future?.cancel(true)
        controller?.release()
        controller = null
        future = null
    }

    fun togglePlayPause() {
        val player = controller ?: return
        if (player.isPlaying) player.pause() else player.play()
    }

    fun skipToNext() {
        val player = controller ?: return
        if (player.hasNextMediaItem()) player.seekToNext()
    }

    fun skipToPrevious() {
        val player = controller ?: return
        if (player.currentPosition > 3000) player.seekTo(0) else if (player.hasPreviousMediaItem()) player.seekToPrevious()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
    }

    fun playMedia(audioUrl: String, title: String, subtitle: String, episodeSlug: String?) {
        val extras = bundleOf("episodeSlug" to episodeSlug)
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(subtitle)
            .setExtras(extras)
            .build()
        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(audioUrl))
            .setMediaMetadata(metadata)
            .build()
        val player = controller ?: return
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        _hasMedia.value = true
        _title.value = title
        _subtitle.value = subtitle
        _episodeSlug.value = episodeSlug
    }

    fun tickPosition() {
        val player = controller ?: return
        _currentPosition.value = player.currentPosition
    }

    private fun updateMediaMetadata(mediaItem: MediaItem?) {
        if (mediaItem == null) {
            _hasMedia.value = false
            return
        }
        _title.value = mediaItem.mediaMetadata.title?.toString() ?: ""
        _subtitle.value = mediaItem.mediaMetadata.artist?.toString() ?: ""
        _episodeSlug.value = mediaItem.mediaMetadata.extras?.getString("episodeSlug")
        _hasMedia.value = true
    }
}

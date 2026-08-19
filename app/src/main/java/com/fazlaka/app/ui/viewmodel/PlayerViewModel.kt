package com.fazlaka.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fazlaka.app.media.PlayerConnection
import com.fazlaka.app.ui.components.MiniPlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val playerConnection: PlayerConnection,
) : ViewModel() {

    val miniPlayerState: StateFlow<MiniPlayerState> = combine(
        combine(
            playerConnection.hasMedia,
            playerConnection.title,
            playerConnection.subtitle,
        ) { hasMedia, title, subtitle -> Triple(hasMedia, title, subtitle) },
        combine(
            playerConnection.isPlaying,
            playerConnection.currentPosition,
            playerConnection.duration,
        ) { isPlaying, position, duration -> Triple(isPlaying, position, duration) },
    ) { (hasMedia, title, subtitle), (isPlaying, position, duration) ->
        MiniPlayerState(
            visible = hasMedia,
            title = title,
            subtitle = subtitle,
            isPlaying = isPlaying,
            progress = if (duration > 0) (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f,
        )
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), MiniPlayerState())

    val episodeSlug: StateFlow<String?> = playerConnection.episodeSlug

    init {
        playerConnection.connect()
        startProgressTicker()
    }

    private fun startProgressTicker() {
        viewModelScope.launch {
            while (true) {
                playerConnection.tickPosition()
                delay(500)
            }
        }
    }

    fun togglePlayPause() = playerConnection.togglePlayPause()
    fun skipToNext() = playerConnection.skipToNext()
    fun skipToPrevious() = playerConnection.skipToPrevious()

    override fun onCleared() {
        super.onCleared()
        playerConnection.disconnect()
    }
}

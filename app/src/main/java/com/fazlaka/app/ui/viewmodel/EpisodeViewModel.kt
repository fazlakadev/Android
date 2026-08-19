package com.fazlaka.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fazlaka.app.core.model.dto.EpisodeDto
import com.fazlaka.app.core.model.dto.RelatedEpisodesDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.data.repository.ContentRepository
import com.fazlaka.app.data.repository.SocialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EpisodeUiState(
    val episode: ApiResult<EpisodeDto>? = null,
    val related: ApiResult<RelatedEpisodesDto>? = null,
    val progress: Int = 0,
    val liked: Boolean = false,
    val likeBusy: Boolean = false,
    val resumePosition: Int = 0,
)

@HiltViewModel
class EpisodeViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val socialRepository: SocialRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val idOrSlug: String = savedStateHandle.get<String>("idOrSlug") ?: ""

    private val _state = MutableStateFlow(EpisodeUiState())
    val state: StateFlow<EpisodeUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val episode = contentRepository.episode(idOrSlug)
            val related = contentRepository.related(idOrSlug)
            _state.value = _state.value.copy(episode = episode, related = related)
            val id = (episode as? ApiResult.Success)?.data?.id
            if (id != null) {
                contentRepository.trackView("episode", id, null, false)
                loadLike(id)
                loadProgress(id)
            }
        }
    }

    private suspend fun loadLike(episodeId: String) {
        val liked = (socialRepository.likeStatus("episode", episodeId) as? ApiResult.Success)?.data?.liked
        if (liked != null) {
            _state.value = _state.value.copy(liked = liked)
        }
    }

    private suspend fun loadProgress(episodeId: String) {
        val position = (contentRepository.progressFor(episodeId) as? ApiResult.Success)?.data?.positionSeconds ?: 0
        if (position > 0) {
            _state.value = _state.value.copy(resumePosition = position)
        }
    }

    fun toggleLike() {
        val id = (_state.value.episode as? ApiResult.Success)?.data?.id ?: return
        if (_state.value.likeBusy) return
        val optimistic = !_state.value.liked
        _state.value = _state.value.copy(liked = optimistic, likeBusy = true)
        viewModelScope.launch {
            val result = socialRepository.toggleLike("episode", id)
            val confirmed = (result as? ApiResult.Success)?.data?.liked
            _state.value = _state.value.copy(
                liked = confirmed ?: optimistic,
                likeBusy = false,
            )
        }
    }

    fun saveProgress(episodeId: String, position: Int, duration: Int?) {
        viewModelScope.launch {
            contentRepository.cacheProgress(episodeId, position, duration)
            contentRepository.updateProgress(episodeId, position, duration)
        }
    }

    fun clearProgress(episodeId: String) {
        viewModelScope.launch {
            contentRepository.deleteLocalProgress(episodeId)
            contentRepository.removeProgress(episodeId)
        }
    }
}

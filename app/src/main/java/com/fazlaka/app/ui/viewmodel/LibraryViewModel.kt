package com.fazlaka.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fazlaka.app.core.model.dto.LikeHistoryItemDto
import com.fazlaka.app.core.model.dto.PlaylistDto
import com.fazlaka.app.core.model.dto.ProgressItemDto
import com.fazlaka.app.core.model.dto.ReferralsDto
import com.fazlaka.app.core.model.dto.ViewHistoryItemDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.data.repository.ContentRepository
import com.fazlaka.app.data.repository.ProfileRepository
import com.fazlaka.app.data.repository.SocialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val loading: Boolean = false,
    val viewHistory: ApiResult<List<ViewHistoryItemDto>>? = null,
    val playlists: ApiResult<List<PlaylistDto>>? = null,
    val progress: ApiResult<List<ProgressItemDto>>? = null,
    val referrals: ApiResult<ReferralsDto>? = null,
    val likesHistory: ApiResult<List<LikeHistoryItemDto>>? = null,
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val profileRepository: ProfileRepository,
    private val socialRepository: SocialRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryUiState())
    val state: StateFlow<LibraryUiState> = _state.asStateFlow()

    fun loadViewHistory() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val r = profileRepository.viewHistory()
            val mapped: ApiResult<List<ViewHistoryItemDto>>? = when (r) {
                is ApiResult.Success -> ApiResult.Success(r.data.data)
                is ApiResult.Failure -> r
            }
            _state.value = _state.value.copy(viewHistory = mapped, loading = false)
        }
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val r = contentRepository.playlists()
            val mapped: ApiResult<List<PlaylistDto>>? = when (r) {
                is ApiResult.Success -> ApiResult.Success(r.data.data)
                is ApiResult.Failure -> r
            }
            _state.value = _state.value.copy(playlists = mapped, loading = false)
        }
    }

    fun loadProgress() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            _state.value = _state.value.copy(
                progress = contentRepository.progressList(),
                loading = false,
            )
        }
    }

    fun loadReferrals() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            _state.value = _state.value.copy(
                referrals = profileRepository.referrals(),
                loading = false,
            )
        }
    }

    fun loadLikesHistory() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val r = socialRepository.likeHistory()
            val mapped: ApiResult<List<LikeHistoryItemDto>>? = when (r) {
                is ApiResult.Success -> ApiResult.Success(r.data.data)
                is ApiResult.Failure -> r
            }
            _state.value = _state.value.copy(likesHistory = mapped, loading = false)
        }
    }

    fun clearViewHistory() {
        viewModelScope.launch {
            profileRepository.clearViewHistory()
            loadViewHistory()
        }
    }
}

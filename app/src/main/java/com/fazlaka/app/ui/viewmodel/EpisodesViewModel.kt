package com.fazlaka.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fazlaka.app.core.model.dto.EpisodeDto
import com.fazlaka.app.core.model.dto.Paginated
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.data.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EpisodesViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<ApiResult<Paginated<EpisodeDto>>?>(null)
    val state: StateFlow<ApiResult<Paginated<EpisodeDto>>?> = _state.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            if (_state.value is ApiResult.Success) {
                _refreshing.value = true
                val result = contentRepository.episodes(page = 1, limit = 50)
                _state.value = result
                _refreshing.value = false
            } else {
                _state.value = contentRepository.episodes(page = 1, limit = 50)
            }
        }
    }
}

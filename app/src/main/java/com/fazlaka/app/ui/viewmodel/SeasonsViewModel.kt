package com.fazlaka.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fazlaka.app.core.model.dto.Paginated
import com.fazlaka.app.core.model.dto.SeasonDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.data.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeasonsViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<ApiResult<Paginated<SeasonDto>>?>(null)
    val state: StateFlow<ApiResult<Paginated<SeasonDto>>?> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun load() {
        viewModelScope.launch {
            if (_state.value == null) {
                _state.value = contentRepository.seasons()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _state.value = contentRepository.seasons()
            _isRefreshing.value = false
        }
    }
}

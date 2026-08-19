package com.fazlaka.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class SeasonViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val idOrSlug: String = savedStateHandle.get<String>("idOrSlug") ?: ""

    private val _state = MutableStateFlow<ApiResult<SeasonDto>?>(null)
    val state: StateFlow<ApiResult<SeasonDto>?> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = contentRepository.season(idOrSlug)
        }
    }
}

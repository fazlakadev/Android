package com.fazlaka.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fazlaka.app.core.database.SearchHistoryEntity
import com.fazlaka.app.core.model.dto.SearchResponseDto
import com.fazlaka.app.core.model.dto.SuggestionsDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.data.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val searching: Boolean = false,
    val results: ApiResult<SearchResponseDto>? = null,
    val suggestions: ApiResult<SuggestionsDto>? = null,
    val submitted: Boolean = false,
    val activeType: String? = null,
    val activeCategory: String? = null,
    val showFilters: Boolean = false,
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var debounceJob: Job? = null

    val recentSearches: Flow<List<SearchHistoryEntity>> = contentRepository.recentSearches()

    fun onQueryChange(query: String) {
        _state.value = _state.value.copy(query = query, submitted = false)
        debounceJob?.cancel()
        if (query.isBlank()) {
            _state.value = _state.value.copy(suggestions = null, results = null)
            return
        }
        debounceJob = viewModelScope.launch {
            delay(300)
            contentRepository.suggestions(query).let { _state.value = _state.value.copy(suggestions = it) }
        }
    }

    fun submitSearch(query: String = _state.value.query) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(searching = true, submitted = true)
            contentRepository.addSearch(query.trim())
            contentRepository.search(
                q = query.trim(),
                type = _state.value.activeType,
                category = _state.value.activeCategory,
            ).let {
                _state.value = _state.value.copy(results = it, searching = false)
            }
        }
    }

    fun onTypeFilter(type: String?) {
        _state.value = _state.value.copy(activeType = if (_state.value.activeType == type) null else type)
        if (_state.value.submitted) submitSearch()
    }

    fun onCategoryFilter(category: String?) {
        _state.value = _state.value.copy(activeCategory = if (_state.value.activeCategory == category) null else category)
        if (_state.value.submitted) submitSearch()
    }

    fun toggleFilters() {
        _state.value = _state.value.copy(showFilters = !_state.value.showFilters)
    }

    fun removeSearch(query: String) {
        viewModelScope.launch { contentRepository.removeSearch(query) }
    }

    fun clearSearches() {
        viewModelScope.launch { contentRepository.clearSearches() }
    }
}

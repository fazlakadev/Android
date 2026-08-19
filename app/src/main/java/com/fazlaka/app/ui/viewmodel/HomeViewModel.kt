package com.fazlaka.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fazlaka.app.core.model.dto.BannerDto
import com.fazlaka.app.core.model.dto.EpisodeDto
import com.fazlaka.app.core.model.dto.Paginated
import com.fazlaka.app.core.model.dto.RecommendationsDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.data.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val banners: ApiResult<List<BannerDto>>? = null,
    val recommendations: ApiResult<RecommendationsDto>? = null,
    val episodes: ApiResult<Paginated<EpisodeDto>>? = null,
    val refreshing: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(refreshing = true)
            val banners = contentRepository.banners("hero")
            val recommendations = contentRepository.recommendations()
            val episodes = contentRepository.episodes(page = 1, limit = 20)
            _state.value = HomeUiState(
                banners = banners,
                recommendations = recommendations,
                episodes = episodes,
            )
        }
    }

    fun onBannerImpression(id: String) {
        viewModelScope.launch { contentRepository.bannerImpression(id) }
    }

    fun onBannerClick(id: String) {
        viewModelScope.launch { contentRepository.bannerClick(id) }
    }
}

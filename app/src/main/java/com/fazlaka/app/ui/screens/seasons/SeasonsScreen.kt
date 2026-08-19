package com.fazlaka.app.ui.screens.seasons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fazlaka.app.core.model.dto.SeasonDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.data.repository.ContentRepository
import com.fazlaka.app.ui.components.ApiResultContent
import com.fazlaka.app.ui.components.EmptyState
import com.fazlaka.app.ui.components.GridSkeleton
import com.fazlaka.app.ui.components.HeroAccents
import com.fazlaka.app.ui.components.HeroSection
import com.fazlaka.app.ui.components.PosterImage
import com.fazlaka.app.ui.navigation.Routes
import com.fazlaka.app.ui.util.localizedSeasonTitle
import com.fazlaka.app.ui.viewmodel.SeasonsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonsScreen(
    onNavigate: (String) -> Unit,
    viewModel: SeasonsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.load() }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize(),
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        HeroSection(
            title = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.seasons_hero_title),
            subtitle = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.seasons_hero_sub),
            badge = "🎬 ${seasonsCount(state)} موسم",
            accent = HeroAccents.Seasons,
            minHeight = 140.dp,
            fullscreenTop = true,
        )
        ApiResultContent(
            result = state,
            onRetry = { viewModel.load() },
            loadingContent = { GridSkeleton() },
        ) { seasons ->
            if (seasons.data.isEmpty()) {
                EmptyState(
                    title = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.seasons_empty),
                    subtitle = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.seasons_empty_sub),
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(seasons.data, key = { it.id }, contentType = { "season" }) { season ->
                        SeasonCard(season = season, onClick = {
                            onNavigate(Routes.season(season.slug.ifBlank { season.id }))
                        })
                    }
                }
            }
        }
    }
    }
}

private fun seasonsCount(state: ApiResult<com.fazlaka.app.core.model.dto.Paginated<SeasonDto>>?): Int =
    (state as? ApiResult.Success)?.data?.data?.size ?: 0

@Composable
private fun SeasonCard(season: SeasonDto, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        PosterImage(
            url = season.coverImage,
            contentDescription = localizedSeasonTitle(season.translations, season.slug),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = localizedSeasonTitle(season.translations, season.slug),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

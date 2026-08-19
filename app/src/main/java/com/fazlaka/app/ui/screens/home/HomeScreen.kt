package com.fazlaka.app.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fazlaka.app.core.model.dto.BannerDto
import com.fazlaka.app.core.model.dto.EpisodeDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.ui.accessibility.accessibleCard
import com.fazlaka.app.ui.components.ApiResultContent
import com.fazlaka.app.ui.components.ErrorState
import com.fazlaka.app.ui.components.HeroAccents
import com.fazlaka.app.ui.components.HeroSection
import com.fazlaka.app.ui.components.HomeSkeleton
import com.fazlaka.app.ui.components.SectionHeader
import com.fazlaka.app.ui.components.PosterImage
import com.fazlaka.app.ui.navigation.Routes
import com.fazlaka.app.ui.theme.FazlakaCyan
import com.fazlaka.app.ui.theme.FazlakaGradientStart
import com.fazlaka.app.ui.util.localizedSeasonTitle
import com.fazlaka.app.ui.util.localizedTitle
import com.fazlaka.app.ui.viewmodel.HomeViewModel
import com.fazlaka.app.ui.viewmodel.HomeUiState
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.banners == null && state.recommendations == null && state.episodes == null) {
        HomeSkeleton()
        return
    }

    val allFailed = state.banners is ApiResult.Failure &&
        state.recommendations is ApiResult.Failure &&
        state.episodes is ApiResult.Failure

    if (allFailed) {
        ErrorState(
            message = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.home_error),
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            onRetry = { viewModel.load() },
        )
        return
    }

    PullToRefreshBox(
        isRefreshing = state.refreshing,
        onRefresh = { viewModel.load() },
        modifier = Modifier.fillMaxSize(),
    ) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -it / 6 },
            ) {
                HeroSection(
                    title = "فذلكة",
                    subtitle = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.home_hero_sub),
                    badge = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.home_hero_badge),
                    accent = HeroAccents.Home,
                    minHeight = 150.dp,
                    fullscreenTop = true,
                )
            }
        }
        item { BannerCarousel(state, onNavigate, viewModel) }
        item {
            SectionHeader(title = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.home_for_you))
        }
        item { RecommendationsRow(state, onNavigate) }
        item {
            SectionHeader(
                title = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.home_latest),
                actionLabel = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.home_all),
                onAction = { onNavigate(Routes.ALL_EPISODES) },
            )
        }
        itemsIndexed(episodesFrom(state), key = { _, it -> it.id }, contentType = { _, _ -> "episode" }) { index, episode ->
            EpisodeRow(
                episode = episode,
                modifier = Modifier.animateItem(
                    fadeInSpec = tween(450, delayMillis = (index % 8) * 55, easing = LinearOutSlowInEasing),
                ),
                onClick = {
                    onNavigate(Routes.episode(episode.slug.ifBlank { episode.id }))
                },
            )
        }
    }
    }
}

private fun episodesFrom(state: HomeUiState): List<EpisodeDto> =
    (state.episodes as? ApiResult.Success)?.data?.data ?: emptyList()

@Composable
private fun BannerCarousel(
    state: HomeUiState,
    onNavigate: (String) -> Unit,
    viewModel: HomeViewModel,
) {
    val banners = (state.banners as? ApiResult.Success)?.data ?: emptyList()
    if (banners.isEmpty()) return
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(banners, key = { _, it -> it.id }, contentType = { _, _ -> "banner" }) { index, banner ->
            Box(
                modifier = Modifier.animateItem(
                    fadeInSpec = tween(500, delayMillis = index * 80, easing = FastOutSlowInEasing),
                ),
            ) {
                BannerItem(banner = banner, onClick = {
                    viewModel.onBannerClick(banner.id)
                    banner.linkUrl?.let { url ->
                        if (url.startsWith("/")) {
                            val path = url.removePrefix("/api/v1/").removePrefix("/")
                            when {
                                path.contains("episode") -> onNavigate(Routes.episode(path.substringAfterLast("/")))
                                path.contains("season") -> onNavigate(Routes.season(path.substringAfterLast("/")))
                                path.contains("article") -> onNavigate(Routes.article(path.substringAfterLast("/")))
                            }
                        }
                    }
                })
            }
        }
    }
}

@Composable
private fun BannerItem(banner: BannerDto, onClick: () -> Unit) {
    val title = banner.translations.firstOrNull { it.locale == "ar" }?.title
        ?: banner.translations.firstOrNull()?.title
        ?: ""
    Box(
        modifier = Modifier
            .width(320.dp)
            .height(170.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .accessibleCard(
                title = title,
                onClickDescription = "فتح الشعار",
            ),
    ) {
        AsyncImage(
            model = banner.imageUrl,
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                        startY = 0.35f,
                        endY = 1f,
                    ),
                ),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RecommendationsRow(state: HomeUiState, onNavigate: (String) -> Unit) {
    val episodes = (state.recommendations as? ApiResult.Success)?.data?.episodes ?: emptyList()
    val seasons = (state.recommendations as? ApiResult.Success)?.data?.seasons ?: emptyList()
    if (episodes.isEmpty() && seasons.isEmpty()) return
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(episodes, key = { _, it -> it.id }, contentType = { _, _ -> "episode" }) { index, ep ->
            Box(
                modifier = Modifier.animateItem(
                    fadeInSpec = tween(450, delayMillis = index * 60, easing = FastOutSlowInEasing),
                ),
            ) {
                PosterImage(
                    url = ep.coverImage,
                    contentDescription = ep.title,
                    modifier = Modifier
                        .width(120.dp)
                        .height(170.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onNavigate(Routes.episode(ep.slug.ifBlank { ep.id })) },
                )
            }
        }
        itemsIndexed(seasons, key = { _, it -> it.id }, contentType = { _, _ -> "season" }) { index, s ->
            Box(
                modifier = Modifier.animateItem(
                    fadeInSpec = tween(450, delayMillis = index * 60, easing = FastOutSlowInEasing),
                ),
            ) {
                PosterImage(
                    url = s.coverImage,
                    contentDescription = s.title,
                    modifier = Modifier
                        .width(120.dp)
                        .height(170.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onNavigate(Routes.season(s.slug.ifBlank { s.id })) },
                )
            }
        }
    }
}

@Composable
private fun EpisodeRow(
    episode: EpisodeDto,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val title = localizedTitle(episode.translations, episode.slug)
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PosterImage(
                url = episode.coverImage,
                contentDescription = title,
                modifier = Modifier
                    .width(100.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(8.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = listOfNotNull(
                        episode.season?.let { localizedSeasonTitle(it.translations) },
                        episode.duration?.let { com.fazlaka.app.ui.util.formatDuration(it) },
                    ).joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

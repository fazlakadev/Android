package com.fazlaka.app.ui.screens.seasons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fazlaka.app.core.model.dto.EpisodeDto
import com.fazlaka.app.core.model.dto.SeasonDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.ui.components.ApiResultContent
import com.fazlaka.app.ui.components.DetailSkeleton
import com.fazlaka.app.ui.components.PosterImage
import com.fazlaka.app.ui.navigation.Routes
import com.fazlaka.app.ui.util.ShareUtils
import com.fazlaka.app.ui.util.formatDuration
import com.fazlaka.app.ui.util.localizedSeasonTitle
import com.fazlaka.app.ui.util.localizedTitle
import com.fazlaka.app.ui.viewmodel.SeasonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonDetailScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: SeasonViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val s = (state as? ApiResult.Success)?.data
                        if (s != null) {
                            val title = localizedSeasonTitle(s.translations, s.slug)
                            ShareUtils.shareSeason(context, s.slug.ifBlank { s.id }, title)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        },
    ) { innerPadding ->
        ApiResultContent(
            result = state,
            onRetry = { viewModel.load() },
            modifier = Modifier.padding(innerPadding),
            loadingContent = { DetailSkeleton(modifier = Modifier.padding(innerPadding)) },
        ) { season ->
            SeasonContent(season = season, onEpisodeClick = { ep ->
                onNavigate(Routes.episode(ep.slug.ifBlank { ep.id }))
            })
        }
    }
}

@Composable
private fun SeasonContent(season: SeasonDto, onEpisodeClick: (EpisodeDto) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
    ) {
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PosterImage(
                        url = season.coverImage,
                        contentDescription = localizedSeasonTitle(season.translations, season.slug),
                        modifier = Modifier
                            .width(140.dp)
                            .height(200.dp),
                        shape = RoundedCornerShape(12.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = localizedSeasonTitle(season.translations, season.slug),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = season.translations.firstOrNull { it.locale == "ar" }?.description
                                ?: season.translations.firstOrNull()?.description
                                ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(com.fazlaka.app.R.string.count_episodes_fmt, season.count?.episodes ?: season.episodes.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
        items(season.episodes, key = { it.id }, contentType = { "episode" }) { episode ->
            EpisodeListItem(episode = episode, onClick = { onEpisodeClick(episode) })
        }
    }
}

@Composable
private fun EpisodeListItem(episode: EpisodeDto, onClick: () -> Unit) {
    val title = localizedTitle(episode.translations, episode.slug)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PosterImage(
                url = episode.coverImage,
                contentDescription = title,
                modifier = Modifier
                    .width(90.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(8.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = listOfNotNull(
                        episode.episodeNumber?.let { stringResource(com.fazlaka.app.R.string.episode_label, it) },
                        episode.duration?.let { formatDuration(it) },
                    ).joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "تشغيل",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

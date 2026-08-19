package com.fazlaka.app.ui.screens.episodes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fazlaka.app.core.model.dto.EpisodeDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.ui.components.ApiResultContent
import com.fazlaka.app.ui.components.EmptyState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.fazlaka.app.ui.components.ListSkeleton
import com.fazlaka.app.ui.components.PosterImage
import com.fazlaka.app.ui.navigation.Routes
import com.fazlaka.app.ui.util.formatDuration
import com.fazlaka.app.ui.util.localizedSeasonTitle
import com.fazlaka.app.ui.util.localizedTitle
import com.fazlaka.app.ui.viewmodel.EpisodesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllEpisodesScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: EpisodesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.refreshing.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.episodes_all_title), fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.load() },
            modifier = Modifier.padding(innerPadding),
        ) {
        ApiResultContent(
            result = state,
            onRetry = { viewModel.load() },
            loadingContent = { ListSkeleton(rowCount = 8) },
        ) { page ->
            if (page.data.isEmpty()) {
                EmptyState(
                    title = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.episodes_empty),
                    subtitle = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.episodes_empty_sub),
                    modifier = Modifier.padding(innerPadding),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                ) {
                    itemsIndexed(page.data, key = { _, it -> it.id }, contentType = { _, _ -> "episode" }) { index, episode ->
                        EpisodeListRow(
                            episode = episode,
                            onClick = {
                                onNavigate(Routes.episode(episode.slug.ifBlank { episode.id }))
                            },
                            modifier = Modifier.padding(top = if (index == 0) 8.dp else 4.dp),
                        )
            }
        }
        }
    }
}
    }
}

@Composable
private fun EpisodeListRow(
    episode: EpisodeDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
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
                    .fillMaxWidth(0.35f)
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
                        episode.duration?.let { formatDuration(it) },
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

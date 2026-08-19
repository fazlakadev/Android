package com.fazlaka.app.ui.screens.content

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import com.fazlaka.app.core.model.dto.PlaylistDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.ui.components.ApiResultContent
import com.fazlaka.app.ui.components.Avatar
import com.fazlaka.app.ui.components.DetailSkeleton
import com.fazlaka.app.ui.components.PosterImage
import com.fazlaka.app.ui.navigation.Routes
import com.fazlaka.app.ui.util.ShareUtils
import com.fazlaka.app.ui.util.formatDuration
import com.fazlaka.app.ui.util.formatRelative
import com.fazlaka.app.ui.util.localizedTitle
import com.fazlaka.app.ui.viewmodel.PlaylistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: PlaylistViewModel = hiltViewModel(),
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
                        val pl = (state as? ApiResult.Success)?.data
                        if (pl != null) {
                            val title = pl.translations.firstOrNull { it.locale == "ar" }?.title
                                ?: pl.translations.firstOrNull()?.title
                                ?: pl.slug
                            ShareUtils.sharePlaylist(context, pl.slug.ifBlank { pl.id }, title)
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
                ),
            )
        },
    ) { innerPadding ->
        ApiResultContent(
            result = state,
            onRetry = { viewModel.load() },
            modifier = Modifier.padding(innerPadding),
            loadingContent = { DetailSkeleton(modifier = Modifier.padding(innerPadding)) },
        ) { playlist ->
            PlaylistContent(playlist = playlist, onEpisodeClick = { ep ->
                onNavigate(Routes.episode(ep.slug.ifBlank { ep.id }))
            })
        }
    }
}

@Composable
private fun PlaylistContent(playlist: PlaylistDto, onEpisodeClick: (EpisodeDto) -> Unit) {
    val title = playlist.translations.firstOrNull { it.locale == "ar" }?.title
        ?: playlist.translations.firstOrNull()?.title
        ?: playlist.slug

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PosterImage(
                    url = playlist.coverImage,
                    contentDescription = title,
                    modifier = Modifier
                        .width(120.dp)
                        .height(170.dp),
                    shape = RoundedCornerShape(12.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(com.fazlaka.app.R.string.item_count_fmt, playlist.count?.items ?: playlist.items.size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    playlist.owner?.let { owner ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Avatar(url = owner.avatarUrl, name = owner.name, size = 24)
                            Spacer(Modifier.width(8.dp))
                            Text(owner.name, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
        items(playlist.items) { item ->
            item.episode?.let { episode ->
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
                            .clickable { onEpisodeClick(episode) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        PosterImage(
                            url = episode.coverImage,
                            contentDescription = localizedTitle(episode.translations, episode.slug),
                            modifier = Modifier
                                .width(90.dp)
                                .height(54.dp),
                            shape = RoundedCornerShape(8.dp),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = localizedTitle(episode.translations, episode.slug),
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = episode.duration?.let { formatDuration(it) } ?: "",
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
        }
    }
}

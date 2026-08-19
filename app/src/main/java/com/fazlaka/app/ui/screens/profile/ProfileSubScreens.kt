package com.fazlaka.app.ui.screens.profile

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fazlaka.app.core.model.dto.PlaylistDto
import com.fazlaka.app.core.model.dto.ProgressItemDto
import com.fazlaka.app.core.model.dto.ViewHistoryItemDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.ui.components.ApiResultContent
import com.fazlaka.app.ui.components.ListSkeleton
import com.fazlaka.app.ui.components.PosterImage
import com.fazlaka.app.ui.navigation.Routes
import com.fazlaka.app.ui.util.formatDuration
import com.fazlaka.app.ui.util.localizedTitle
import com.fazlaka.app.ui.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewHistoryScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadViewHistory() }

    SimpleScaffold(onBack = onBack, title = stringResource(com.fazlaka.app.R.string.pf_history_title)) { innerPadding ->
        ApiResultContent(
            result = state.viewHistory,
            onRetry = { viewModel.loadViewHistory() },
            modifier = Modifier.padding(innerPadding),
            emptyTitle = stringResource(com.fazlaka.app.R.string.pf_history_empty),
            loadingContent = { ListSkeleton(modifier = Modifier.padding(innerPadding)) },
        ) { items ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(items, key = { it.contentId }, contentType = { "historyItem" }) { item ->
                    HistoryRow(item = item, onClick = {
                        val slug = item.slug ?: item.episode?.slug ?: item.contentId
                        onNavigate(Routes.episode(slug.ifBlank { item.contentId }))
                    })
                }
                if (items.isNotEmpty()) {
                    item {
                        TextButton(onClick = { viewModel.clearViewHistory() }) {
                            Text(stringResource(com.fazlaka.app.R.string.pf_clear_history))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadProgress() }

    SimpleScaffold(onBack = onBack, title = stringResource(com.fazlaka.app.R.string.pf_progress_title)) { innerPadding ->
        ApiResultContent(
            result = state.progress,
            onRetry = { viewModel.loadProgress() },
            modifier = Modifier.padding(innerPadding),
            emptyTitle = stringResource(com.fazlaka.app.R.string.pf_progress_empty),
            loadingContent = { ListSkeleton(modifier = Modifier.padding(innerPadding)) },
        ) { items ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(items, key = { it.episodeId }, contentType = { "progressItem" }) { item ->
                    ProgressRow(item = item, onClick = {
                        onNavigate(Routes.episode(item.episode?.slug ?: item.episodeId))
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPlaylistsScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadPlaylists() }

    SimpleScaffold(onBack = onBack, title = stringResource(com.fazlaka.app.R.string.pf_playlists_title)) { innerPadding ->
        ApiResultContent(
            result = state.playlists,
            onRetry = { viewModel.loadPlaylists() },
            modifier = Modifier.padding(innerPadding),
            emptyTitle = stringResource(com.fazlaka.app.R.string.pf_playlists_empty),
            loadingContent = { ListSkeleton(modifier = Modifier.padding(innerPadding)) },
        ) { items ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(items, key = { it.id }, contentType = { "playlist" }) { playlist ->
                    PlaylistRow(playlist = playlist, onClick = {
                        onNavigate(Routes.playlist(playlist.slug.ifBlank { playlist.id }))
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikesHistoryScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadLikesHistory() }

    SimpleScaffold(onBack = onBack, title = stringResource(com.fazlaka.app.R.string.pf_likes_title)) { innerPadding ->
        ApiResultContent(
            result = state.likesHistory,
            onRetry = { viewModel.loadLikesHistory() },
            modifier = Modifier.padding(innerPadding),
            emptyTitle = stringResource(com.fazlaka.app.R.string.pf_likes_empty),
            loadingContent = { ListSkeleton(modifier = Modifier.padding(innerPadding)) },
        ) { items ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(items, key = { it.contentId }, contentType = { "likeItem" }) { item ->
                    HistoryRow(
                        item = com.fazlaka.app.core.model.dto.ViewHistoryItemDto(
                            id = item.id,
                            contentType = item.contentType,
                            contentId = item.contentId,
                            title = item.title,
                            coverImage = item.coverImage,
                            slug = item.episode?.slug,
                            episode = item.episode,
                            watchedAt = item.likedAt,
                        ),
                        onClick = {
                            onNavigate(Routes.episode(item.episode?.slug ?: item.contentId))
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferralsScreen(
    onBack: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadReferrals() }

    SimpleScaffold(onBack = onBack, title = stringResource(com.fazlaka.app.R.string.pf_referral_title)) { innerPadding ->
        ApiResultContent(
            result = state.referrals,
            onRetry = { viewModel.loadReferrals() },
            modifier = Modifier.padding(innerPadding),
            emptyTitle = stringResource(com.fazlaka.app.R.string.pf_referral_empty),
            loadingContent = { ListSkeleton(modifier = Modifier.padding(innerPadding)) },
        ) { data ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                Text(stringResource(com.fazlaka.app.R.string.pf_referral_share), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = data.referralCode ?: "—",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(com.fazlaka.app.R.string.pf_referral_count_fmt, data.referrals.size),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleScaffold(
    onBack: () -> Unit,
    title: String,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
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
        content(innerPadding)
    }
}

@Composable
private fun HistoryRow(item: ViewHistoryItemDto, onClick: () -> Unit) {
    val title = item.title ?: item.episode?.let { localizedTitle(it.translations) } ?: ""
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
                url = item.coverImage ?: item.episode?.coverImage,
                contentDescription = title,
                modifier = Modifier
                    .width(90.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(8.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (item.completed) stringResource(com.fazlaka.app.R.string.pf_complete) else com.fazlaka.app.ui.util.formatRelative(item.watchedAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (item.completed) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ProgressRow(item: ProgressItemDto, onClick: () -> Unit) {
    val title = item.title ?: item.episode?.let { localizedTitle(it.translations) } ?: ""
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
                url = item.coverImage ?: item.episode?.coverImage,
                contentDescription = title,
                modifier = Modifier
                    .width(90.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(8.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${item.percent}% • ${formatDuration(item.positionSeconds)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PlaylistRow(playlist: PlaylistDto, onClick: () -> Unit) {
    val title = playlist.translations.firstOrNull { it.locale == "ar" }?.title
        ?: playlist.translations.firstOrNull()?.title
        ?: playlist.slug
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
                url = playlist.coverImage,
                contentDescription = title,
                modifier = Modifier
                    .width(90.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(8.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(com.fazlaka.app.R.string.item_count_fmt, playlist.count?.items ?: playlist.items.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

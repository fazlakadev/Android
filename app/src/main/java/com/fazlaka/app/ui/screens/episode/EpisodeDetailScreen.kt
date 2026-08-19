package com.fazlaka.app.ui.screens.episode

import android.view.ViewGroup
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.fazlaka.app.analytics.AnalyticsTracker
import com.fazlaka.app.core.model.dto.EpisodeDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.ui.accessibility.accessibleButton
import com.fazlaka.app.ui.components.ApiResultContent
import com.fazlaka.app.ui.components.DetailSkeleton
import com.fazlaka.app.ui.components.PosterImage
import com.fazlaka.app.ui.navigation.Routes
import com.fazlaka.app.ui.util.ShareUtils
import com.fazlaka.app.ui.util.formatDuration
import com.fazlaka.app.ui.util.localizedSeasonTitle
import com.fazlaka.app.ui.util.localizedTitle
import com.fazlaka.app.ui.viewmodel.EpisodeViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeDetailScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: EpisodeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.accessibleButton(label = stringResource(com.fazlaka.app.R.string.accessibility_back)),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val ep = (viewModel.state.value.episode as? com.fazlaka.app.core.network.ApiResult.Success)?.data
                            if (ep != null) {
                                val title = com.fazlaka.app.ui.util.localizedTitle(ep.translations, ep.slug)
                                ShareUtils.shareEpisode(context, ep.slug.ifBlank { ep.id }, title)
                            }
                        },
                        modifier = Modifier.accessibleButton(label = stringResource(com.fazlaka.app.R.string.accessibility_share)),
                    ) {
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
            result = state.episode,
            onRetry = { viewModel.load() },
            modifier = Modifier.padding(innerPadding),
            loadingContent = { DetailSkeleton(modifier = Modifier.padding(innerPadding)) },
        ) { episode ->
            EpisodeContent(
                episode = episode,
                related = (state.related as? ApiResult.Success)?.data?.data ?: emptyList(),
                liked = state.liked,
                likeBusy = state.likeBusy,
                resumePosition = state.resumePosition,
                viewModel = viewModel,
                onNavigate = onNavigate,
            )
        }
    }
}

@UnstableApi
@Composable
private fun EpisodeContent(
    episode: EpisodeDto,
    related: List<EpisodeDto>,
    liked: Boolean,
    likeBusy: Boolean,
    resumePosition: Int,
    viewModel: EpisodeViewModel,
    onNavigate: (String) -> Unit,
) {
    val context = LocalContext.current
    val title = localizedTitle(episode.translations, episode.slug)
    val mediaUrl = episode.videoUrl ?: episode.audioUrl

    val analyticsTracker = remember { AnalyticsTracker() }

    LaunchedEffect(episode.id) {
        if (mediaUrl != null) {
            analyticsTracker.logContentPlay(
                contentType = if (episode.videoUrl != null) "video" else "audio",
                contentId = episode.id,
                title = title,
            )
        }
    }

    val player = remember(mediaUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(mediaUrl ?: ""))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    LaunchedEffect(mediaUrl) {
        while (true) {
            kotlinx.coroutines.delay(10_000)
            if (player.duration > 0 && player.currentPosition > 0) {
                viewModel.saveProgress(episode.id, player.currentPosition.toInt(), episode.duration)
            }
        }
    }

    val resume = viewModel.state.value.resumePosition
    LaunchedEffect(episode.id, resume) {
        if (resume > 0) {
            player.seekTo(resume.toLong())
        }
    }

    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
    ) {
        item {
            if (mediaUrl != null) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            useController = true
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            )
                            setPlayer(player)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                )
            } else {
                PosterImage(
                    url = episode.coverImage,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                )
            }
        }
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = viewModel::toggleLike,
                        enabled = !likeBusy,
                        modifier = Modifier.accessibleButton(
                            label = if (liked) stringResource(com.fazlaka.app.R.string.accessibility_remove_like)
                                    else stringResource(com.fazlaka.app.R.string.accessibility_like),
                        ),
                    ) {
                        Icon(
                            imageVector = if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "إعجاب",
                            tint = if (liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = listOfNotNull(
                            episode.season?.let { localizedSeasonTitle(it.translations) },
                            episode.episodeNumber?.let { stringResource(com.fazlaka.app.R.string.episode_label, it) },
                            episode.duration?.let { formatDuration(it) },
                        ).joinToString(" • "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    if (resumePosition > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clickable { player.seekTo(resumePosition.toLong()) }
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    RoundedCornerShape(50),
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.height(16.dp),
                            )
                            Text(
                                text = stringResource(com.fazlaka.app.R.string.episodes_resume_fmt, formatDuration(resumePosition)),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f),
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Text(
                        text = episode.translations.firstOrNull { it.locale == "ar" }?.description
                            ?: episode.translations.firstOrNull()?.description
                            ?: stringResource(com.fazlaka.app.R.string.episodes_no_desc),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(20.dp),
                    )
                }
            }
        }
        if (related.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(com.fazlaka.app.R.string.episodes_related),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            itemsIndexed(related, key = { _, it -> it.id }, contentType = { _, _ -> "episode" }) { index, ep ->
                RelatedRow(
                    episode = ep,
                    onClick = {
                        onNavigate(Routes.episode(ep.slug.ifBlank { ep.id }))
                    },
                    modifier = Modifier
                        .animateItem()
                        .padding(bottom = if (index == related.lastIndex) 8.dp else 0.dp),
                )
            }
        }
    }
}

@Composable
private fun RelatedRow(episode: EpisodeDto, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val title = localizedTitle(episode.translations, episode.slug)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "relatedRowScale")
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick,
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
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
                    overflow = TextOverflow.Ellipsis,
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

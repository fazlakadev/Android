package com.fazlaka.app.ui.screens.messages

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fazlaka.app.core.model.dto.ConversationSummaryDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.ui.components.ApiResultContent
import com.fazlaka.app.ui.components.Avatar
import com.fazlaka.app.ui.components.HeroAccents
import com.fazlaka.app.ui.components.HeroSection
import com.fazlaka.app.ui.components.ListSkeleton
import com.fazlaka.app.ui.navigation.Routes
import com.fazlaka.app.ui.util.formatRelative
import com.fazlaka.app.ui.viewmodel.MessagesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    onNavigate: (String) -> Unit,
    viewModel: MessagesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PullToRefreshBox(
        isRefreshing = state.refreshing,
        onRefresh = { viewModel.load() },
        modifier = Modifier.fillMaxSize(),
    ) {
    ApiResultContent(
        result = state.conversations,
        onRetry = { viewModel.load() },
        emptyTitle = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.msgs_empty),
        emptySubtitle = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.msgs_empty_sub),
        loadingContent = { ListSkeleton(modifier = Modifier.statusBarsPadding()) },
    ) { page ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
        ) {
            item {
                HeroSection(
                    title = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.msgs_title),
                    subtitle = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.msgs_subtitle),
                    badge = "💬 ${page.meta.total} محادثة",
                    accent = HeroAccents.Messages,
                    minHeight = 140.dp,
                    fullscreenTop = true,
                )
                Spacer(Modifier.height(8.dp))
            }
            itemsIndexed(page.data, key = { _, it -> it.id }, contentType = { _, _ -> "conversation" }) { index, conversation ->
                ConversationRow(
                    conversation = conversation,
                    modifier = Modifier.animateItem(
                        fadeInSpec = tween(400, delayMillis = (index % 8) * 50, easing = LinearOutSlowInEasing),
                    ),
                    onClick = {
                        onNavigate(Routes.chat(conversation.id))
                    },
                )
            }
        }
    }
    }
}

@Composable
private fun ConversationRow(conversation: ConversationSummaryDto, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val isGroup = conversation.kind == "group"
    val displayName = when {
        isGroup -> conversation.group?.name ?: stringResource(com.fazlaka.app.R.string.msg_group)
        else -> conversation.other?.name ?: conversation.other?.username ?: stringResource(com.fazlaka.app.R.string.msg_user)
    }
    val avatarUrl = when {
        isGroup -> conversation.group?.avatarUrl
        else -> conversation.other?.avatarUrl
    }
    val msgImageLabel = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.chat_msg_image)
    val msgVideoLabel = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.chat_msg_video)
    val msgVoiceLabel = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.chat_msg_voice)
    val lastBody = conversation.lastMessage?.let { msg ->
        when (msg.type) {
            "image" -> msgImageLabel + if (msg.body.isNotBlank()) " · ${msg.body}" else ""
            "video" -> msgVideoLabel + if (msg.body.isNotBlank()) " · ${msg.body}" else ""
            "audio" -> msgVoiceLabel
            else -> msg.body
        }
    } ?: ""
    val time = remember(conversation.lastMessage?.createdAt, conversation.updatedAt) {
        formatRelative(conversation.lastMessage?.createdAt ?: conversation.updatedAt)
    }

    Surface(
        modifier = modifier
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
            Avatar(url = avatarUrl, name = displayName, size = 48)
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = lastBody,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (conversation.unreadCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        androidx.compose.material3.Badge(
                            modifier = Modifier.size(20.dp, 16.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                        ) {
                            Text("${conversation.unreadCount}")
                        }
                    }
                }
            }
        }
    }
}

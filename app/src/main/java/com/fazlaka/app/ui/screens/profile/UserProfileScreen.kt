package com.fazlaka.app.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fazlaka.app.core.model.dto.PublicProfileDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.ui.components.ApiResultContent
import com.fazlaka.app.ui.components.Avatar
import com.fazlaka.app.ui.components.ProfileSkeleton
import com.fazlaka.app.ui.viewmodel.MessageStartState
import com.fazlaka.app.ui.viewmodel.UserProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onBack: () -> Unit,
    onOpenConversation: (String) -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val relation by viewModel.relation.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val toast by viewModel.toast.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    val convErrorMessage = stringResource(com.fazlaka.app.R.string.up_conv_error)
    LaunchedEffect(message) {
        when (message) {
            is MessageStartState.Ready -> {
                val id = (message as MessageStartState.Ready).conversationId
                viewModel.resetMessage()
                onOpenConversation(id)
            }
            MessageStartState.Failed -> {
                snackbar.showSnackbar(convErrorMessage)
                viewModel.resetMessage()
            }
            else -> Unit
        }
    }
    LaunchedEffect(toast) {
        toast?.let { snackbar.showSnackbar(it); viewModel.clearToast() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
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
            loadingContent = { ProfileSkeleton(modifier = Modifier.padding(innerPadding)) },
        ) { profile ->
            PublicProfileContent(
                profile = profile,
                busy = busy || message is MessageStartState.Loading,
                relationStatus = relation?.status ?: "none",
                relationIncoming = relation?.incoming == true,
                onMessage = viewModel::startConversation,
                onAddFriend = viewModel::addFriend,
                onRespond = viewModel::respondToRequest,
                onRemoveFriend = viewModel::removeFriend,
                onToggleBlock = viewModel::toggleBlock,
            )
        }
        SnackbarHost(hostState = snackbar, modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun PublicProfileContent(
    profile: PublicProfileDto,
    busy: Boolean,
    relationStatus: String,
    relationIncoming: Boolean,
    onMessage: () -> Unit,
    onAddFriend: () -> Unit,
    onRespond: (Boolean) -> Unit,
    onRemoveFriend: () -> Unit,
    onToggleBlock: () -> Unit,
) {
    val blocked = relationStatus == "blocked"
    val friends = relationStatus == "accepted"
    val pendingIncoming = relationStatus == "pending" && relationIncoming
    val pendingOutgoing = relationStatus == "pending" && !relationIncoming

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))
        Avatar(url = profile.avatarUrl, name = profile.name, size = 80)
        Spacer(Modifier.height(12.dp))
        Text(profile.name, style = MaterialTheme.typography.headlineMedium)
        Text(
            "@${profile.username}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!profile.bio.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = profile.bio,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Stat(stringResource(com.fazlaka.app.R.string.up_stats_friends), profile.stats.friendsCount)
            Stat(stringResource(com.fazlaka.app.R.string.up_stats_ratings), profile.stats.ratingsCount)
            Stat(stringResource(com.fazlaka.app.R.string.up_stats_articles), profile.stats.articlesCount)
        }
        Spacer(Modifier.height(24.dp))

        when {
            blocked -> {
                OutlinedButton(
                    onClick = onToggleBlock,
                    enabled = !busy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) { Text(stringResource(com.fazlaka.app.R.string.up_unblock)) }
            }
            friends -> {
                Button(
                    onClick = onMessage,
                    enabled = !busy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.width(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(stringResource(com.fazlaka.app.R.string.up_send_message))
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onRemoveFriend,
                        enabled = !busy,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                    ) { Text(stringResource(com.fazlaka.app.R.string.up_remove_friend)) }
                    OutlinedButton(
                        onClick = onToggleBlock,
                        enabled = !busy,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                    ) { Text(stringResource(com.fazlaka.app.R.string.up_block), color = MaterialTheme.colorScheme.error) }
                }
            }
            pendingIncoming -> {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onRespond(true) },
                        enabled = !busy,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                    ) { Text(stringResource(com.fazlaka.app.R.string.up_accept_friend)) }
                    OutlinedButton(
                        onClick = { onRespond(false) },
                        enabled = !busy,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                    ) { Text(stringResource(com.fazlaka.app.R.string.up_reject)) }
                }
            }
            pendingOutgoing -> {
                OutlinedButton(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) { Text(stringResource(com.fazlaka.app.R.string.up_pending)) }
                if (!busy) {
                    Button(
                        onClick = onMessage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                    ) { Text(stringResource(com.fazlaka.app.R.string.up_send_message)) }
                }
            }
            else -> {
                Button(
                    onClick = onAddFriend,
                    enabled = !busy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) { Text(stringResource(com.fazlaka.app.R.string.up_add_friend)) }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onToggleBlock,
                    enabled = !busy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                ) { Text(stringResource(com.fazlaka.app.R.string.up_block), color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", style = MaterialTheme.typography.titleLarge)
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

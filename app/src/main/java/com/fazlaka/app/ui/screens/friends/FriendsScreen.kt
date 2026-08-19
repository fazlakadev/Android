package com.fazlaka.app.ui.screens.friends

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fazlaka.app.core.model.dto.FriendRequestDto
import com.fazlaka.app.core.model.dto.FriendUserDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.ui.components.ApiResultContent
import com.fazlaka.app.ui.components.Avatar
import com.fazlaka.app.ui.components.GlassCard
import com.fazlaka.app.ui.components.HeroAccents
import com.fazlaka.app.ui.components.HeroSection
import com.fazlaka.app.ui.components.ListSkeleton
import com.fazlaka.app.ui.components.ModernSearchBar
import com.fazlaka.app.ui.components.glowShadow
import com.fazlaka.app.ui.navigation.Routes
import com.fazlaka.app.ui.theme.FazlakaGradientStart
import com.fazlaka.app.ui.viewmodel.FriendsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: FriendsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var tab by remember { mutableIntStateOf(0) }
    val reqLabel = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_tab_requests)
    var requestSubTab by remember { mutableIntStateOf(0) }
    var blockTarget by remember { mutableStateOf<FriendUserDto?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize(),
    ) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            HeroSection(
                title = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_tab_friends),
                subtitle = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_search_hero_sub),
                badge = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_friends_fmt, (state.friends as? ApiResult.Success)?.data?.meta?.total ?: 0),
                accent = HeroAccents.Friends,
                minHeight = 140.dp,
                fullscreenTop = true,
            )

            // Search
            ModernSearchBar(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchChange,
                placeholder = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_search_hint),
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )

            TabRow(
                selectedTabIndex = tab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier
                            .height(3.dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Tab(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    text = { Text(androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_tab_friends), style = MaterialTheme.typography.bodyMedium) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Tab(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    text = {
                        Text(
                            reqLabel + incomingCount(state)?.let { " ($it)" }.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Tab(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    text = { Text(androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_tab_suggestions), style = MaterialTheme.typography.bodyMedium) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            val searching = state.searchQuery.isNotBlank()
            when {
                searching -> SearchResultsTab(state, viewModel, onNavigate)
                tab == 0 -> FriendsTab(state, viewModel, onNavigate) { blockTarget = it }
                tab == 1 -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChipSmall(
                            label = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_incoming),
                            selected = requestSubTab == 0,
                            modifier = Modifier.weight(1f),
                        ) { requestSubTab = 0 }
                        FilterChipSmall(
                            label = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_outgoing),
                            selected = requestSubTab == 1,
                            modifier = Modifier.weight(1f),
                        ) { requestSubTab = 1 }
                    }
                    if (requestSubTab == 0) IncomingRequestsTab(state, viewModel)
                    else OutgoingRequestsTab(state, viewModel)
                }
                else -> SuggestionsTab(state, viewModel, onNavigate) { blockTarget = it }
                }
            }
        }
    }

    blockTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { blockTarget = null },
            title = { Text(androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_block_title, target.name)) },
            text = { Text(androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_block_text)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.block(target.id)
                        blockTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) { Text(androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_block)) }
            },
            dismissButton = {
                TextButton(onClick = { blockTarget = null }) { Text(androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_cancel)) }
            },
        )
    }
}

private fun incomingCount(state: com.fazlaka.app.ui.viewmodel.FriendsUiState): Int? =
    (state.incoming as? ApiResult.Success)?.data?.meta?.total?.takeIf { it > 0 }

@Composable
private fun FilterChipSmall(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        ),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(vertical = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun SearchResultsTab(
    state: com.fazlaka.app.ui.viewmodel.FriendsUiState,
    viewModel: FriendsViewModel,
    onNavigate: (String) -> Unit,
) {
    when {
        state.searching -> Box(
            Modifier.fillMaxWidth().padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(26.dp))
        }
        state.searchResults is ApiResult.Failure -> Text(
            text = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_search_error),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
        )
        else -> {
            val results = (state.searchResults as? ApiResult.Success)?.data.orEmpty()
            if (results.isEmpty()) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_no_results),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    itemsIndexed(results, key = { _, it -> it.id }, contentType = { _, _ -> "friendUser" }) { index, user ->
                        FriendRow(
                            friend = user,
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(
                                    400,
                                    delayMillis = (index % 8) * 50,
                                    easing = LinearOutSlowInEasing,
                                ),
                            ),
                            onOpenProfile = { onNavigate(Routes.userProfile(user.id)) },
                            onMessage = { viewModel.openConversation(user.id) { onNavigate(Routes.chat(it)) } },
                            onAdd = { viewModel.sendRequest(user.id) },
                            onRemove = null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendsTab(
    state: com.fazlaka.app.ui.viewmodel.FriendsUiState,
    viewModel: FriendsViewModel,
    onNavigate: (String) -> Unit,
    onBlock: (FriendUserDto) -> Unit,
) {
    ApiResultContent(
        result = state.friends,
        onRetry = { viewModel.load() },
        emptyTitle = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_empty),
        emptySubtitle = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_empty_sub),
        loadingContent = { ListSkeleton() },
    ) { page ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            itemsIndexed(page.data, key = { _, it -> it.id }, contentType = { _, _ -> "friendUser" }) { index, friend ->
                FriendRow(
                    friend = friend,
                    modifier = Modifier.animateItem(
                        fadeInSpec = tween(
                            400,
                            delayMillis = (index % 8) * 50,
                            easing = LinearOutSlowInEasing,
                        ),
                    ),
                    onOpenProfile = { onNavigate(Routes.userProfile(friend.id)) },
                    onMessage = { viewModel.openConversation(friend.id) { onNavigate(Routes.chat(it)) } },
                    onAdd = null,
                    onRemove = { viewModel.remove(friend.id) },
                    onBlock = { onBlock(friend) },
                )
            }
        }
    }
}

@Composable
private fun IncomingRequestsTab(
    state: com.fazlaka.app.ui.viewmodel.FriendsUiState,
    viewModel: FriendsViewModel,
) {
    ApiResultContent(
        result = state.incoming,
        onRetry = { viewModel.load() },
        emptyTitle = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_requests_empty),
        loadingContent = { ListSkeleton() },
    ) { page ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            itemsIndexed(page.data, key = { _, it -> it.id }, contentType = { _, _ -> "friendRequest" }) { index, request ->
                RequestRow(
                    request = request,
                    incoming = true,
                    modifier = Modifier.animateItem(
                        fadeInSpec = tween(
                            400,
                            delayMillis = (index % 8) * 50,
                            easing = LinearOutSlowInEasing,
                        ),
                    ),
                    viewModel = viewModel,
                )
            }
        }
    }
}

@Composable
private fun OutgoingRequestsTab(
    state: com.fazlaka.app.ui.viewmodel.FriendsUiState,
    viewModel: FriendsViewModel,
) {
    ApiResultContent(
        result = state.outgoing,
        onRetry = { viewModel.load() },
        emptyTitle = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_outgoing_empty),
        emptySubtitle = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_outgoing_empty_sub),
        loadingContent = { ListSkeleton() },
    ) { page ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            itemsIndexed(page.data, key = { _, it -> it.id }, contentType = { _, _ -> "friendRequest" }) { index, request ->
                RequestRow(
                    request = request,
                    incoming = false,
                    modifier = Modifier.animateItem(
                        fadeInSpec = tween(
                            400,
                            delayMillis = (index % 8) * 50,
                            easing = LinearOutSlowInEasing,
                        ),
                    ),
                    viewModel = viewModel,
                )
            }
        }
    }
}

@Composable
private fun SuggestionsTab(
    state: com.fazlaka.app.ui.viewmodel.FriendsUiState,
    viewModel: FriendsViewModel,
    onNavigate: (String) -> Unit,
    onBlock: (FriendUserDto) -> Unit,
) {
    ApiResultContent(
        result = state.suggestions,
        onRetry = { viewModel.load() },
        emptyTitle = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_sugg_empty),
        loadingContent = { ListSkeleton() },
    ) { suggestions ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            itemsIndexed(suggestions, key = { _, it -> it.id }, contentType = { _, _ -> "friendUser" }) { index, friend ->
                FriendRow(
                    friend = friend,
                    modifier = Modifier.animateItem(
                        fadeInSpec = tween(
                            400,
                            delayMillis = (index % 8) * 50,
                            easing = LinearOutSlowInEasing,
                        ),
                    ),
                    onOpenProfile = { onNavigate(Routes.userProfile(friend.id)) },
                    onMessage = null,
                    onAdd = { viewModel.sendRequest(friend.id) },
                    onRemove = null,
                    onBlock = { onBlock(friend) },
                )
            }
        }
    }
}

@Composable
private fun FriendRow(
    friend: FriendUserDto,
    modifier: Modifier = Modifier,
    onOpenProfile: () -> Unit,
    onMessage: (() -> Unit)?,
    onAdd: (() -> Unit)?,
    onRemove: (() -> Unit)?,
    onBlock: (() -> Unit)? = null,
) {
    var menuOpen by remember { mutableStateOf(false) }
    GlassCard(
        modifier = modifier
            .glowShadow(elevation = 7.dp, glowColor = FazlakaGradientStart.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenProfile)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Avatar(url = friend.avatarUrl, name = friend.name, size = 46)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    friend.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "@${friend.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (onMessage != null) {
                IconButton(onClick = onMessage, modifier = Modifier.size(38.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.Chat,
                        contentDescription = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_message),
                        tint = FazlakaGradientStart,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            if (onAdd != null) {
                Button(
                    onClick = onAdd,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_add), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
            }
            if (onRemove != null || onBlock != null) {
                Box {
                    IconButton(
                        onClick = { menuOpen = true },
                        modifier = Modifier.size(38.dp),
                    ) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = "خيارات",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        if (onRemove != null) {
                            DropdownMenuItem(
                                text = { Text(androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_remove)) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                onClick = { menuOpen = false; onRemove() },
                            )
                        }
                        if (onBlock != null) {
                            DropdownMenuItem(
                                text = { Text(androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_block), color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Block,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                                onClick = { menuOpen = false; onBlock() },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestRow(
    request: FriendRequestDto,
    incoming: Boolean,
    modifier: Modifier = Modifier,
    viewModel: FriendsViewModel,
) {
    val person = if (incoming) request.sender else request.receiver
    GlassCard(modifier = modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Avatar(url = person?.avatarUrl, name = person?.name ?: "؟", size = 46)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    person?.name ?: androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_user),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "@${person?.username ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (incoming) {
                Button(
                    onClick = { viewModel.accept(request.id) },
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_accept), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.width(6.dp))
                OutlinedButton(
                    onClick = { viewModel.reject(request.id) },
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Text(androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_reject), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Icon(
                    Icons.Filled.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_pending),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

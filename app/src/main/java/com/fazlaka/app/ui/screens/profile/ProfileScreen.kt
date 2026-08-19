package com.fazlaka.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fazlaka.app.R
import com.fazlaka.app.ui.components.Avatar
import com.fazlaka.app.ui.components.ProfileSkeleton
import com.fazlaka.app.ui.navigation.Routes
import com.fazlaka.app.ui.theme.FazlakaCyan
import com.fazlaka.app.ui.theme.FazlakaGradientMid
import com.fazlaka.app.ui.theme.FazlakaGradientStart
import com.fazlaka.app.ui.util.ShareUtils
import com.fazlaka.app.ui.viewmodel.AuthViewModel
import com.fazlaka.app.ui.viewmodel.ProfileViewModel

/**
 * Profile tab — personal activity only (history, playlists, likes, friends...).
 * App preferences (dark mode, language, security, support) live in Settings,
 * reachable from the gear icon in the header.
 */

@Composable
fun ProfileScreen(
    onNavigate: (String) -> Unit,
    onLoggedOut: () -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val state by profileViewModel.state.collectAsStateWithLifecycle()
    val user by profileViewModel.userFlow.collectAsStateWithLifecycle(initialValue = null)
    val stats by profileViewModel.stats.collectAsStateWithLifecycle()
    var showPhoto by remember { mutableStateOf(false) }

    if (user == null) {
        ProfileSkeleton()
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        item {
            val fullUser = (state.me as? com.fazlaka.app.core.network.ApiResult.Success)?.data
            ProfileHeader(
                name = user?.name ?: "",
                username = user?.username ?: "",
                bio = fullUser?.bio,
                bannerUrl = fullUser?.bannerUrl,
                avatarUrl = user?.avatarUrl,
                stats = stats,
                onAvatarClick = { showPhoto = true },
                onEdit = { onNavigate(Routes.EDIT_PROFILE) },
                onMore = onNavigate,
            )
        }

        item {
            Spacer(Modifier.height(20.dp))
            SectionLabel(stringResource(R.string.sec_account))
            MenuGroup(
                items = listOf(
                    MenuItem(Icons.Default.Edit, stringResource(R.string.pf_edit_profile), stringResource(R.string.pf_edit_profile_sub)) { onNavigate(Routes.EDIT_PROFILE) },
                    MenuItem(Icons.Default.Security, stringResource(R.string.pf_security), stringResource(R.string.pf_security_sub)) { onNavigate(Routes.SECURITY) },
                    MenuItem(Icons.Default.Email, stringResource(R.string.pf_email), stringResource(R.string.pf_email_sub)) { onNavigate(Routes.SECONDARY_EMAILS) },
                    MenuItem(Icons.Default.Link, stringResource(R.string.pf_linked), stringResource(R.string.pf_linked_sub)) { onNavigate(Routes.LINKED_ACCOUNTS) },
                    MenuItem(Icons.Default.HistoryEdu, stringResource(R.string.pf_activity_log), stringResource(R.string.pf_activity_log_sub)) { onNavigate(Routes.ACTIVITY_LOG) },
                ),
            )
        }

        item {
            Spacer(Modifier.height(14.dp))
            SectionLabel(stringResource(R.string.sec_activity))
            MenuGroup(
                items = listOf(
                    MenuItem(Icons.Default.History, stringResource(R.string.pf_history), stringResource(R.string.pf_history_sub)) { onNavigate(Routes.VIEW_HISTORY) },
                    MenuItem(Icons.Default.PlaylistPlay, stringResource(R.string.pf_playlists), stringResource(R.string.pf_playlists_sub)) { onNavigate(Routes.MY_PLAYLISTS) },
                    MenuItem(Icons.Default.Favorite, stringResource(R.string.pf_likes), stringResource(R.string.pf_likes_sub)) { onNavigate(Routes.LIKES_HISTORY) },
                ),
            )
        }

        item {
            Spacer(Modifier.height(14.dp))
            SectionLabel(stringResource(R.string.sec_community))
            MenuGroup(
                items = listOf(
                    MenuItem(Icons.Default.Group, stringResource(R.string.pf_friends), stringResource(R.string.pf_friends_sub)) { onNavigate(Routes.FRIENDS) },
                    MenuItem(Icons.Default.Notifications, stringResource(R.string.pf_notifications), stringResource(R.string.pf_notifications_sub)) { onNavigate(Routes.NOTIFICATIONS) },
                    MenuItem(Icons.Default.PersonAdd, stringResource(R.string.pf_referral), stringResource(R.string.pf_referral_sub)) { onNavigate(Routes.REFERRALS) },
                ),
            )
        }

        item {
            Spacer(Modifier.height(14.dp))
            SectionLabel(stringResource(R.string.sec_about))
            MenuGroup(
                items = listOf(
                    MenuItem(Icons.Default.Settings, stringResource(R.string.pf_settings), stringResource(R.string.pf_settings_sub)) { onNavigate(Routes.SETTINGS) },
                    MenuItem(Icons.Default.PrivacyTip, stringResource(R.string.pf_privacy), stringResource(R.string.pf_privacy_sub)) { onNavigate(Routes.PRIVACY_POLICY) },
                    MenuItem(Icons.Default.SupportAgent, stringResource(R.string.pf_support), stringResource(R.string.pf_support_sub)) { onNavigate(Routes.SUPPORT) },
                ),
            )
        }

        item {
            Spacer(Modifier.height(16.dp))
            Surface(
                onClick = { authViewModel.logout { onLoggedOut() } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = stringResource(R.string.pf_logout),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }

    if (showPhoto) {
        PhotoViewerDialog(
            avatarUrl = user?.avatarUrl,
            name = user?.name ?: stringResource(com.fazlaka.app.R.string.up_user),
            username = user?.username ?: "",
            onDismiss = { showPhoto = false },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 20.dp, height = 6.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    Brush.horizontalGradient(listOf(FazlakaGradientStart, FazlakaCyan)),
                ),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}


private data class MenuItem(
    val icon: ImageVector,
    val label: String,
    val subtitle: String,
    val onClick: () -> Unit,
)


@Composable
private fun MenuGroup(items: List<MenuItem>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        ),
    ) {
        Column {
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 66.dp, end = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
                    )
                }
                ProfileMenuRow(icon = item.icon, label = item.label, subtitle = item.subtitle, onClick = item.onClick)
            }
        }
    }
}


@Composable
private fun ProfileHeader(
    name: String,
    username: String,
    bio: String?,
    bannerUrl: String?,
    avatarUrl: String?,
    stats: com.fazlaka.app.core.model.dto.PublicProfileStatsDto?,
    onAvatarClick: () -> Unit,
    onEdit: () -> Unit,
    onMore: (String) -> Unit,
) {
    val context = LocalContext.current
    // Living brand gradient with the user's banner blended on top when set.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        FazlakaGradientStart.copy(alpha = 0.34f),
                        FazlakaCyan.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            ),
    ) {
        if (!bannerUrl.isNullOrBlank()) {
            coil.compose.AsyncImage(
                model = bannerUrl,
                contentDescription = "الغلاف",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .alpha(0.55f),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                        ),
                    ),
            )
        }
        // More menu shortcut — settings, security, privacy, support…
        com.fazlaka.app.ui.components.MoreMenuButton(
            onNavigate = onMore,
            onDarkSurface = true,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(10.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onAvatarClick)
                    .border(3.dp, FazlakaCyan.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Avatar(url = avatarUrl, name = name, size = 110)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(FazlakaGradientStart, FazlakaGradientMid)),
                        )
                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = name.ifEmpty { stringResource(com.fazlaka.app.R.string.up_user) },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "@${username.ifEmpty { "user" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!bio.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 28.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            if (stats != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileStat("${stats.friendsCount}", stringResource(R.string.stat_friends))
                    ProfileStat("${stats.ratingsCount}", stringResource(R.string.stat_ratings))
                    ProfileStat("${stats.playlistsCount}", stringResource(R.string.stat_playlists))
                    ProfileStat("${stats.articlesCount}", stringResource(R.string.stat_articles))
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    onClick = onEdit,
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.pf_edit_file),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    )
                }
                Surface(
                    onClick = {
                        ShareUtils.shareProfile(context, username, name.ifEmpty { username })
                    },
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "مشاركة",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}


@Composable
private fun ProfileStat(value: String, label: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}


@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronLeft,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.size(18.dp),
        )
    }
}


@Composable
private fun PhotoViewerDialog(
    avatarUrl: String?,
    name: String,
    username: String,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.93f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp),
            ) {
                Avatar(url = avatarUrl, name = name, size = 260)
                Spacer(Modifier.height(24.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "@$username",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(28.dp))
                Text(
                    text = stringResource(com.fazlaka.app.R.string.up_close),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f),
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f)),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "إغلاق",
                    tint = Color.White,
                )
            }
        }
    }
}

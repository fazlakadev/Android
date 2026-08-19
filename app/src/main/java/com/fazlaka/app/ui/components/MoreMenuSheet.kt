package com.fazlaka.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fazlaka.app.BuildConfig
import com.fazlaka.app.ui.navigation.Routes
import com.fazlaka.app.ui.theme.FazlakaCyan
import com.fazlaka.app.ui.theme.FazlakaGradientMid
import com.fazlaka.app.ui.theme.FazlakaGradientStart

private data class MoreItem(
    val icon: ImageVector,
    @androidx.annotation.StringRes val titleRes: Int,
    @androidx.annotation.StringRes val subtitleRes: Int,
    val route: String?,
)

private val moreItems = listOf(
    MoreItem(Icons.Filled.Settings, com.fazlaka.app.R.string.more_settings, com.fazlaka.app.R.string.more_settings_sub, Routes.SETTINGS),
    MoreItem(Icons.Filled.Devices, com.fazlaka.app.R.string.more_security, com.fazlaka.app.R.string.more_security_sub, Routes.SECURITY),
    MoreItem(Icons.Filled.History, com.fazlaka.app.R.string.more_activity, com.fazlaka.app.R.string.more_activity_sub, Routes.ACTIVITY_LOG),
    MoreItem(Icons.Filled.PrivacyTip, com.fazlaka.app.R.string.more_privacy, com.fazlaka.app.R.string.more_privacy_sub, Routes.PRIVACY_POLICY),
    MoreItem(Icons.Filled.Description, com.fazlaka.app.R.string.more_terms, com.fazlaka.app.R.string.more_terms_sub, Routes.TERMS),
    MoreItem(Icons.Filled.SupportAgent, com.fazlaka.app.R.string.more_support, com.fazlaka.app.R.string.more_support_sub, Routes.SUPPORT),
)

/**
 * "المزيد" — floating pill button that opens a rich bottom-sheet menu with
 * everything beyond the main tabs (settings, privacy, terms, support…).
 */
@Composable
fun MoreMenuButton(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    onDarkSurface: Boolean = false,
) {
    var open by remember { mutableStateOf(false) }

    IconButton(
        onClick = { open = true },
        modifier = modifier
            .size(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (onDarkSurface) {
                    Color.Black.copy(alpha = 0.28f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f)
                }
            ),
    ) {
        Icon(
            imageVector = Icons.Filled.MoreHoriz,
            contentDescription = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.tab_more),
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
    }

    if (open) {
        MoreMenuSheet(
            onDismiss = { open = false },
            onNavigate = { route ->
                open = false
                onNavigate(route)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreMenuSheet(
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(FazlakaGradientStart, FazlakaGradientMid),
                        ),
                    )
                    .padding(16.dp),
            ) {
                Column {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.more_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.more_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            moreItems.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { item.route?.let(onNavigate) }
                        .padding(horizontal = 12.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(21.dp),
                        )
                    }
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(item.titleRes),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = androidx.compose.ui.res.stringResource(item.subtitleRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
                Spacer(Modifier.height(4.dp))
            }

            // About footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = FazlakaCyan,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.more_about, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

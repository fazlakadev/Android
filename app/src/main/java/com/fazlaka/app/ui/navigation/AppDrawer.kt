package com.fazlaka.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fazlaka.app.ui.theme.FazlakaCyan
import com.fazlaka.app.ui.theme.FazlakaGradientMid
import com.fazlaka.app.ui.theme.FazlakaGradientStart
import com.fazlaka.app.ui.viewmodel.MainViewModel

@Composable
fun AppDrawer(
    onNavigate: (String) -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(12.dp))

        // Brand header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        listOf(FazlakaGradientStart, FazlakaGradientMid),
                    ),
                )
                .padding(20.dp),
        ) {
            Column {
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "فذلكة",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "المحتوى العربي في مكان واحد",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Content section
        DrawerSectionLabel("المحتوى")

        DrawerItem(
            icon = Icons.Default.Article,
            label = "المقالات",
            subtitle = "اقرأ أحدث المقالات والمدونات",
            onClick = { onNavigate(Routes.ARTICLE.replace("{idOrSlug}", "latest")) },
        )
        DrawerItem(
            icon = Icons.Default.List,
            label = "الحلقات",
            subtitle = "تابع أحدث الحلقات المتوفرة",
            onClick = { onNavigate(Routes.ALL_EPISODES) },
        )
        DrawerItem(
            icon = Icons.Default.Headphones,
            label = "المواسم",
            subtitle = "استكشف جميع المواسم",
            onClick = { onNavigate(Routes.SEASONS) },
        )
        DrawerItem(
            icon = Icons.Default.PlaylistPlay,
            label = "قوائم التشغيل",
            subtitle = "قوائمك المفضلة والمحفوظة",
            onClick = { onNavigate(Routes.MY_PLAYLISTS) },
        )
        DrawerItem(
            icon = Icons.Default.Mail,
            label = "الرسائل",
            subtitle = "محادثاتك الخاصة والمجموعات",
            onClick = { onNavigate(Routes.MESSAGES) },
        )

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        )
        Spacer(Modifier.height(8.dp))

        // Info section
        DrawerSectionLabel("معلومات")

        DrawerItem(
            icon = Icons.Default.PrivacyTip,
            label = "سياسة الخصوصية",
            subtitle = "كيف نتعامل مع بياناتك",
            onClick = { onNavigate(Routes.PRIVACY_POLICY) },
        )
        DrawerItem(
            icon = Icons.Default.Description,
            label = "شروط الاستخدام",
            subtitle = "قواعد استخدام فذلكة",
            onClick = { onNavigate(Routes.TERMS) },
        )

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        )
        Spacer(Modifier.height(8.dp))

        // Dark mode toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { viewModel.setDarkMode(!state.darkMode) }
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(
                imageVector = if (state.darkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (state.darkMode) "الوضع الداكن" else "الوضع الفاتح",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "اضغط للتبديل",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = if (state.darkMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(44.dp, 24.dp),
            ) {
                Box(
                    modifier = Modifier.padding(2.dp),
                    contentAlignment = if (state.darkMode) Alignment.CenterEnd else Alignment.CenterStart,
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                    ) {}
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DrawerSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
    )
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(21.dp),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

package com.fazlaka.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgeDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fazlaka.app.ui.theme.FazlakaCyan

@Composable
fun AppTopBar(
    onOpenDrawer: () -> Unit,
    onSearch: () -> Unit,
    onFriends: () -> Unit,
    friendRequestCount: Int = 0,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Hamburger menu
        IconButton(onClick = onOpenDrawer) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "القائمة",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(26.dp),
            )
        }

        // App title
        Text(
            text = "فذلكة",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp),
        )

        // Search icon
        IconButton(onClick = onSearch) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "بحث",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(21.dp),
                )
            }
        }

        Spacer(Modifier.width(4.dp))

        // Friends icon
        IconButton(onClick = onFriends) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "الأصدقاء",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(21.dp),
                )
                if (friendRequestCount > 0) {
                    Badge(
                        containerColor = FazlakaCyan,
                        contentColor = Color(0xFF06202A),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(0.dp),
                    ) {
                        Text(
                            text = if (friendRequestCount > 99) "99+" else "$friendRequestCount",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.width(4.dp))
    }
}

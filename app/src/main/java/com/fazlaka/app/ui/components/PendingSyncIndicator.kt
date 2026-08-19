package com.fazlaka.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.fazlaka.app.core.network.OfflineManager

@Composable
fun PendingSyncIndicator(offlineManager: OfflineManager) {
    val count by offlineManager.pendingCount.collectAsStateWithLifecycle()
    val alpha by animateFloatAsState(if (count > 0) 1f else 0f, label = "pendingAlpha")
    val desc = if (count > 0) "$count pending sync" else ""

    Box(
        modifier = Modifier
            .size(8.dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.tertiary)
            .semantics { contentDescription = desc },
    )
}

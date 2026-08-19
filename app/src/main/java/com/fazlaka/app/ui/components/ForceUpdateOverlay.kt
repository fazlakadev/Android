package com.fazlaka.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fazlaka.app.core.update.UpdateInfo
import com.fazlaka.app.ui.theme.FazlakaBackground
import com.fazlaka.app.ui.theme.FazlakaGradientDeep
import com.fazlaka.app.ui.theme.FazlakaSuccess
import com.fazlaka.app.ui.theme.FazlakaWarning

@Composable
fun ForceUpdateOverlay(
    updateInfo: UpdateInfo,
    currentVersion: String,
    isDownloading: Boolean,
    downloadProgress: Int,
    onDownload: () -> Unit,
) {
    val progress by animateFloatAsState(
        targetValue = downloadProgress / 100f,
        animationSpec = tween(300),
        label = "download_progress",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FazlakaGradientDeep),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = FazlakaWarning.copy(alpha = 0.15f),
                modifier = Modifier.size(100.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "⬆️",
                        style = MaterialTheme.typography.displayMedium,
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "تحديث مطلوب",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "الإصدار الحالي $currentVersion قديم.\nيجب التحديث للمتابعة.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "الإصدار الجديد: ${updateInfo.version}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = FazlakaWarning,
            )

            if (!updateInfo.forceUpdateMessage.isNullOrBlank()) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FazlakaWarning.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = updateInfo.forceUpdateMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = FazlakaWarning,
                        modifier = Modifier.padding(14.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            AnimatedVisibility(visible = isDownloading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "جارٍ التحميل… $downloadProgress%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = FazlakaWarning,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            if (!isDownloading) {
                Button(
                    onClick = onDownload,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FazlakaWarning,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Text(
                        text = "تحميل وتثبيت",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (isDownloading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp,
                    color = FazlakaWarning,
                )
            }
        }
    }
}

package com.fazlaka.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fazlaka.app.ui.theme.FazlakaCyan
import com.fazlaka.app.ui.theme.FazlakaCyanDeep
import com.fazlaka.app.ui.theme.FazlakaGlow
import com.fazlaka.app.ui.theme.FazlakaGradientDeep
import com.fazlaka.app.ui.theme.FazlakaGradientEnd
import com.fazlaka.app.ui.theme.FazlakaGradientMid
import com.fazlaka.app.ui.theme.FazlakaGradientStart
import kotlin.math.cos
import kotlin.math.sin

/** Per-page accent used by [HeroSection] so every screen gets its own
 *  signature tint while staying inside the Fazlaka brand family. */
data class HeroAccent(
    val primary: Color,
    val secondary: Color,
    val glow: Color,
)

object HeroAccents {
    val Home = HeroAccent(FazlakaGradientStart, FazlakaCyan, FazlakaCyan)
    val Seasons = HeroAccent(Color(0xFF8B5CF6), Color(0xFFD946EF), Color(0xFFD946EF))
    val Search = HeroAccent(FazlakaCyanDeep, FazlakaCyan, FazlakaCyan)
    val Messages = HeroAccent(Color(0xFF6366F1), FazlakaCyan, FazlakaCyan)
    val Friends = HeroAccent(Color(0xFF8B5CF6), Color(0xFF22D3EE), Color(0xFFF59E0B))
    val Profile = HeroAccent(FazlakaGradientStart, FazlakaGradientMid, FazlakaGlow)
    val Security = HeroAccent(Color(0xFF0EA5E9), Color(0xFF8B5CF6), Color(0xFF0EA5E9))
    val Notifications = HeroAccent(Color(0xFFF59E0B), Color(0xFF8B5CF6), FazlakaGlow)
    val Settings = HeroAccent(Color(0xFF64748B), FazlakaCyan, FazlakaCyan)
    val Support = HeroAccent(Color(0xFF10B981), FazlakaCyan, Color(0xFF10B981))
    val Library = HeroAccent(Color(0xFF8B5CF6), Color(0xFFF59E0B), FazlakaGlow)
}

/**
 * Living gradient hero: a slowly breathing brand gradient with drifting
 * light orbs. Used as the signature header of every main screen.
 * Grows with its content (never clips) and ends with rounded corners.
 *
 * @param fullscreenTop when true the gradient paints behind the status bar
 *   (edge-to-edge feel on modern phones) while the text stays inset.
 */
@Composable
fun HeroSection(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    badge: String? = null,
    accent: HeroAccent = HeroAccents.Home,
    minHeight: Dp = 140.dp,
    fullscreenTop: Boolean = false,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val transition = rememberInfiniteTransition(label = "hero")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16_000, easing = LinearEasing),
        ),
        label = "heroPhase",
    )
    val breathe by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "heroBreathe",
    )
    val background = MaterialTheme.colorScheme.background
    val rad = Math.toRadians(phase.toDouble())
    val shape = RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .clip(shape)
            .background(FazlakaGradientEnd),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            // Base diagonal brand gradient whose angle slowly oscillates
            val shift = (breathe * 0.18f) * w
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        FazlakaGradientEnd,
                        accent.primary.copy(alpha = 0.92f),
                        accent.secondary.copy(alpha = 0.55f),
                    ),
                    start = Offset(-shift, 0f),
                    end = Offset(w + shift, h),
                ),
                size = size,
            )
            // Drifting glowing orbs
            val orbs = listOf(
                Triple(
                    0.22f * w + (sin(rad) * 0.10f * w).toFloat(),
                    0.28f * h + (cos(rad) * 0.16f * h).toFloat(),
                    accent.glow.copy(alpha = 0.32f),
                ),
                Triple(
                    0.86f * w + (cos(rad) * 0.08f * w).toFloat(),
                    0.68f * h + (sin(rad * 1.4) * 0.20f * h).toFloat(),
                    FazlakaCyan.copy(alpha = 0.28f),
                ),
                Triple(
                    0.60f * w + (sin(rad * 0.7 + 2.0) * 0.12f * w).toFloat(),
                    -0.05f * h + (cos(rad * 0.9) * 0.10f * h).toFloat(),
                    Color.White.copy(alpha = 0.13f),
                ),
            )
            orbs.forEach { (cx, cy, color) ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color, Color.Transparent),
                        center = Offset(cx, cy),
                        radius = h * 0.75f,
                    ),
                    radius = h * 0.75f,
                    center = Offset(cx, cy),
                )
            }
            // Soft bottom haze only (content stays fully readable)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.10f),
                    ),
                    startY = h * 0.72f,
                    endY = h,
                ),
                size = size,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (fullscreenTop) {
                        Modifier.windowInsetsPadding(WindowInsets.statusBars)
                    } else {
                        Modifier
                    },
                )
                .padding(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            if (badge != null) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.25f),
                    ),
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 4.dp),
                    )
                }
                Spacer(Modifier.height(9.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(5.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.88f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            content()
        }
    }
}

/** Colored drop shadow (spot/ambient tint on API 28+, plain elevation below). */
fun Modifier.glowShadow(
    elevation: Dp = 14.dp,
    shape: Shape = RoundedCornerShape(18.dp),
    glowColor: Color = FazlakaGradientStart,
): Modifier {
    return this.shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = glowColor,
        spotColor = glowColor,
    )
}

/** Translucent "glass" surface: soft container, gradient hairline border
 *  and a light sheen across the top edge. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            FazlakaCyan.copy(alpha = 0.35f),
            FazlakaGradientStart.copy(alpha = 0.30f),
            Color.Transparent,
        ),
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f),
            )
            .border(1.dp, borderBrush, shape),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) { content() }
    }
}

/** Small circular icon chip with a soft brand glow — used inside heroes. */
@Composable
fun GlowIconChip(
    emoji: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    tint: Color = FazlakaCyan,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        tint.copy(alpha = 0.35f),
                        tint.copy(alpha = 0.08f),
                    ),
                ),
            )
            .border(1.dp, tint.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, style = MaterialTheme.typography.titleMedium)
    }
}

/** Row of stat pills used under heroes / profiles. */
@Composable
fun HeroStatPills(
    stats: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        stats.forEach { (value, label) ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.14f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.22f),
                ),
                modifier = Modifier.weight(1f),
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

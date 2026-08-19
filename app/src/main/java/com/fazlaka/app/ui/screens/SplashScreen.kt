package com.fazlaka.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.fazlaka.app.ui.components.BrandLogo
import com.fazlaka.app.ui.components.BrandSpinner
import com.fazlaka.app.ui.theme.FazlakaCyan
import com.fazlaka.app.ui.theme.FazlakaGradientMid
import com.fazlaka.app.ui.theme.FazlakaGradientNight
import com.fazlaka.app.ui.theme.FazlakaGradientStart
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    startDestination: String,
    onNavigate: (String) -> Unit,
) {
    val entrance = remember { Animatable(0f) }
    val pulse by rememberInfiniteTransition(label = "splashPulse").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    LaunchedEffect(Unit) {
        entrance.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(FazlakaGradientNight, FazlakaGradientMid, FazlakaGradientStart),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Dual-tone glow: cyan bottom-left + purple top-right
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(FazlakaCyan.copy(alpha = 0.25f), Color.Transparent),
                        radius = 900f,
                        center = Offset(200f, 1200f),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(FazlakaGradientStart.copy(alpha = 0.30f), Color.Transparent),
                        radius = 1100f,
                        center = Offset(900f, 300f),
                    ),
                ),
        )
        Box(
            modifier = Modifier.graphicsLayer {
                val breathe = 0.97f + 0.03f * pulse
                scaleX = entrance.value * breathe
                scaleY = entrance.value * breathe
                alpha = 0.75f + 0.25f * pulse
            },
        ) {
            BrandLogo(size = 44.dp)
        }
        BrandSpinner(
            diameter = 28.dp,
            strokeWidth = 2.5.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp),
        )
    }
    LaunchedEffect(startDestination) {
        delay(1200)
        onNavigate(startDestination)
    }
}

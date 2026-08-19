package com.fazlaka.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColors = darkColorScheme(
    primary = FazlakaPrimary,
    onPrimary = FazlakaOnPrimary,
    primaryContainer = Color(0xFF4C1D95),
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = FazlakaSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF2D1B4E),
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = FazlakaGlow,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF3A2A12),
    onTertiaryContainer = Color(0xFFFFE3B3),
    background = FazlakaBackground,
    onBackground = Color(0xFFE9EAF0),
    surface = FazlakaSurface,
    onSurface = Color(0xFFF1F2F8),
    surfaceVariant = FazlakaSurfaceDark,
    onSurfaceVariant = Color(0xFFB6BCCB),
    surfaceContainerLowest = Color(0xFF0A0E1A),
    surfaceContainerLow = Color(0xFF111726),
    surfaceContainer = FazlakaCardDark,
    surfaceContainerHigh = FazlakaFieldDark,
    surfaceContainerHighest = Color(0xFF283247),
    outline = Color(0xFF3D4663),
    outlineVariant = Color(0xFF232C40),
    error = FazlakaError,
    onError = Color.White,
    errorContainer = Color(0xFF450A0A),
    onErrorContainer = Color(0xFFFECACA),
    scrim = Color.Black,
)

private val LightColors = lightColorScheme(
    primary = FazlakaPrimaryDark,
    onPrimary = FazlakaOnPrimary,
    primaryContainer = FazlakaPrimaryContainer,
    onPrimaryContainer = FazlakaPrimaryDark,
    secondary = FazlakaSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF3B2A63),
    tertiary = FazlakaGradientStart,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF3E8FF),
    onTertiaryContainer = Color(0xFF4C1D95),
    background = Color.White,
    onBackground = Color(0xFF111827),
    surface = Color.White,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF4B5563),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F8FC),
    surfaceContainer = Color(0xFFF3F4F6),
    surfaceContainerHigh = Color(0xFFECEDF2),
    surfaceContainerHighest = Color(0xFFE5E7EB),
    outline = Color(0xFF9CA3AF),
    outlineVariant = Color(0xFFE5E7EB),
    error = FazlakaError,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    scrim = Color.Black,
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(30.dp),
)

@Composable
fun FazlakaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content,
    )
}

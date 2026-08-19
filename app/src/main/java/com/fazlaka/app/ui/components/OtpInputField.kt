package com.fazlaka.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fazlaka.app.ui.theme.FazlakaCyan
import com.fazlaka.app.ui.theme.FazlakaGradientMid
import com.fazlaka.app.ui.theme.FazlakaGradientStart

/**
 * 6-digit OTP input rendered as individual animated cells. Each filled cell
 * pulses in with a spring animation, and a sweeping cursor blinks on the
 * active cell. Fully RTL-aware for Arabic users.
 */
@Composable
fun OtpInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    digitCount: Int = 6,
) {
    val focusRequester = remember { FocusRequester() }
    val filledCount = value.length.coerceIn(0, digitCount)

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            repeat(digitCount) { index ->
                val isFilled = index < filledCount
                val isActive = index == filledCount && filledCount < digitCount
                val scale by animateFloatAsState(
                    targetValue = if (isFilled) 1f else 0.92f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "otpScale",
                )

                OtpCell(
                    char = if (isFilled) value[index] else null,
                    isActive = isActive,
                    modifier = Modifier.size(width = 48.dp, height = 58.dp)
                        .graphicsLayer { scaleX = scale; scaleY = scale },
                )
            }
        }

        // Hidden field that captures keyboard input
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.filter(Char::isDigit).take(digitCount)) },
            modifier = Modifier.size(1.dp).focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(color = Color.Transparent, fontSize = MaterialTheme.typography.bodyLarge.fontSize),
        )
    }
}

@Composable
private fun OtpCell(
    char: Char?,
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(14.dp)
    // Glass colors tuned for the dark gradient auth background
    val borderColor = when {
        char != null -> FazlakaGradientStart
        isActive -> FazlakaCyan
        else -> Color.White.copy(alpha = 0.28f)
    }
    val bgColor = when {
        char != null -> FazlakaGradientStart.copy(alpha = 0.18f)
        isActive -> Color.White.copy(alpha = 0.10f)
        else -> Color.White.copy(alpha = 0.07f)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(2.dp, borderColor, shape),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = char != null,
            enter = scaleIn(
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            ) + fadeIn(),
        ) {
            Text(
                text = char?.toString() ?: "",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Visual password-strength bar shown below the password field. */
@Composable
fun PasswordStrengthIndicator(
    password: String,
    modifier: Modifier = Modifier,
) {
    if (password.isEmpty()) return
    val strength = remember(password) { computeStrength(password) }
    val label = remember(strength) {
        when (strength) {
            0 -> "ضعيفة جدًا"
            1 -> "ضعيفة"
            2 -> "متوسطة"
            3 -> "قوية"
            else -> "قوية جدًا"
        }
    }
    val color = when (strength) {
        0, 1 -> Color(0xFFEF4444)
        2 -> Color(0xFFF59E0B)
        3, 4 -> Color(0xFF10B981)
        else -> Color(0xFF10B981)
    }
    val fraction by animateFloatAsState(
        targetValue = (strength + 1) / 5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "strengthBar",
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.15f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(FazlakaGradientStart, color),
                        ),
                    ),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

private fun computeStrength(pw: String): Int {
    var score = 0
    if (pw.length >= 8) score++
    if (pw.length >= 12) score++
    if (pw.any { it.isUpperCase() }) score++
    if (pw.any { it.isDigit() }) score++
    if (pw.any { !it.isLetterOrDigit() }) score++
    return score.coerceIn(0, 4)
}

/** Animated title inside auth screens: fades + slides in per-word. */
@Composable
fun AnimatedAuthTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    val words = title.split(" ")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        words.forEachIndexed { index, word ->
            androidx.compose.animation.AnimatedVisibility(
                visible = true,
                enter = fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(
                        500,
                        delayMillis = index * 120,
                        easing = androidx.compose.animation.core.FastOutSlowInEasing,
                    ),
                ) + androidx.compose.animation.slideInVertically(
                    animationSpec = androidx.compose.animation.core.tween(
                        500,
                        delayMillis = index * 120,
                        easing = androidx.compose.animation.core.FastOutSlowInEasing,
                    ),
                ) { -it / 5 },
            ) {
                Text(
                    text = word,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                )
            }
        }
    }
}

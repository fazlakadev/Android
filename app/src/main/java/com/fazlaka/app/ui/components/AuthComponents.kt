package com.fazlaka.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.fazlaka.app.ui.theme.FacebookBlue
import com.fazlaka.app.ui.theme.FazlakaCardDark
import com.fazlaka.app.ui.theme.FazlakaCyan
import com.fazlaka.app.ui.theme.FazlakaGradientMid
import com.fazlaka.app.ui.theme.FazlakaGradientNight
import com.fazlaka.app.ui.theme.FazlakaGradientStart
import com.fazlaka.app.ui.theme.GitHubBlack
import com.fazlaka.app.ui.theme.GoogleBlue
import com.fazlaka.app.ui.theme.GoogleGreen
import com.fazlaka.app.ui.theme.GoogleRed
import com.fazlaka.app.ui.theme.GoogleYellow

/**
 * Dark auth backdrop: solid night base with a few soft glowing orbs
 * (radial gradients fading to transparent — no hard edges, no loud bands).
 */
@Composable
fun AuthBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FazlakaGradientNight),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawOrb(
                center = Offset(size.width * 0.88f, size.height * 0.10f),
                radius = size.width * 0.80f,
                color = FazlakaGradientStart,
                alpha = 0.16f,
            )
            drawOrb(
                center = Offset(size.width * 0.10f, size.height * 0.88f),
                radius = size.width * 0.70f,
                color = FazlakaCyan,
                alpha = 0.10f,
            )
            drawOrb(
                center = Offset(size.width * 0.30f, size.height * 0.42f),
                radius = size.width * 0.55f,
                color = FazlakaGradientMid,
                alpha = 0.07f,
            )
        }
        content()
    }
}

private fun DrawScope.drawOrb(center: Offset, radius: Float, color: Color, alpha: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = alpha),
                color.copy(alpha = alpha * 0.30f),
                Color.Transparent,
            ),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}

@Composable
fun BrandLogo(
    size: Dp = 56.dp,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(com.fazlaka.app.R.drawable.logo),
        contentDescription = "شعار فذلكة",
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit,
    )
}

@Composable
fun BrandHeader(
    tagline: String = "منصة المحتوى والحلقات والمقالات العربية",
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BrandLogo(size = 64.dp)
        Spacer(Modifier.height(10.dp))
        Text(
            text = tagline,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.72f),
            textAlign = TextAlign.Center,
        )
    }
}

/** Modern glassmorphism auth card with entrance animation and edge-to-edge safe
 *  area handling. The card automatically scrolls to keep the ime-visible field
 *  on screen. */
@Composable
fun AuthScaffold(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    showBrand: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entrance.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        )
    }

    AuthBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Brand at top
            if (showBrand) {
                BrandHeader()
                Spacer(Modifier.height(20.dp))
            }

            // Dark elevated card
            Surface(
                color = Color(0xFF10182B),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f)),
                shadowElevation = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = entrance.value
                        scaleY = entrance.value
                        alpha = entrance.value
                    },
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AnimatedAuthTitle(title = title)
                    if (subtitle != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.65f),
                            textAlign = TextAlign.Center,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                        )
                    }
                    Spacer(Modifier.height(22.dp))
                    content()
                }
            }
        }
    }
}

/**
 * Glass-style auth input tuned for the dark gradient auth background:
 * translucent container, soft white borders, white text, and an inside
 * placeholder (never a floating label) so nothing ever spills outside
 * the field box.
 */
@Composable
fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    icon: ImageVector? = null,
    singleLine: Boolean = true,
    supportingText: String? = null,
) {
    val shape = RoundedCornerShape(14.dp)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        placeholder = {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.55f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        singleLine = singleLine,
        isError = supportingText != null,
        visualTransformation = if (isPassword) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = keyboardOptions,
        shape = shape,
        leadingIcon = icon?.let {
            { Icon(it, contentDescription = null, tint = Color.White.copy(alpha = 0.75f)) }
        },
        supportingText = supportingText?.let {
            {
                Text(
                    it,
                    color = Color(0xFFFCA5A5),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White,
            focusedBorderColor = FazlakaGradientStart,
            unfocusedBorderColor = Color.White.copy(alpha = 0.28f),
            focusedContainerColor = Color.White.copy(alpha = 0.12f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
            focusedPlaceholderColor = Color.White.copy(alpha = 0.55f),
            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.55f),
            errorTextColor = Color.White,
            errorBorderColor = Color(0xFFFCA5A5),
            errorCursorColor = Color(0xFFFCA5A5),
            errorContainerColor = Color.White.copy(alpha = 0.08f),
        ),
    )
}

@Composable
fun AuthButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    enabled: Boolean = true,
) {
    val active = enabled && !loading
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        enabled = active,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = Color.White,
        ),
        elevation = null,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (active) {
                        Brush.horizontalGradient(listOf(FazlakaGradientStart, FazlakaGradientMid))
                    } else {
                        SolidColor(Color(0xFF374151))
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = Color.White,
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

enum class SocialProvider(val displayName: String) {
    Google("Google"),
    GitHub("GitHub"),
    Facebook("Facebook"),
}

@Composable
fun SocialAuthButton(
    provider: SocialProvider,
    onClick: () -> Unit,
    loading: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val active = !loading
    val containerColor = if (androidx.compose.foundation.isSystemInDarkTheme()) {
        FazlakaCardDark.copy(alpha = 0.85f)
    } else {
        Color.White
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(enabled = active, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SocialBadge(provider)
        Spacer(Modifier.size(12.dp))
        Text(
            text = when (provider) {
                SocialProvider.Google -> "المتابعة عبر Google"
                SocialProvider.GitHub -> "المتابعة عبر GitHub"
                SocialProvider.Facebook -> "المتابعة عبر Facebook"
            },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Spacer(Modifier.width(8.dp))
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        }
    }
}

/** Compact circular social sign-in button for the social row. */
@Composable
fun SocialIconButton(
    provider: SocialProvider,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (interactionSource, pressed) = rememberPressInteraction()
    Box(
        modifier = modifier
            .size(56.dp)
            .pressableScale(pressedScale = 0.92f, pressed = pressed)
            .clip(CircleShape)
            .background(Color(0xFF182038))
            .border(1.dp, Color.White.copy(alpha = 0.10f), CircleShape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when (provider) {
            SocialProvider.Google -> GoogleIcon(Modifier.size(26.dp))
            SocialProvider.GitHub -> GithubIcon(Modifier.size(28.dp))
            SocialProvider.Facebook -> Text(
                text = "f",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/** Row of the three social providers. */
@Composable
fun SocialIconRow(
    onGoogle: () -> Unit,
    onGitHub: () -> Unit,
    onFacebook: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SocialIconButton(SocialProvider.Google, onGoogle)
        SocialIconButton(SocialProvider.GitHub, onGitHub)
        SocialIconButton(SocialProvider.Facebook, onFacebook)
    }
}

@Composable
private fun SocialBadge(provider: SocialProvider) {
    val badgeColor = when (provider) {
        SocialProvider.Google -> Color.White
        SocialProvider.GitHub -> GitHubBlack
        SocialProvider.Facebook -> FacebookBlue
    }
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(badgeColor),
        contentAlignment = Alignment.Center,
    ) {
        when (provider) {
            SocialProvider.Google -> GoogleIcon(Modifier.size(24.dp))
            SocialProvider.GitHub -> GithubIcon(Modifier.size(26.dp))
            SocialProvider.Facebook -> Text(
                text = "f",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun GoogleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.20f
        val radius = size.minDimension * 0.42f
        val c = center
        val rect = Rect(
            offset = Offset(c.x - radius, c.y - radius),
            size = Size(radius * 2, radius * 2),
        )
        val style = Stroke(width = stroke, cap = StrokeCap.Butt)
        drawArc(GoogleBlue, 205f, 245f, false, rect.topLeft, rect.size, style = style)
        drawArc(GoogleRed, 90f, 42f, false, rect.topLeft, rect.size, style = style)
        drawArc(GoogleYellow, 48f, 42f, false, rect.topLeft, rect.size, style = style)
        drawArc(GoogleGreen, 6f, 42f, false, rect.topLeft, rect.size, style = style)
        drawLine(
            color = GoogleBlue,
            start = Offset(c.x - radius * 0.55f, c.y),
            end = Offset(c.x + radius * 0.05f, c.y),
            strokeWidth = stroke,
        )
    }
}

@Composable
fun GithubIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(Color.White, radius = w * 0.30f, center = center)
        drawRect(
            Color.White,
            topLeft = Offset(center.x - w * 0.30f, center.y + h * 0.10f),
            size = Size(w * 0.60f, h * 0.28f),
        )
        drawRect(Color.White, topLeft = Offset(center.x - w * 0.36f, center.y - h * 0.26f), size = Size(w * 0.16f, h * 0.26f))
        drawRect(Color.White, topLeft = Offset(center.x + w * 0.20f, center.y - h * 0.26f), size = Size(w * 0.16f, h * 0.26f))
        drawCircle(Color.Black, radius = w * 0.07f, center = Offset(center.x - w * 0.08f, center.y))
        drawCircle(Color.Black, radius = w * 0.07f, center = Offset(center.x + w * 0.16f, center.y))
    }
}

@Composable
fun OrDivider(
    text: String = "أو",
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color.White.copy(alpha = 0.18f),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp),
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color.White.copy(alpha = 0.18f),
        )
    }
}

@Composable
fun AuthLinkRow(
    text: String,
    linkText: String,
    onLinkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.72f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.size(4.dp))
        Text(
            text = linkText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable(onClick = onLinkClick),
        )
    }
}

fun authFieldIcon(isPassword: Boolean, keyboardType: KeyboardType): ImageVector = when {
    isPassword -> Icons.Filled.Lock
    keyboardType == KeyboardType.Email -> Icons.Filled.Email
    else -> Icons.Filled.Person
}

package com.fazlaka.app.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fazlaka.app.ui.components.AnimatedAuthTitle
import com.fazlaka.app.ui.components.AuthBackground
import com.fazlaka.app.ui.components.AuthButton
import com.fazlaka.app.ui.components.AuthField
import com.fazlaka.app.ui.components.OtpInputField
import com.fazlaka.app.ui.components.PasswordStrengthIndicator
import com.fazlaka.app.ui.navigation.Routes
import com.fazlaka.app.ui.theme.FazlakaCyan
import com.fazlaka.app.ui.theme.FazlakaGradientMid
import com.fazlaka.app.ui.theme.FazlakaGradientStart
import com.fazlaka.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val RESEND_COOLDOWN = 45

@Composable
fun ResetPasswordScreen(
    email: String,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var cooldown by remember { mutableIntStateOf(RESEND_COOLDOWN) }
    val resentMessage = stringResource(com.fazlaka.app.R.string.verify_resent)

    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }
    LaunchedEffect(Unit) {
        while (cooldown > 0) {
            delay(1000)
            cooldown--
        }
    }

    val passwordsMatch = password == confirm

    AuthBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(48.dp))
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    FazlakaGradientStart.copy(alpha = 0.22f),
                                    FazlakaCyan.copy(alpha = 0.08f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.LockReset,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(36.dp),
                    )
                }
                Spacer(Modifier.height(18.dp))
                AnimatedAuthTitle(title = stringResource(com.fazlaka.app.R.string.reset_title))
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(com.fazlaka.app.R.string.reset_subtitle_fmt, email),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
                OtpInputField(
                    value = code,
                    onValueChange = { code = it },
                )
                Spacer(Modifier.height(24.dp))
                AuthField(
                    value = password,
                    onValueChange = { password = it },
                    label = stringResource(com.fazlaka.app.R.string.reset_new_password),
                    isPassword = true,
                    icon = Icons.Filled.LockReset,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )
                AnimatedVisibility(
                    visible = password.isNotEmpty(),
                    enter = fadeIn() + slideInVertically { -it / 3 },
                ) {
                    Column {
                        Spacer(Modifier.height(8.dp))
                        PasswordStrengthIndicator(password = password)
                    }
                }
                Spacer(Modifier.height(12.dp))
                AuthField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = stringResource(com.fazlaka.app.R.string.reset_confirm),
                    isPassword = true,
                    icon = Icons.Filled.LockReset,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    supportingText = if (confirm.isNotEmpty() && !passwordsMatch) {
                        stringResource(com.fazlaka.app.R.string.reset_mismatch)
                    } else {
                        null
                    },
                )
                Spacer(Modifier.height(20.dp))
                AuthButton(
                    text = stringResource(com.fazlaka.app.R.string.reset_submit),
                    onClick = {
                        viewModel.resetPassword(password, email, otp = code) {
                            onNavigate(Routes.LOGIN)
                        }
                    },
                    loading = state.loading,
                    enabled = password.length >= 8 && passwordsMatch && code.length == 6,
                )
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        if (cooldown == 0) {
                            viewModel.forgotPassword(email) { ok ->
                                if (ok) {
                                    cooldown = RESEND_COOLDOWN
                                    scope.launch { snackbar.showSnackbar(resentMessage) }
                                }
                            }
                        }
                    },
                    enabled = cooldown == 0,
                ) {
                    Text(
                        text = if (cooldown == 0) {
                            stringResource(com.fazlaka.app.R.string.verify_resend)
                        } else {
                            stringResource(com.fazlaka.app.R.string.verify_resend_cooldown, cooldown)
                        },
                    )
                }
                TextButton(onClick = { onBack() }) {
                    Text(stringResource(com.fazlaka.app.R.string.common_back))
                }
                Spacer(Modifier.height(32.dp))
            }
            SnackbarHost(hostState = snackbar, modifier = Modifier.padding(16.dp))
        }
    }
}

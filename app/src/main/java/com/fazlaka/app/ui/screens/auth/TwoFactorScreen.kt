package com.fazlaka.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fazlaka.app.ui.components.AnimatedAuthTitle
import com.fazlaka.app.ui.components.AuthBackground
import com.fazlaka.app.ui.components.AuthButton
import com.fazlaka.app.ui.components.OtpInputField
import com.fazlaka.app.ui.navigation.Routes
import com.fazlaka.app.ui.theme.FazlakaCyan
import com.fazlaka.app.ui.theme.FazlakaGradientMid
import com.fazlaka.app.ui.theme.FazlakaGradientStart
import com.fazlaka.app.ui.viewmodel.AuthViewModel

@Composable
fun TwoFactorScreen(
    onNavigate: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    var otp by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }
    LaunchedEffect(state.success) {
        if (state.success) onNavigate(Routes.MAIN)
    }

    AuthBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(88.dp)
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
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(38.dp),
                    )
                }
                Spacer(Modifier.height(20.dp))
                AnimatedAuthTitle(title = stringResource(com.fazlaka.app.R.string.twofa_title))
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(com.fazlaka.app.R.string.twofa_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(28.dp))
                OtpInputField(
                    value = otp,
                    onValueChange = { otp = it.filter(Char::isDigit).take(6) },
                )
                Spacer(Modifier.height(28.dp))
                AuthButton(
                    text = stringResource(com.fazlaka.app.R.string.twofa_submit),
                    onClick = {
                        state.twoFactorEmail?.let {
                            viewModel.loginTwoFactor(it, otp) { }
                        }
                    },
                    loading = state.loading,
                    enabled = otp.length == 6,
                )
                Spacer(Modifier.weight(1.2f))
            }
            SnackbarHost(hostState = snackbar, modifier = Modifier.padding(16.dp))
        }
    }
}

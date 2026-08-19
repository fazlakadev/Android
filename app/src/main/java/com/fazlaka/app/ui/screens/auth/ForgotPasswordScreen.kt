package com.fazlaka.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fazlaka.app.ui.components.AuthButton
import com.fazlaka.app.ui.components.AuthField
import com.fazlaka.app.ui.components.AuthScaffold
import com.fazlaka.app.ui.navigation.Routes
import com.fazlaka.app.ui.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    var email by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        AuthScaffold(
            title = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.forgot_title),
            subtitle = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.forgot_subtitle),
        ) {
            AuthField(
                value = email,
                onValueChange = { email = it },
                label = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.login_email),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            Spacer(Modifier.height(20.dp))
            AuthButton(
                text = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.forgot_submit),
                onClick = {
                    viewModel.forgotPassword(email) { ok ->
                        if (ok) onNavigate(Routes.resetPassword(email.trim()))
                    }
                },
                loading = state.loading,
                enabled = email.isNotBlank(),
            )
        }
        SnackbarHost(hostState = snackbar, modifier = Modifier.padding(16.dp))
    }
}

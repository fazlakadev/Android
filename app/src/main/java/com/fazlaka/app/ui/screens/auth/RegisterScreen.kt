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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fazlaka.app.ui.components.AuthButton
import com.fazlaka.app.ui.components.AuthField
import com.fazlaka.app.ui.components.AuthLinkRow
import com.fazlaka.app.ui.components.AuthScaffold
import com.fazlaka.app.ui.components.OrDivider
import com.fazlaka.app.ui.components.PasswordStrengthIndicator
import com.fazlaka.app.ui.components.SocialIconRow
import com.fazlaka.app.ui.navigation.Routes
import com.fazlaka.app.ui.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onNavigate: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var referral by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }
    LaunchedEffect(state.success) {
        if (state.success) onNavigate(Routes.MAIN)
    }
    LaunchedEffect(state.justRegistered) {
        if (state.justRegistered) {
            onNavigate(Routes.verifyEmail(state.registeredEmail ?: ""))
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AuthScaffold(
            title = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.register_title),
            subtitle = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.register_subtitle),
        ) {
            AuthField(
                value = name,
                onValueChange = { name = it },
                label = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.register_name),
                icon = Icons.Filled.Person,
            )
            Spacer(Modifier.height(12.dp))
            AuthField(
                value = email,
                onValueChange = { email = it },
                label = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.register_email),
                icon = Icons.Filled.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            Spacer(Modifier.height(12.dp))
            AuthField(
                value = username,
                onValueChange = { username = it },
                label = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.register_username),
                icon = Icons.Filled.AlternateEmail,
            )
            Spacer(Modifier.height(12.dp))
            AuthField(
                value = password,
                onValueChange = { password = it },
                label = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.register_password),
                isPassword = true,
                icon = Icons.Filled.Lock,
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
                value = referral,
                onValueChange = { referral = it },
                label = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.register_referral),
                icon = Icons.Filled.ConfirmationNumber,
            )
            Spacer(Modifier.height(20.dp))
            AuthButton(
                text = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.register_submit),
                onClick = {
                    viewModel.register(email, password, name, username, referral) { }
                },
                loading = state.loading,
                enabled = email.isNotBlank() && password.length >= 8 && name.isNotBlank() && username.isNotBlank(),
            )
            Spacer(Modifier.height(22.dp))
            OrDivider(text = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.auth_or_divider))
            Spacer(Modifier.height(18.dp))
            SocialIconRow(
                onGoogle = { onNavigate(Routes.oauth("google")) },
                onGitHub = { onNavigate(Routes.oauth("github")) },
                onFacebook = { onNavigate(Routes.oauth("facebook")) },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(20.dp))
            AuthLinkRow(
                text = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.auth_has_account),
                linkText = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.auth_login_link),
                onLinkClick = { onNavigate(Routes.LOGIN) },
            )
        }
        SnackbarHost(hostState = snackbar, modifier = Modifier.padding(16.dp))
    }
}

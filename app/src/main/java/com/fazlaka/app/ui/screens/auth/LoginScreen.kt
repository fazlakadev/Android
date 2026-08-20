package com.fazlaka.app.ui.screens.auth

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fazlaka.app.core.location.locationProvider
import com.fazlaka.app.ui.components.AuthButton
import com.fazlaka.app.ui.components.AuthField
import com.fazlaka.app.ui.components.AuthLinkRow
import com.fazlaka.app.ui.components.AuthScaffold
import com.fazlaka.app.ui.components.OrDivider
import com.fazlaka.app.ui.components.SocialIconRow
import com.fazlaka.app.ui.navigation.Routes
import com.fazlaka.app.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onNavigate: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            locationProvider(context).start()
        }
    }
    LaunchedEffect(Unit) {
        val provider = locationProvider(context)
        if (!provider.hasPermission()) {
            locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            provider.start()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }
    LaunchedEffect(state.requiresTwoFactor) {
        if (state.requiresTwoFactor) onNavigate(Routes.TWO_FACTOR)
    }
    LaunchedEffect(state.success) {
        if (state.success) onNavigate(Routes.MAIN)
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AuthScaffold(
            title = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.login_title),
            subtitle = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.login_subtitle),
        ) {
            AuthField(
                value = email,
                onValueChange = { email = it },
                label = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.login_email),
                icon = com.fazlaka.app.ui.components.authFieldIcon(false, KeyboardType.Email),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            Spacer(Modifier.height(12.dp))
            AuthField(
                value = password,
                onValueChange = { password = it },
                label = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.login_password),
                isPassword = true,
                icon = com.fazlaka.app.ui.components.authFieldIcon(true, KeyboardType.Password),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            Spacer(Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = { onNavigate(Routes.FORGOT_PASSWORD) }) {
                    Text(androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.login_forgot))
                }
            }
            Spacer(Modifier.height(6.dp))
            AuthButton(
                text = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.login_submit),
                onClick = { viewModel.login(email, password) { } },
                loading = state.loading,
                enabled = email.isNotBlank() && password.isNotBlank(),
            )
            Spacer(Modifier.height(22.dp))
            OrDivider(text = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.auth_or_divider))
            Spacer(Modifier.height(18.dp))
            SocialIconRow(
                onGoogle = { viewModel.googleSignIn {} },
                onGitHub = { onNavigate(Routes.oauth("github")) },
                onFacebook = { onNavigate(Routes.oauth("facebook")) },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(20.dp))
            AuthLinkRow(
                text = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.auth_no_account),
                linkText = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.auth_create_account),
                onLinkClick = { onNavigate(Routes.REGISTER) },
            )
        }
        SnackbarHost(hostState = snackbar, modifier = Modifier.padding(16.dp))
    }
}

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fazlaka.app.ui.components.AuthButton
import com.fazlaka.app.ui.components.AuthField
import com.fazlaka.app.ui.components.AuthScaffold
import com.fazlaka.app.ui.components.OtpInputField
import com.fazlaka.app.ui.navigation.Routes
import com.fazlaka.app.ui.viewmodel.AuthViewModel

@Composable
fun PhoneAuthScreen(
    mode: String,
    onNavigate: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }
    LaunchedEffect(state.success) {
        if (state.success) onNavigate(Routes.MAIN)
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        AuthScaffold(
            title = if (mode == "register") stringResource(com.fazlaka.app.R.string.phone_title_register) else stringResource(com.fazlaka.app.R.string.phone_title_login),
            subtitle = if (mode == "register") stringResource(com.fazlaka.app.R.string.phone_subtitle_register) else stringResource(com.fazlaka.app.R.string.phone_subtitle_login),
        ) {
            if (!state.pendingPhone) {
                if (mode == "register") {
                    AuthField(value = name, onValueChange = { name = it }, label = stringResource(com.fazlaka.app.R.string.register_name))
                    Spacer(Modifier.height(12.dp))
                    AuthField(value = username, onValueChange = { username = it }, label = stringResource(com.fazlaka.app.R.string.register_username))
                    Spacer(Modifier.height(12.dp))
                }
                AuthField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = stringResource(com.fazlaka.app.R.string.phone_number),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )
                Spacer(Modifier.height(20.dp))
                AuthButton(
                    text = stringResource(com.fazlaka.app.R.string.phone_send_code),
                    onClick = {
                        if (mode == "register") {
                            viewModel.registerPhone(phone, name, username)
                        } else {
                            viewModel.requestPhoneLogin(phone)
                        }
                    },
                    loading = state.loading,
                    enabled = phone.isNotBlank(),
                )
            } else {
                Text(
                    "تم إرسال رمز التحقق إلى: ${state.phoneChallenge?.phone ?: phone}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.75f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Spacer(Modifier.height(20.dp))
                OtpInputField(
                    value = code,
                    onValueChange = { code = it.filter(Char::isDigit).take(6) },
                )
                Spacer(Modifier.height(24.dp))
                AuthButton(
                    text = stringResource(com.fazlaka.app.R.string.phone_verify),
                    onClick = {
                        viewModel.completePhone(
                            phone = phone,
                            verificationId = state.phoneChallenge?.verificationId ?: "",
                            code = code,
                        ) { }
                    },
                    loading = state.loading,
                    enabled = code.length == 6,
                )
            }
        }
        SnackbarHost(hostState = snackbar, modifier = Modifier.padding(16.dp))
    }
}

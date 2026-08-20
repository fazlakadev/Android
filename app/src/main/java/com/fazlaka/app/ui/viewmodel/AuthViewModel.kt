package com.fazlaka.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fazlaka.app.core.common.ErrorMessages.localized
import com.fazlaka.app.core.model.dto.AuthResultDto
import com.fazlaka.app.core.model.dto.PhoneChallengeDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.core.notification.PushRepository
import com.fazlaka.app.core.auth.GoogleSignInHelper
import com.fazlaka.app.core.auth.GoogleSignInException
import com.fazlaka.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val requiresTwoFactor: Boolean = false,
    val twoFactorEmail: String? = null,
    val twoFactorMethod: String? = null,
    val pendingPhone: Boolean = false,
    val phoneChallenge: PhoneChallengeDto? = null,
    val justRegistered: Boolean = false,
    val registeredEmail: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val pushRepository: PushRepository,
    private val googleSignInHelper: GoogleSignInHelper,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun login(email: String, password: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            when (val r = authRepository.login(email.trim(), password)) {
                is ApiResult.Success -> handleAuthResult(r.data, onDone)
                is ApiResult.Failure -> {
                    val twoFactor = r.code == 403 || (r.message?.contains("twoFactor", true) == true)
                    _state.value = AuthUiState(
                        error = r.localized(),
                        requiresTwoFactor = twoFactor,
                        twoFactorEmail = email.trim(),
                    )
                }
            }
        }
    }

    fun loginTwoFactor(email: String, otp: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            when (val r = authRepository.loginTwoFactor(email, otp)) {
                is ApiResult.Success -> handleAuthResult(r.data, onDone)
                is ApiResult.Failure -> _state.value = AuthUiState(error = r.localized())
            }
        }
    }

    fun socialLogin(accessToken: String?, refreshToken: String?) {
        if (accessToken.isNullOrEmpty()) {
            _state.value = AuthUiState(error = "لم يتم الحصول على بيانات الدخول من المزود")
            return
        }
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            val ok = authRepository.persistSession(
                AuthResultDto(accessToken = accessToken, refreshToken = refreshToken),
            )
            if (ok) {
                _state.value = AuthUiState(success = true)
            } else {
                _state.value = AuthUiState(error = "حدث خطأ أثناء حفظ الجلسة")
            }
        }
    }

    fun googleNativeLogin(idToken: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            when (val r = authRepository.googleNativeLogin(idToken)) {
                is ApiResult.Success -> handleAuthResult(r.data, onDone)
                is ApiResult.Failure -> {
                    _state.value = AuthUiState(error = r.localized())
                }
            }
        }
    }

    fun googleSignIn(onDone: () -> Unit) {
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            try {
                val idToken = googleSignInHelper.signIn()
                googleNativeLogin(idToken, onDone)
            } catch (e: GoogleSignInException) {
                _state.value = AuthUiState(error = e.message ?: "تعذر تسجيل الدخول عبر Google")
            } catch (e: Exception) {
                _state.value = AuthUiState(error = e.message ?: "تعذر تسجيل الدخول عبر Google")
            }
        }
    }

    fun register(
        email: String,
        password: String,
        name: String,
        username: String,
        referralCode: String?,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            val r = authRepository.register(
                com.fazlaka.app.core.model.dto.RegisterRequest(
                    email = email.trim(),
                    password = password,
                    name = name.trim(),
                    username = username.trim(),
                    locale = "ar",
                    referralCode = referralCode?.takeIf { it.isNotBlank() },
                ),
            )
            when (r) {
                is ApiResult.Success -> {
                    val result = r.data
                    if (result.pending) {
                        _state.value = AuthUiState(
                            justRegistered = true,
                            registeredEmail = email.trim(),
                        )
                    } else {
                        handleAuthResult(result, onDone)
                    }
                }
                is ApiResult.Failure -> _state.value = AuthUiState(error = r.localized())
            }
        }
    }

    fun registerPhone(phone: String, name: String, username: String) {
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            val r = authRepository.registerPhone(
                com.fazlaka.app.core.model.dto.RegisterPhoneRequest(
                    phone = phone.trim(),
                    name = name.trim().ifEmpty { null },
                    username = username.trim(),
                ),
            )
            when (r) {
                is ApiResult.Success -> _state.value = AuthUiState(
                    pendingPhone = true,
                    phoneChallenge = r.data,
                )
                is ApiResult.Failure -> _state.value = AuthUiState(error = r.localized())
            }
        }
    }

    fun requestPhoneLogin(phone: String) {
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            val r = authRepository.requestPhoneLogin(
                com.fazlaka.app.core.model.dto.RegisterPhoneRequest(phone = phone.trim(), username = ""),
            )
            when (r) {
                is ApiResult.Success -> _state.value = AuthUiState(
                    pendingPhone = true,
                    phoneChallenge = r.data,
                )
                is ApiResult.Failure -> _state.value = AuthUiState(error = r.localized())
            }
        }
    }

    fun completePhone(
        phone: String,
        verificationId: String,
        code: String,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            val r = authRepository.completePhoneAuth(
                com.fazlaka.app.core.model.dto.PhoneCompleteRequest(
                    phone = phone.trim(),
                    verificationId = verificationId,
                    code = code,
                ),
            )
            when (r) {
                is ApiResult.Success -> handleAuthResult(r.data, onDone)
                is ApiResult.Failure -> _state.value = AuthUiState(error = r.localized())
            }
        }
    }

    fun forgotPassword(email: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            when (val r = authRepository.forgotPassword(email.trim())) {
                is ApiResult.Success -> {
                    _state.value = AuthUiState(success = true)
                    onDone(true)
                }
                is ApiResult.Failure -> {
                    _state.value = AuthUiState(error = r.localized())
                    onDone(false)
                }
            }
        }
    }

    fun resetPassword(password: String, email: String?, token: String? = null, otp: String? = null, onDone: () -> Unit) {
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            val r = authRepository.resetPassword(
                com.fazlaka.app.core.model.dto.ResetPasswordRequest(
                    password = password,
                    email = email,
                    token = token,
                    otp = otp,
                ),
            )
            when (r) {
                is ApiResult.Success -> {
                    _state.value = AuthUiState(success = true)
                    onDone()
                }
                is ApiResult.Failure -> _state.value = AuthUiState(error = r.localized())
            }
        }
    }

    fun verifyEmail(email: String, otp: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            val r = authRepository.verifyEmail(
                com.fazlaka.app.core.model.dto.VerifyEmailRequest(
                    email = email.trim(),
                    otp = otp.trim(),
                ),
            )
            when (r) {
                is ApiResult.Success -> {
                    _state.value = AuthUiState(success = true)
                    onDone()
                }
                is ApiResult.Failure -> _state.value = AuthUiState(error = r.localized())
            }
        }
    }

    fun resendVerification(email: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            val r = authRepository.resendVerification(
                com.fazlaka.app.core.model.dto.ResendVerificationRequest(email = email.trim()),
            )
            when (r) {
                is ApiResult.Success -> {
                    _state.value = AuthUiState(success = true)
                    onDone(true)
                }
                is ApiResult.Failure -> {
                    _state.value = AuthUiState(error = r.localized())
                    onDone(false)
                }
            }
        }
    }

    fun acceptTermsAndContinue(username: String?, onDone: () -> Unit) {
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            when (val r = authRepository.acceptTerms(username)) {
                is ApiResult.Success -> handleAuthResult(r.data, onDone)
                is ApiResult.Failure -> _state.value = AuthUiState(error = r.localized())
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onDone()
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun handleAuthResult(result: AuthResultDto, onDone: () -> Unit) {
        if (result.requiresTwoFactor) {
            _state.value = AuthUiState(
                requiresTwoFactor = true,
                twoFactorEmail = result.email,
                twoFactorMethod = result.method,
            )
            return
        }
        if (result.pending) {
            _state.value = AuthUiState(
                pendingPhone = true,
                justRegistered = true,
            )
            return
        }
        viewModelScope.launch {
            val ok = authRepository.persistSession(result)
            if (ok) {
                pushRepository.fetchAndRegisterToken()
                _state.value = AuthUiState(success = true)
                onDone()
            } else {
                _state.value = AuthUiState(error = "حدث خطأ أثناء حفظ الجلسة")
            }
        }
    }
}

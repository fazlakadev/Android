package com.fazlaka.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fazlaka.app.core.common.ErrorMessages.localized
import com.fazlaka.app.core.model.dto.SessionDto
import com.fazlaka.app.core.model.dto.SuccessDto
import com.fazlaka.app.core.model.dto.TotpSetupDto
import com.fazlaka.app.core.model.dto.UserDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SecurityUiState(
    val loading: Boolean = false,
    val sessions: ApiResult<List<SessionDto>>? = null,
    val changingPassword: Boolean = false,
    val totp: ApiResult<TotpSetupDto>? = null,
    val totpBusy: Boolean = false,
    val twoFactorEnabled: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SecurityUiState())
    val state: StateFlow<SecurityUiState> = _state.asStateFlow()

    init {
        loadSessions()
        refreshTwoFactorStatus()
    }

    fun loadSessions() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            _state.value = _state.value.copy(
                sessions = authRepository.sessions(),
                loading = false,
            )
        }
    }

    private fun refreshTwoFactorStatus() {
        viewModelScope.launch {
            val enabled = (authRepository.me() as? ApiResult.Success)?.data?.twoFactorEnabled
            if (enabled != null) {
                _state.value = _state.value.copy(twoFactorEnabled = enabled)
            }
        }
    }

    fun changePassword(current: String, new: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(changingPassword = true, error = null)
            when (val r = authRepository.changePassword(current, new)) {
                is ApiResult.Success -> _state.value =
                    _state.value.copy(changingPassword = false, message = "تم تغيير كلمة المرور")
                is ApiResult.Failure -> _state.value =
                    _state.value.copy(changingPassword = false, error = r.localized())
            }
        }
    }

    fun revokeSession(id: String) {
        viewModelScope.launch {
            authRepository.revokeSession(id)
            loadSessions()
        }
    }

    fun revokeOtherSessions() {
        viewModelScope.launch {
            authRepository.revokeOtherSessions()
            loadSessions()
        }
    }

    fun loadTotp() {
        viewModelScope.launch {
            _state.value = _state.value.copy(totpBusy = true, error = null)
            _state.value = _state.value.copy(totp = authRepository.setupTotp(), totpBusy = false)
        }
    }

    fun enableTotp(code: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(totpBusy = true, error = null)
            when (val r = authRepository.enableTotp(code)) {
                is ApiResult.Success -> _state.value = _state.value.copy(
                    totpBusy = false,
                    totp = null,
                    twoFactorEnabled = true,
                    message = "تم تفعيل التحقق بخطوتين",
                )
                is ApiResult.Failure -> _state.value =
                    _state.value.copy(totpBusy = false, error = r.localized())
            }
        }
    }

    fun disableTotp(code: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(totpBusy = true, error = null)
            when (val r = authRepository.disableTotp(code)) {
                is ApiResult.Success -> _state.value = _state.value.copy(
                    totpBusy = false,
                    totp = null,
                    twoFactorEnabled = false,
                    message = "تم تعطيل التحقق بخطوتين",
                )
                is ApiResult.Failure -> _state.value =
                    _state.value.copy(totpBusy = false, error = r.localized())
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(message = null, error = null)
    }
}

package com.fazlaka.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fazlaka.app.core.common.ErrorMessages.localized
import com.fazlaka.app.core.model.dto.AuthEventDto
import com.fazlaka.app.core.model.dto.LinkStartResultDto
import com.fazlaka.app.core.model.dto.LinkStatusDto
import com.fazlaka.app.core.model.dto.Paginated
import com.fazlaka.app.core.model.dto.PreferenceDto
import com.fazlaka.app.core.model.dto.SuccessDto
import com.fazlaka.app.core.model.dto.UpdatePreferencesRequest
import com.fazlaka.app.core.model.dto.UserDto
import com.fazlaka.app.core.model.dto.UserEmailDto
import com.fazlaka.app.core.model.dto.UserEmailsDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.data.repository.AuthRepository
import com.fazlaka.app.data.repository.ProfileRepository
import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ---------------------------------------------------------------------------
// Activity / security events log
// ---------------------------------------------------------------------------

data class ActivityLogUiState(
    val events: List<AuthEventDto> = emptyList(),
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val page: Int = 1,
    val endReached: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ActivityLogViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ActivityLogUiState())
    val state: StateFlow<ActivityLogUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, page = 1)
            when (val r = authRepository.securityEvents(page = 1, limit = 30)) {
                is ApiResult.Success -> {
                    val items = r.data.data
                    _state.value = _state.value.copy(
                        events = items,
                        loading = false,
                        endReached = items.isEmpty() || !r.data.meta.hasNextPage,
                    )
                }
                is ApiResult.Failure -> _state.value = _state.value.copy(
                    loading = false,
                    error = r.localized(),
                )
            }
        }
    }

    fun loadMore() {
        val s = _state.value
        if (s.loading || s.loadingMore || s.endReached) return
        viewModelScope.launch {
            _state.value = s.copy(loadingMore = true)
            val next = s.page + 1
            when (val r = authRepository.securityEvents(page = next, limit = 30)) {
                is ApiResult.Success -> {
                    val items = r.data.data
                    _state.value = _state.value.copy(
                        events = _state.value.events + items,
                        loadingMore = false,
                        page = next,
                        endReached = items.isEmpty() || !r.data.meta.hasNextPage,
                    )
                }
                is ApiResult.Failure -> _state.value = _state.value.copy(loadingMore = false)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Secondary emails management
// ---------------------------------------------------------------------------

data class SecondaryEmailsUiState(
    val emails: UserEmailsDto? = null,
    val loading: Boolean = false,
    val busy: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

@HiltViewModel
class SecondaryEmailsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SecondaryEmailsUiState())
    val state: StateFlow<SecondaryEmailsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            when (val r = authRepository.userEmails()) {
                is ApiResult.Success -> _state.value =
                    _state.value.copy(loading = false, emails = r.data)
                is ApiResult.Failure -> _state.value =
                    _state.value.copy(loading = false, error = r.localized())
            }
        }
    }

    fun add(email: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, error = null)
            when (val r = authRepository.addUserEmail(email.trim())) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        busy = false,
                        message = "تمت الإضافة — تحقق من بريدك للحصول على رمز التفعيل",
                    )
                    load()
                }
                is ApiResult.Failure -> _state.value =
                    _state.value.copy(busy = false, error = r.localized())
            }
        }
    }

    fun verify(email: String, otp: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, error = null)
            when (val r = authRepository.verifyUserEmail(email, otp)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(busy = false, message = "تم توثيق البريد")
                    load()
                }
                is ApiResult.Failure -> _state.value =
                    _state.value.copy(busy = false, error = r.localized())
            }
        }
    }

    fun requestPrimary(email: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, error = null)
            when (val r = authRepository.requestPrimaryEmail(email)) {
                is ApiResult.Success -> _state.value = _state.value.copy(
                    busy = false,
                    message = "أرسلنا رمز تأكيد إلى $email",
                )
                is ApiResult.Failure -> _state.value =
                    _state.value.copy(busy = false, error = r.localized())
            }
        }
    }

    fun makePrimary(email: String, otp: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, error = null)
            when (val r = authRepository.makePrimaryEmail(email, otp)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        busy = false,
                        message = "أصبح $email بريدك الأساسي — سيتم تسجيل خروجك من باقي الأجهزة",
                    )
                    load()
                }
                is ApiResult.Failure -> _state.value =
                    _state.value.copy(busy = false, error = r.localized())
            }
        }
    }

    fun remove(email: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, error = null)
            when (val r = authRepository.removeUserEmail(email)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(busy = false, message = "تم حذف البريد")
                    load()
                }
                is ApiResult.Failure -> _state.value =
                    _state.value.copy(busy = false, error = r.localized())
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(message = null, error = null)
    }
}

// ---------------------------------------------------------------------------
// Linked accounts (Google / GitHub / Facebook / phone)
// ---------------------------------------------------------------------------

data class LinkedAccountsUiState(
    val status: LinkStatusDto? = null,
    val user: UserDto? = null,
    val loading: Boolean = false,
    val busy: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    /** Set while the OAuth link WebView should be shown. */
    val linkUrl: String? = null,
    val linkProvider: String? = null,
)

@HiltViewModel
class LinkedAccountsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LinkedAccountsUiState())
    val state: StateFlow<LinkedAccountsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val status = authRepository.linkStatus()
                val me = authRepository.me()
                _state.value = _state.value.copy(
                    loading = false,
                    status = (status as? ApiResult.Success)?.data ?: _state.value.status,
                    user = (me as? ApiResult.Success)?.data ?: _state.value.user,
                    error = when (status) {
                        is ApiResult.Failure -> status.localized()
                        is ApiResult.Success -> (me as? ApiResult.Failure)?.localized()
                    },
                )
            } catch (e: Exception) {
                Log.e("LinkedAccountsVM", "load failed", e)
                _state.value = _state.value.copy(
                    loading = false,
                    error = "تعذر تحميل البيانات — حاول مرة أخرى",
                )
            }
        }
    }

    fun unlink(provider: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, error = null)
            try {
                when (val r = authRepository.unlinkProvider(provider)) {
                    is ApiResult.Success -> {
                        _state.value = _state.value.copy(
                            busy = false,
                            status = r.data,
                            message = "تم فك الربط",
                        )
                    }
                    is ApiResult.Failure -> _state.value =
                        _state.value.copy(busy = false, error = r.localized())
                }
            } catch (e: Exception) {
                Log.e("LinkedAccountsVM", "unlink failed", e)
                _state.value = _state.value.copy(busy = false, error = "خطأ غير متوقع")
            }
        }
    }

    fun removePhone() {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, error = null)
            try {
                when (val r = authRepository.removePhone()) {
                    is ApiResult.Success -> {
                        _state.value = _state.value.copy(busy = false, message = "تم فك ربط الهاتف")
                        load()
                    }
                    is ApiResult.Failure -> _state.value =
                        _state.value.copy(busy = false, error = r.localized())
                }
            } catch (e: Exception) {
                Log.e("LinkedAccountsVM", "removePhone failed", e)
                _state.value = _state.value.copy(busy = false, error = "خطأ غير متوقع")
            }
        }
    }

    /** Password-confirmed link start; on success exposes the WebView URL. */
    fun startLink(provider: String, password: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, error = null)
            try {
                val (result, cookie) = authRepository.startOauthLink(provider, password)
                applyLinkResult(provider, result, cookie)
            } catch (e: Exception) {
                Log.e("LinkedAccountsVM", "startLink failed", e)
                _state.value = _state.value.copy(busy = false, error = "خطأ غير متوقع أثناء الربط")
            }
        }
    }

    /** OTP confirmation for passwordless accounts, then opens WebView. */
    fun confirmLinkOtp(provider: String, otp: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, error = null)
            try {
                val (result, cookie) = authRepository.confirmOauthLinkOtp(provider, otp)
                applyLinkResult(provider, result, cookie)
            } catch (e: Exception) {
                Log.e("LinkedAccountsVM", "confirmLinkOtp failed", e)
                _state.value = _state.value.copy(busy = false, error = "خطأ غير متوقع أثناء التأكيد")
            }
        }
    }

    private fun applyLinkResult(
        provider: String,
        result: ApiResult<LinkStartResultDto>,
        cookie: String?,
    ) {
        when (result) {
            is ApiResult.Success -> {
                val data = result.data
                when {
                    data.requiresOtp -> _state.value = _state.value.copy(
                        busy = false,
                        message = "otp-required",
                    )
                    data.redirectUrl != null -> {
                        linkCookie = cookie
                        _state.value = _state.value.copy(
                            busy = false,
                            linkUrl = data.redirectUrl,
                            linkProvider = provider,
                        )
                    }
                    else -> _state.value = _state.value.copy(busy = false)
                }
            }
            is ApiResult.Failure -> _state.value =
                _state.value.copy(busy = false, error = result.localized())
        }
    }

    var linkCookie: String? = null
        private set

    fun closeWebView(linked: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                linkUrl = null,
                linkProvider = null,
                message = if (linked) "تم ربط الحساب بنجاح" else null,
            )
            if (linked) load()
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(message = null, error = null)
    }
}

// ---------------------------------------------------------------------------
// Change primary email
// ---------------------------------------------------------------------------

data class ChangeEmailUiState(
    val busy: Boolean = false,
    val step: Int = 1,
    val currentEmail: String? = null,
    val message: String? = null,
    val error: String? = null,
    val done: Boolean = false,
)

@HiltViewModel
class ChangeEmailViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ChangeEmailUiState())
    val state: StateFlow<ChangeEmailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val me = (authRepository.me() as? ApiResult.Success)?.data
            _state.value = _state.value.copy(currentEmail = me?.email)
        }
    }

    fun request(newEmail: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, error = null)
            when (val r = authRepository.requestChangeEmail(newEmail.trim())) {
                is ApiResult.Success -> _state.value = _state.value.copy(
                    busy = false,
                    step = 2,
                    message = "أرسلنا رمز التأكيد إلى بريدك الحالي",
                )
                is ApiResult.Failure -> _state.value =
                    _state.value.copy(busy = false, error = r.localized())
            }
        }
    }

    fun confirm(newEmail: String, otp: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, error = null)
            when (val r = authRepository.confirmChangeEmail(newEmail.trim(), otp)) {
                is ApiResult.Success -> _state.value = _state.value.copy(
                    busy = false,
                    done = true,
                    message = "تم تغيير بريدك الأساسي — تحقق من بريدك الجديد لتوثيقه",
                )
                is ApiResult.Failure -> _state.value =
                    _state.value.copy(busy = false, error = r.localized())
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(message = null, error = null)
    }
}

// ---------------------------------------------------------------------------
// Login alerts preference toggle (used by the security hub)
// ---------------------------------------------------------------------------

data class LoginAlertsUiState(
    val loginAlerts: Boolean = true,
    val emailNotifications: Boolean = true,
    val busy: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

@HiltViewModel
class LoginAlertsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginAlertsUiState())
    val state: StateFlow<LoginAlertsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            when (val r = profileRepository.preferences()) {
                is ApiResult.Success -> _state.value = _state.value.copy(
                    loginAlerts = r.data.loginAlerts,
                    emailNotifications = r.data.emailNotifications,
                )
                is ApiResult.Failure -> Unit
            }
        }
    }

    fun setLoginAlerts(enabled: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, loginAlerts = enabled)
            when (val r = profileRepository.updatePreferences(
                UpdatePreferencesRequest(loginAlerts = enabled),
            )) {
                is ApiResult.Success<PreferenceDto> -> _state.value = _state.value.copy(
                    busy = false,
                    message = if (enabled) "سيصلك إيميل عند كل تسجيل دخول" else "تم إيقاف تنبيهات الدخول",
                )
                is ApiResult.Failure -> _state.value = _state.value.copy(
                    busy = false,
                    loginAlerts = !enabled,
                    error = r.localized(),
                )
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(message = null, error = null)
    }
}

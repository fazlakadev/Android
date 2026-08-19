package com.fazlaka.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fazlaka.app.core.datastore.SessionManager
import com.fazlaka.app.core.location.locationProvider
import com.fazlaka.app.core.model.dto.GeolocationRequest
import com.fazlaka.app.core.model.dto.UpdatePreferencesRequest
import com.fazlaka.app.core.notification.PushRepository
import com.fazlaka.app.core.realtime.RealtimeManager
import com.fazlaka.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class SessionUiState(
    val isLoggedIn: Boolean = false,
    val onboarded: Boolean = false,
    val darkMode: Boolean = true,
    val locale: String = "ar",
    val startDestination: String = "splash",
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val session: SessionManager,
    private val profileRepository: ProfileRepository,
    @ApplicationContext private val appContext: Context,
    @Suppress("unused") private val realtime: RealtimeManager,
    private val pushRepository: PushRepository,
) : ViewModel() {

    val uiState: StateFlow<SessionUiState> = combine(
        session.isLoggedIn,
        session.onboarded,
        session.darkMode,
        session.locale,
    ) { loggedIn, onboarded, dark, locale ->
        SessionUiState(
            isLoggedIn = loggedIn,
            onboarded = onboarded,
            darkMode = dark,
            locale = locale,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SessionUiState())

    init {
        // Always fetch & log the FCM token on startup (debug visibility)
        viewModelScope.launch {
            try {
                val token = com.google.firebase.messaging.FirebaseMessaging.getInstance().token.await()
                android.util.Log.d("FCM", "========= FCM Registration Token =========")
                android.util.Log.d("FCM", token)
                android.util.Log.d("FCM", "===========================================")
            } catch (e: Exception) {
                android.util.Log.e("FCM", "Failed to fetch FCM token", e)
            }
        }
        // Sync preferences + report device geolocation once the user is signed in.
        viewModelScope.launch {
            session.isLoggedIn.collect { loggedIn ->
                if (loggedIn) {
                    syncPreferencesFromServer()
                    reportGeolocation()
                    pushRepository.fetchAndRegisterToken()
                }
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            session.setDarkMode(enabled)
            pushPreference(UpdatePreferencesRequest(darkMode = enabled))
        }
    }

    fun setLocale(locale: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            session.setLocale(locale)
            pushPreference(UpdatePreferencesRequest(locale = locale))
            onDone()
        }
    }

    /** Pulls server-side preference overrides (dark mode / locale) on login. */
    private suspend fun syncPreferencesFromServer() {
        val prefs = (profileRepository.preferences() as? com.fazlaka.app.core.network.ApiResult.Success)?.data
            ?: return
        session.setDarkMode(prefs.darkMode)
        session.setLocale(prefs.locale)
    }

    private suspend fun pushPreference(request: UpdatePreferencesRequest) {
        runCatching { profileRepository.updatePreferences(request) }
    }

    /** Best-effort GPS report to the backend after login (security audit trail). */
    private suspend fun reportGeolocation() {
        if (!locationProvider(appContext).hasPermission()) return
        runCatching {
            val location = locationProvider(appContext).refreshOnce() ?: return
            profileRepository.saveGeolocation(
                GeolocationRequest(
                    lat = location.latitude,
                    lng = location.longitude,
                ),
            )
        }
    }
}

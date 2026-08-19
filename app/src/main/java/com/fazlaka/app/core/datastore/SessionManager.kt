package com.fazlaka.app.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fazlaka.app.core.model.dto.AuthResultDto
import com.fazlaka.app.core.model.dto.UserDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore by preferencesDataStore(name = "fazlaka_session")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        val KEY_ACCESS = stringPreferencesKey("access_token")
        val KEY_REFRESH = stringPreferencesKey("refresh_token")
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_NAME = stringPreferencesKey("name")
        val KEY_EMAIL = stringPreferencesKey("email")
        val KEY_AVATAR = stringPreferencesKey("avatar_url")
        val KEY_LOCALE = stringPreferencesKey("locale")
        val KEY_DARK = booleanPreferencesKey("dark_mode")
        val KEY_ONBOARDED = booleanPreferencesKey("onboarded")

        private const val LOCALE_PREFS = "fazlaka_locale_sync"
        private const val LOCALE_KEY = "locale"
    }

    val accessToken: Flow<String?> = context.sessionDataStore.data.map { it[KEY_ACCESS] }
    val refreshToken: Flow<String?> = context.sessionDataStore.data.map { it[KEY_REFRESH] }

    val locale: Flow<String> = context.sessionDataStore.data.map { it[KEY_LOCALE] ?: "ar" }
    val darkMode: Flow<Boolean> = context.sessionDataStore.data.map { it[KEY_DARK] ?: true }
    val onboarded: Flow<Boolean> = context.sessionDataStore.data.map { it[KEY_ONBOARDED] ?: false }
    val isLoggedIn: Flow<Boolean> = context.sessionDataStore.data.map { !it[KEY_ACCESS].isNullOrEmpty() }

    val currentUserId: Flow<String?> = context.sessionDataStore.data.map { it[KEY_USER_ID] }
    val currentUserName: Flow<String?> = context.sessionDataStore.data.map { it[KEY_NAME] }
    val currentUsername: Flow<String?> = context.sessionDataStore.data.map { it[KEY_USERNAME] }
    val currentEmail: Flow<String?> = context.sessionDataStore.data.map { it[KEY_EMAIL] }
    val currentAvatar: Flow<String?> = context.sessionDataStore.data.map { it[KEY_AVATAR] }

    data class SessionUser(
        val id: String?,
        val username: String?,
        val name: String?,
        val email: String?,
        val avatarUrl: String?,
    )

    fun currentUserFlow(): Flow<SessionUser> =
        context.sessionDataStore.data.map { prefs ->
            SessionUser(
                id = prefs[KEY_USER_ID],
                username = prefs[KEY_USERNAME],
                name = prefs[KEY_NAME],
                email = prefs[KEY_EMAIL],
                avatarUrl = prefs[KEY_AVATAR],
            )
        }

    suspend fun saveSession(auth: AuthResultDto) {
        val user = auth.user
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_ACCESS] = auth.accessToken.orEmpty()
            prefs[KEY_REFRESH] = auth.refreshToken.orEmpty()
            if (user != null) saveUserFields(prefs, user)
        }
    }

    suspend fun updateUser(user: UserDto) {
        context.sessionDataStore.edit { prefs -> saveUserFields(prefs, user) }
    }

    suspend fun updateTokens(access: String?, refresh: String?) {
        context.sessionDataStore.edit { prefs ->
            if (access != null) prefs[KEY_ACCESS] = access
            if (refresh != null) prefs[KEY_REFRESH] = refresh
        }
    }

    suspend fun setLocale(value: String) {
        context.sessionDataStore.edit { it[KEY_LOCALE] = value }
        // Synchronous mirror so MainActivity.attachBaseContext can read the
        // locale before DataStore is available (instant locale on recreate).
        context
            .getSharedPreferences(LOCALE_PREFS, android.content.Context.MODE_PRIVATE)
            .edit()
            .putString(LOCALE_KEY, value)
            .commit()
    }

    /** Instant locale read for activity attach (defaults to Arabic). */
    fun localeSync(): String =
        context
            .getSharedPreferences(LOCALE_PREFS, android.content.Context.MODE_PRIVATE)
            .getString(LOCALE_KEY, "ar") ?: "ar"

    suspend fun setDarkMode(value: Boolean) {
        context.sessionDataStore.edit { it[KEY_DARK] = value }
    }

    suspend fun setOnboarded(value: Boolean) {
        context.sessionDataStore.edit { it[KEY_ONBOARDED] = value }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { it.clear() }
    }

    suspend fun accessTokenValue(): String? = accessToken.first()

    suspend fun refreshTokenValue(): String? = refreshToken.first()

    suspend fun localeValue(): String = locale.first()

    private fun saveUserFields(
        prefs: androidx.datastore.preferences.core.MutablePreferences,
        user: UserDto,
    ) {
        prefs[KEY_USER_ID] = user.id
        prefs[KEY_USERNAME] = user.username
        prefs[KEY_NAME] = user.name
        prefs[KEY_EMAIL] = user.email.orEmpty()
        prefs[KEY_AVATAR] = user.avatarUrl.orEmpty()
        user.locale.takeIf { it.isNotBlank() }?.let { prefs[KEY_LOCALE] = it }
        if (!user.onboardedAt.isNullOrEmpty()) prefs[KEY_ONBOARDED] = true
    }
}

package com.fazlaka.app.core.network

import com.fazlaka.app.core.datastore.SessionManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp Authenticator: on 401, exchanges the refresh token for a new pair
 * and retries the original request. Public endpoints (login/register) are
 * never retried.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val sessionManager: SessionManager,
    private val refreshApi: RefreshApi,
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != 401) return null
        val url = response.request.url.toString()
        if (url.contains("/auth/login") ||
            url.contains("/auth/register") ||
            url.contains("/auth/refresh") ||
            url.contains("/auth/phone/login") ||
            url.contains("/auth/forgot-password") ||
            url.contains("/auth/reset-password")
        ) {
            return null
        }

        val newToken = runBlocking {
            mutex.withLock {
                val refresh = sessionManager.refreshTokenValue()
                    ?: return@withLock null
                val result = runCatching { refreshApi.refresh(RefreshRequest(refresh)) }.getOrNull()
                val pair = result?.data
                val access = pair?.accessToken
                val newRefresh = pair?.refreshToken
                if (access.isNullOrEmpty()) {
                    sessionManager.clearSession()
                    null
                } else {
                    sessionManager.updateTokens(access, newRefresh ?: refresh)
                    access
                }
            }
        } ?: return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .build()
    }
}

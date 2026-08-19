package com.fazlaka.app.core.network

import com.fazlaka.app.BuildConfig
import com.fazlaka.app.core.datastore.SessionManager
import com.fazlaka.app.core.location.LocationProvider
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Adds the bearer token, locale and geo headers on top of the static
 * platform headers (see [PlatformHeadersInterceptor]).
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager,
    private val locationProvider: LocationProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { runCatching { sessionManager.accessTokenValue() }.getOrNull() }
        val locale = runBlocking { runCatching { sessionManager.localeValue() }.getOrNull() } ?: "ar"

        val builder = PlatformHeadersInterceptor.withPlatformHeaders(chain.request().newBuilder())
            .header("Accept-Language", locale)

        val lat = locationProvider.lat
        val lng = locationProvider.lng
        if (lat != null && lng != null) {
            builder.header("x-lat", lat.toString())
            builder.header("x-lng", lng.toString())
        }

        if (!token.isNullOrEmpty()) {
            builder.header("Authorization", "Bearer $token")
        }
        return chain.proceed(builder.build())
    }
}

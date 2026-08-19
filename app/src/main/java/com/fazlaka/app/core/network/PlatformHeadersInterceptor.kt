package com.fazlaka.app.core.network

import com.fazlaka.app.BuildConfig
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Static platform/device headers identifying the Android client.
 * Safe to use on ANY client (including the refresh client) because it
 * never touches the session or performs blocking reads.
 */
class PlatformHeadersInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = withPlatformHeaders(chain.request().newBuilder()).build()
        return chain.proceed(request)
    }

    companion object {
        fun withPlatformHeaders(builder: Request.Builder): Request.Builder = builder
            .header("x-platform", "MOBILE")
            .header("x-device-type", "mobile")
            .header("x-os", "Android ${android.os.Build.VERSION.RELEASE}")
            .header("x-device-name", "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
            .header("x-app-version", BuildConfig.VERSION_NAME)
            .header("Accept", "application/json")
    }
}

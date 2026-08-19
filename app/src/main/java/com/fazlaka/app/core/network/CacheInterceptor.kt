package com.fazlaka.app.core.network

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline interceptor: serves stale cached responses (up to 7 days)
 * regardless of network status, so the app always has something to show.
 */
@Singleton
class OfflineCacheInterceptor @Inject constructor(
    private val networkMonitor: NetworkMonitor,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val isOnline = networkMonitor.status.value != NetworkStatus.OFFLINE

        if (!isOnline) {
            val cacheControl = CacheControl.Builder()
                .maxStale(7, TimeUnit.DAYS)
                .onlyIfCached()
                .build()
            request = request.newBuilder()
                .cacheControl(cacheControl)
                .build()
        }

        return chain.proceed(request)
    }
}

/**
 * Network interceptor: caches GET responses for 5 minutes.
 */
class CacheInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        val cacheControl = CacheControl.Builder()
            .maxAge(5, TimeUnit.MINUTES)
            .build()

        return response.newBuilder()
            .removeHeader("Pragma")
            .removeHeader("Cache-Control")
            .header("Cache-Control", cacheControl.toString())
            .build()
    }
}

object HttpCache {
    private const val CACHE_SIZE = 20L * 1024 * 1024 // 20 MB
    private const val DIR_NAME = "http_cache"

    fun create(context: Context): Cache {
        val cacheDir = File(context.cacheDir, DIR_NAME)
        return Cache(cacheDir, CACHE_SIZE)
    }
}

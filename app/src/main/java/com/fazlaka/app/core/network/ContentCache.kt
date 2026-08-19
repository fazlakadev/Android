package com.fazlaka.app.core.network

import android.util.LruCache
import javax.inject.Inject
import javax.inject.Singleton

data class CacheEntry<T>(
    val data: T,
    val timestamp: Long = System.currentTimeMillis(),
) {
    fun isFresh(maxAgeMs: Long): Boolean =
        System.currentTimeMillis() - timestamp < maxAgeMs

    fun age(): Long = System.currentTimeMillis() - timestamp
}

@Singleton
class ContentCache @Inject constructor() {
    private val cache = LruCache<String, CacheEntry<Any>>(50)

    companion object {
        const val FRESH_5MIN = 5 * 60 * 1000L
        const val FRESH_15MIN = 15 * 60 * 1000L
        const val STALE_7DAYS = 7 * 24 * 60 * 60 * 1000L
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? {
        val entry = cache.get(key) ?: return null
        if (!entry.isFresh(STALE_7DAYS)) {
            cache.remove(key)
            return null
        }
        return entry.data as? T
    }

    fun <T> put(key: String, data: T) {
        cache.put(key, CacheEntry(data as Any))
    }

    fun remove(key: String) {
        cache.remove(key)
    }

    fun isFresh(key: String, maxAgeMs: Long = FRESH_5MIN): Boolean {
        val entry = cache.get(key) ?: return false
        return entry.isFresh(maxAgeMs)
    }

    fun clear() {
        cache.evictAll()
    }
}

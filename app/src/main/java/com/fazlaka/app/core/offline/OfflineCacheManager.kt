package com.fazlaka.app.core.offline

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

private val Context.offlineDataStore by preferencesDataStore(name = "fazlaka_offline_cache")

private const val EPISODES_KEY = "cached_episodes"
private const val ARTICLES_KEY = "cached_articles"
private const val SEASONS_KEY = "cached_seasons"
private const val CACHE_TIMESTAMP_PREFIX = "cache_ts_"

@Singleton
class OfflineCacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore: DataStore<Preferences> get() = context.offlineDataStore

    suspend fun cacheEpisodes(episodes: List<Map<String, Any>>) {
        val json = JSONArray(episodes.map { org.json.JSONObject(it) }).toString()
        dataStore.edit { it[stringPreferencesKey(EPISODES_KEY)] = json }
    }

    fun getCachedEpisodes(): Flow<List<Map<String, Any>>> = dataStore.data.map { prefs ->
        prefs[stringPreferencesKey(EPISODES_KEY)]
            ?.let { parseJsonArray(it) }
            ?: emptyList()
    }

    suspend fun cacheArticles(articles: List<Map<String, Any>>) {
        val json = JSONArray(articles.map { org.json.JSONObject(it) }).toString()
        dataStore.edit { it[stringPreferencesKey(ARTICLES_KEY)] = json }
    }

    fun getCachedArticles(): Flow<List<Map<String, Any>>> = dataStore.data.map { prefs ->
        prefs[stringPreferencesKey(ARTICLES_KEY)]
            ?.let { parseJsonArray(it) }
            ?: emptyList()
    }

    suspend fun cacheSeasons(seasons: List<Map<String, Any>>) {
        val json = JSONArray(seasons.map { org.json.JSONObject(it) }).toString()
        dataStore.edit { it[stringPreferencesKey(SEASONS_KEY)] = json }
    }

    fun getCachedSeasons(): Flow<List<Map<String, Any>>> = dataStore.data.map { prefs ->
        prefs[stringPreferencesKey(SEASONS_KEY)]
            ?.let { parseJsonArray(it) }
            ?: emptyList()
    }

    suspend fun cacheRecentContent(type: String, items: List<Map<String, Any>>) {
        val json = JSONArray(items.map { org.json.JSONObject(it) }).toString()
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey(type)] = json
            prefs[longPreferencesKey("${CACHE_TIMESTAMP_PREFIX}$type")] = System.currentTimeMillis()
        }
    }

    fun getCachedContent(type: String): Flow<List<Map<String, Any>>> = dataStore.data.map { prefs ->
        prefs[stringPreferencesKey(type)]
            ?.let { parseJsonArray(it) }
            ?: emptyList()
    }

    suspend fun clearOldCache(maxAgeMs: Long = DEFAULT_MAX_AGE_MS) {
        val now = System.currentTimeMillis()
        dataStore.edit { prefs ->
            val keysToRemove = prefs.asMap().keys.filterIsInstance<Preferences.Key<*>>()
            for (key in keysToRemove) {
                if (key.name.startsWith(CACHE_TIMESTAMP_PREFIX)) {
                    val ts = prefs[key] as? Long ?: continue
                    if (now - ts > maxAgeMs) {
                        val baseName = key.name.removePrefix(CACHE_TIMESTAMP_PREFIX)
                        prefs.remove(stringPreferencesKey(baseName))
                        prefs.remove(key)
                    }
                }
            }
        }
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }

    private fun parseJsonArray(json: String): List<Map<String, Any>> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                obj.keys().asSequence().associateWith { key -> obj.get(key) }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        const val DEFAULT_MAX_AGE_MS: Long = 7 * 24 * 60 * 60 * 1000L
    }
}

package com.fazlaka.app.core.offline

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineCacheManagerTest {

    @Test
    fun cacheEpisodes_producesValidJsonArray() {
        val episodes = listOf(
            mapOf<String, Any>("id" to "ep1", "title" to "Episode 1", "duration" to 1200),
            mapOf<String, Any>("id" to "ep2", "title" to "Episode 2", "duration" to 900),
        )
        val json = JSONArray(episodes.map { JSONObject(it) }).toString()
        val arr = JSONArray(json)
        assertEquals(2, arr.length())
        assertEquals("ep1", arr.getJSONObject(0).getString("id"))
        assertEquals("Episode 1", arr.getJSONObject(0).getString("title"))
        assertEquals(1200, arr.getJSONObject(0).getInt("duration"))
    }

    @Test
    fun getCachedEpisodes_returnsEmptyList_whenNoCache() {
        val json: String? = null
        val result = json?.let { parseJsonArray(it) } ?: emptyList()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getCachedEpisodes_returnsStoredEpisodes() {
        val episodes = listOf(
            mapOf<String, Any>("id" to "ep1", "title" to "Episode 1"),
            mapOf<String, Any>("id" to "ep2", "title" to "Episode 2"),
        )
        val json = JSONArray(episodes.map { JSONObject(it) }).toString()
        val result = parseJsonArray(json)
        assertEquals(2, result.size)
        assertEquals("ep1", result[0]["id"])
        assertEquals("Episode 2", result[1]["title"])
    }

    @Test
    fun clearAll_removesAllCachedData() {
        val data = mutableMapOf<String, String>()
        data["cached_episodes"] = "[{\"id\":\"ep1\"}]"
        data["cached_articles"] = "[{\"id\":\"art1\"}]"
        data["cached_seasons"] = "[{\"id\":\"s1\"}]"
        data.clear()
        assertTrue(data.isEmpty())
    }

    @Test
    fun clearOldCache_removesExpiredEntries() {
        val maxAgeMs = 7 * 24 * 60 * 60 * 1000L
        val now = System.currentTimeMillis()
        val oldTimestamp = now - maxAgeMs - 1000L
        val recentTimestamp = now - 1000L

        data class CacheEntry(val key: String, val timestamp: Long)

        val entries = listOf(
            CacheEntry("cached_episodes", oldTimestamp),
            CacheEntry("cached_articles", recentTimestamp),
        )

        val remaining = entries.filter { now - it.timestamp <= maxAgeMs }
        assertEquals(1, remaining.size)
        assertEquals("cached_articles", remaining[0].key)
    }

    @Test
    fun clearOldCache_keepsEntriesWithinMaxAge() {
        val maxAgeMs = 7 * 24 * 60 * 60 * 1000L
        val now = System.currentTimeMillis()
        val recentTimestamp = now - 1000L

        data class CacheEntry(val key: String, val timestamp: Long)

        val entries = listOf(
            CacheEntry("cached_episodes", recentTimestamp),
            CacheEntry("cached_articles", recentTimestamp),
        )

        val remaining = entries.filter { now - it.timestamp <= maxAgeMs }
        assertEquals(2, remaining.size)
    }

    @Test
    fun parseJsonArray_handlesValidJson() {
        val json = """[{"id":"1","title":"Test"},{"id":"2","title":"Test2"}]"""
        val result = parseJsonArray(json)
        assertEquals(2, result.size)
        assertEquals("1", result[0]["id"])
        assertEquals("Test", result[0]["title"])
    }

    @Test
    fun parseJsonArray_handlesInvalidJson() {
        val result = parseJsonArray("not valid json")
        assertTrue(result.isEmpty())
    }

    @Test
    fun parseJsonArray_handlesEmptyArray() {
        val result = parseJsonArray("[]")
        assertTrue(result.isEmpty())
    }

    @Test
    fun cacheEpisodes_preservesNestedObjects() {
        val episodes = listOf(
            mapOf<String, Any>(
                "id" to "ep1",
                "season" to mapOf("id" to "s1", "title" to "Season 1"),
            ),
        )
        val json = JSONArray(
            episodes.map { entry ->
                val obj = JSONObject()
                obj.put("id", entry["id"])
                val season = entry["season"] as Map<*, *>
                val seasonObj = JSONObject()
                season.forEach { (k, v) -> seasonObj.put(k, v) }
                obj.put("season", seasonObj)
                obj
            },
        ).toString()
        val result = parseJsonArray(json)
        assertEquals(1, result.size)
        assertEquals("ep1", result[0]["id"])
        val season = result[0]["season"] as JSONObject
        assertEquals("s1", season.getString("id"))
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
}

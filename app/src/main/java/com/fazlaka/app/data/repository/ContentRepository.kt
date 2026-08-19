package com.fazlaka.app.data.repository

import com.fazlaka.app.core.database.LocalProgressDao
import com.fazlaka.app.core.database.LocalProgressEntity
import com.fazlaka.app.core.database.SearchHistoryDao
import com.fazlaka.app.core.database.SearchHistoryEntity
import com.fazlaka.app.core.datastore.SessionManager
import com.fazlaka.app.core.model.dto.ArticleDto
import com.fazlaka.app.core.model.dto.BannerDto
import com.fazlaka.app.core.model.dto.CreatePlaylistRequest
import com.fazlaka.app.core.model.dto.EpisodeDto
import com.fazlaka.app.core.model.dto.Paginated
import com.fazlaka.app.core.model.dto.PlaylistDto
import com.fazlaka.app.core.model.dto.PlaylistItemRequest
import com.fazlaka.app.core.model.dto.ProgressItemDto
import com.fazlaka.app.core.model.dto.ProgressRequest
import com.fazlaka.app.core.model.dto.RecommendationsDto
import com.fazlaka.app.core.model.dto.RelatedEpisodesDto
import com.fazlaka.app.core.model.dto.SearchResponseDto
import com.fazlaka.app.core.model.dto.SeasonDto
import com.fazlaka.app.core.model.dto.SuccessDto
import com.fazlaka.app.core.model.dto.SuggestionsDto
import com.fazlaka.app.core.model.dto.ViewTrackRequest
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.core.network.ApiService
import com.fazlaka.app.core.network.ContentCache
import com.fazlaka.app.core.network.NetworkMonitor
import com.fazlaka.app.core.network.OfflineManager
import com.fazlaka.app.core.network.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepository @Inject constructor(
    private val api: ApiService,
    private val session: SessionManager,
    private val json: Json,
    private val searchHistoryDao: SearchHistoryDao,
    private val localProgressDao: LocalProgressDao,
    private val cache: ContentCache,
    private val networkMonitor: NetworkMonitor,
    private val offlineManager: OfflineManager,
) {

    private suspend fun locale(): String = session.localeValue()

    // ---------------- Episodes ----------------
    suspend fun episodes(
        page: Int = 1,
        limit: Int = 20,
        seasonId: String? = null,
        search: String? = null,
        platform: String? = null,
    ): ApiResult<Paginated<EpisodeDto>> {
        val key = "episodes:${locale()}:$page:$limit:$seasonId:$search:$platform"
        val cached = cache.get<Paginated<EpisodeDto>>(key)
        if (cached != null && cache.isFresh(key)) {
            return ApiResult.Success(cached)
        }
        val result = safeApiCall({ api.episodes(locale(), page, limit, seasonId, search, platform) }, json)
        if (result is ApiResult.Success) cache.put(key, result.data)
        return result
    }

    suspend fun episode(idOrSlug: String): ApiResult<EpisodeDto> {
        val key = "episode:${locale()}:$idOrSlug"
        val cached = cache.get<EpisodeDto>(key)
        if (cached != null && cache.isFresh(key, ContentCache.FRESH_15MIN)) {
            return ApiResult.Success(cached)
        }
        val result = safeApiCall({ api.episode(idOrSlug, locale()) }, json)
        if (result is ApiResult.Success) cache.put(key, result.data)
        return result
    }

    suspend fun related(idOrSlug: String, limit: Int = 10): ApiResult<RelatedEpisodesDto> =
        safeApiCall({ api.relatedEpisodes(idOrSlug, locale(), limit) }, json)

    // ---------------- Seasons ----------------
    suspend fun seasons(page: Int = 1, limit: Int = 20): ApiResult<Paginated<SeasonDto>> {
        val key = "seasons:${locale()}:$page:$limit"
        val cached = cache.get<Paginated<SeasonDto>>(key)
        if (cached != null && cache.isFresh(key)) {
            return ApiResult.Success(cached)
        }
        val result = safeApiCall({ api.seasons(locale(), page, limit) }, json)
        if (result is ApiResult.Success) cache.put(key, result.data)
        return result
    }

    suspend fun season(idOrSlug: String): ApiResult<SeasonDto> {
        val key = "season:${locale()}:$idOrSlug"
        val cached = cache.get<SeasonDto>(key)
        if (cached != null && cache.isFresh(key, ContentCache.FRESH_15MIN)) {
            return ApiResult.Success(cached)
        }
        val result = safeApiCall({ api.season(idOrSlug, locale()) }, json)
        if (result is ApiResult.Success) cache.put(key, result.data)
        return result
    }

    // ---------------- Playlists ----------------
    suspend fun playlists(page: Int = 1, limit: Int = 20): ApiResult<Paginated<PlaylistDto>> {
        val key = "playlists:${locale()}:$page:$limit"
        val cached = cache.get<Paginated<PlaylistDto>>(key)
        if (cached != null && cache.isFresh(key)) {
            return ApiResult.Success(cached)
        }
        val result = safeApiCall({ api.playlists(locale(), page, limit) }, json)
        if (result is ApiResult.Success) cache.put(key, result.data)
        return result
    }

    suspend fun playlist(idOrSlug: String): ApiResult<PlaylistDto> {
        val key = "playlist:${locale()}:$idOrSlug"
        val cached = cache.get<PlaylistDto>(key)
        if (cached != null && cache.isFresh(key, ContentCache.FRESH_15MIN)) {
            return ApiResult.Success(cached)
        }
        val result = safeApiCall({ api.playlist(idOrSlug, locale()) }, json)
        if (result is ApiResult.Success) cache.put(key, result.data)
        return result
    }

    suspend fun createPlaylist(request: CreatePlaylistRequest): ApiResult<PlaylistDto> =
        safeApiCall({ api.createPlaylist(request) }, json)

    suspend fun addPlaylistItem(playlistId: String, episodeId: String) =
        safeApiCall({ api.addPlaylistItem(playlistId, PlaylistItemRequest(episodeId)) }, json)

    suspend fun removePlaylistItem(playlistId: String, episodeId: String): ApiResult<SuccessDto> =
        safeApiCall({ api.removePlaylistItem(playlistId, episodeId) }, json)

    // ---------------- Articles ----------------
    suspend fun articles(page: Int = 1, limit: Int = 20): ApiResult<Paginated<ArticleDto>> {
        val key = "articles:${locale()}:$page:$limit"
        val cached = cache.get<Paginated<ArticleDto>>(key)
        if (cached != null && cache.isFresh(key)) {
            return ApiResult.Success(cached)
        }
        val result = safeApiCall({ api.articles(locale(), page, limit) }, json)
        if (result is ApiResult.Success) cache.put(key, result.data)
        return result
    }

    suspend fun article(idOrSlug: String): ApiResult<ArticleDto> {
        val key = "article:${locale()}:$idOrSlug"
        val cached = cache.get<ArticleDto>(key)
        if (cached != null && cache.isFresh(key, ContentCache.FRESH_15MIN)) {
            return ApiResult.Success(cached)
        }
        val result = safeApiCall({ api.article(idOrSlug, locale()) }, json)
        if (result is ApiResult.Success) cache.put(key, result.data)
        return result
    }

    // ---------------- Search ----------------
    suspend fun search(
        q: String,
        page: Int = 1,
        limit: Int = 20,
        type: String? = null,
        category: String? = null,
        platform: String? = null,
        sort: String? = null,
    ): ApiResult<SearchResponseDto> =
        safeApiCall(
            { api.globalSearch(q, locale(), page, limit, type, category, platform, sort) },
            json,
        )

    suspend fun suggestions(q: String): ApiResult<SuggestionsDto> =
        safeApiCall({ api.suggestions(q, locale()) }, json)

    suspend fun recommendations(): ApiResult<RecommendationsDto> =
        safeApiCall({ api.recommendations(locale()) }, json)

    // ---------------- Banners ----------------
    suspend fun banners(position: String? = null): ApiResult<List<BannerDto>> =
        safeApiCall({ api.banners(locale(), position) }, json)

    suspend fun bannerImpression(id: String): ApiResult<SuccessDto> =
        safeApiCall({ api.bannerImpression(id) }, json)

    suspend fun bannerClick(id: String): ApiResult<SuccessDto> =
        safeApiCall({ api.bannerClick(id) }, json)

    // ---------------- Progress (server) ----------------
    suspend fun progressList(): ApiResult<List<ProgressItemDto>> =
        safeApiCall({ api.progressList(locale()) }, json)

    suspend fun progressFor(episodeId: String): ApiResult<ProgressItemDto?> =
        safeApiCall({ api.progressFor(episodeId) }, json)

    suspend fun updateProgress(episodeId: String, position: Int, duration: Int?): ApiResult<ProgressItemDto> {
        val result = safeApiCall({ api.updateProgress(episodeId, ProgressRequest(position, duration)) }, json)
        if (result is ApiResult.Failure && result.message == "network.error") {
            // Queue for sync when back online
            offlineManager.enqueue(
                method = "POST",
                endpoint = "progress/$episodeId",
                body = """{"position":$position${if (duration != null) ""","duration":$duration""" else ""}}""",
                actionType = "progress",
                contentId = episodeId,
            )
        }
        return result
    }

    suspend fun removeProgress(episodeId: String): ApiResult<SuccessDto> =
        safeApiCall({ api.removeProgress(episodeId) }, json)

    // ---------------- Views ----------------
    suspend fun trackView(contentType: String, contentId: String, durationSec: Int?, completed: Boolean) {
        val result = safeApiCall({ api.trackView(ViewTrackRequest(contentType, contentId, durationSec, completed)) }, json)
        if (result is ApiResult.Failure && result.message == "network.error") {
            offlineManager.enqueue(
                method = "POST",
                endpoint = "views/track",
                body = """{"contentType":"$contentType","contentId":"$contentId","durationSec":$durationSec,"completed":$completed}""",
                actionType = "view_track",
                contentId = contentId,
            )
        }
    }

    // ---------------- Local progress cache ----------------
    fun localProgress(): Flow<List<LocalProgressEntity>> = localProgressDao.all()

    fun localProgressFor(episodeId: String): Flow<LocalProgressEntity?> =
        localProgressDao.forEpisode(episodeId)

    suspend fun cacheProgress(episodeId: String, position: Int, duration: Int?) {
        localProgressDao.upsert(
            LocalProgressEntity(
                episodeId = episodeId,
                positionSeconds = position,
                durationSeconds = duration,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun clearLocalProgress() = localProgressDao.clear()

    suspend fun deleteLocalProgress(episodeId: String) = localProgressDao.deleteById(episodeId)

    // ---------------- Search history ----------------
    fun recentSearches(): Flow<List<SearchHistoryEntity>> = searchHistoryDao.recent()

    suspend fun addSearch(query: String) {
        searchHistoryDao.insert(SearchHistoryEntity(query = query))
    }

    suspend fun removeSearch(query: String) = searchHistoryDao.delete(query)

    suspend fun clearSearches() = searchHistoryDao.clear()
}

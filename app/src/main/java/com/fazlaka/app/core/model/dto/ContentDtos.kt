package com.fazlaka.app.core.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EpisodeTranslationDto(
    val id: String = "",
    val locale: String = "ar",
    val title: String = "",
    val description: String? = null,
    val content: String? = null,
)

@Serializable
data class SeasonTranslationDto(
    val id: String = "",
    val locale: String = "ar",
    val title: String = "",
    val description: String? = null,
)

@Serializable
data class SeasonDto(
    val id: String = "",
    val slug: String = "",
    val createdById: String? = null,
    val platform: String = "WEB",
    val coverImage: String? = null,
    val published: Boolean = true,
    val publishedAt: String? = null,
    val sortOrder: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val translations: List<SeasonTranslationDto> = emptyList(),
    val episodes: List<EpisodeDto> = emptyList(),
    val articles: List<ArticleDto> = emptyList(),
    @SerialName("_count")
    val count: SeasonCountDto? = null,
)

@Serializable
data class SeasonCountDto(
    val episodes: Int = 0,
)

@Serializable
data class EpisodeDto(
    val id: String = "",
    val slug: String = "",
    val seasonId: String? = null,
    val authorId: String? = null,
    val platform: String = "WEB",
    val coverImage: String? = null,
    val videoUrl: String? = null,
    val audioUrl: String? = null,
    val duration: Int? = null,
    val episodeNumber: Int? = null,
    val category: String? = null,
    val releaseYear: Int? = null,
    val tags: List<String> = emptyList(),
    val published: Boolean = true,
    val publishedAt: String? = null,
    val viewsCount: Int = 0,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val translations: List<EpisodeTranslationDto> = emptyList(),
    val author: AuthorDto? = null,
    val season: SeasonDto? = null,
)

@Serializable
data class PlaylistTranslationDto(
    val id: String = "",
    val locale: String = "ar",
    val title: String = "",
    val description: String? = null,
)

@Serializable
data class PlaylistItemDto(
    val id: String = "",
    val playlistId: String = "",
    val episodeId: String? = null,
    val sortOrder: Int = 0,
    val addedAt: String? = null,
    val episode: EpisodeDto? = null,
)

@Serializable
data class PlaylistDto(
    val id: String = "",
    val slug: String = "",
    val kind: String = "user",
    val platform: String = "WEB",
    val ownerId: String? = null,
    val createdByAdminId: String? = null,
    val coverImage: String? = null,
    val isPublic: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val translations: List<PlaylistTranslationDto> = emptyList(),
    val owner: AuthorDto? = null,
    val items: List<PlaylistItemDto> = emptyList(),
    @SerialName("_count")
    val count: PlaylistCountDto? = null,
)

@Serializable
data class PlaylistCountDto(
    val items: Int = 0,
)

@Serializable
data class ArticleTranslationDto(
    val id: String = "",
    val locale: String = "ar",
    val title: String = "",
    val excerpt: String? = null,
    val body: String = "",
    val seoTitle: String? = null,
    val seoDescription: String? = null,
)

@Serializable
data class ArticleDto(
    val id: String = "",
    val slug: String = "",
    val authorId: String? = null,
    val seasonId: String? = null,
    val platform: String = "WEB",
    val coverImage: String? = null,
    val category: String? = null,
    val tags: List<String> = emptyList(),
    val bodyFormat: String = "text",
    val published: Boolean = true,
    val publishedAt: String? = null,
    val viewsCount: Int = 0,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val translations: List<ArticleTranslationDto> = emptyList(),
    val author: AuthorDto? = null,
    val season: SeasonDto? = null,
)

@Serializable
data class BannerTranslationDto(
    val id: String = "",
    val locale: String = "ar",
    val title: String = "",
    val subtitle: String? = null,
)

@Serializable
data class BannerDto(
    val id: String = "",
    val imageUrl: String = "",
    val linkUrl: String? = null,
    val position: String = "hero",
    val active: Boolean = true,
    val sortOrder: Int = 0,
    val startsAt: String? = null,
    val endsAt: String? = null,
    val clicks: Int = 0,
    val impressions: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val translations: List<BannerTranslationDto> = emptyList(),
)

/** Search result row (type-specific optional fields). */
@Serializable
data class SearchResultDto(
    val type: String = "episode",
    val id: String = "",
    val slug: String = "",
    val title: String = "",
    val description: String? = null,
    val excerpt: String? = null,
    val coverImage: String? = null,
    val duration: Int? = null,
    val seasonId: String? = null,
    val seasonTitle: String? = null,
    val category: String? = null,
    val publishedAt: String? = null,
    val viewsCount: Int = 0,
)

@Serializable
data class SearchResponseDto(
    val query: String = "",
    val normalized: String? = null,
    val results: List<SearchResultDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 20,
)

@Serializable
data class SuggestionDto(
    val type: String = "episode",
    val slug: String = "",
    val title: String = "",
    val coverImage: String? = null,
)

@Serializable
data class SuggestionsDto(
    val query: String = "",
    val results: List<SuggestionDto> = emptyList(),
)

@Serializable
data class RecommendEpisodeDto(
    val id: String = "",
    val slug: String = "",
    val title: String? = null,
    val description: String? = null,
    val coverImage: String? = null,
    val duration: Int? = null,
    val seasonId: String? = null,
    val seasonTitle: String? = null,
    val publishedAt: String? = null,
)

@Serializable
data class RecommendArticleDto(
    val id: String = "",
    val slug: String = "",
    val title: String? = null,
    val excerpt: String? = null,
    val coverImage: String? = null,
    val category: String? = null,
    val publishedAt: String? = null,
)

@Serializable
data class RecommendSeasonDto(
    val id: String = "",
    val slug: String = "",
    val title: String? = null,
    val description: String? = null,
    val coverImage: String? = null,
    val publishedAt: String? = null,
)

@Serializable
data class RecommendationsDto(
    val episodes: List<RecommendEpisodeDto> = emptyList(),
    val articles: List<RecommendArticleDto> = emptyList(),
    val seasons: List<RecommendSeasonDto> = emptyList(),
)

/** Wrapper for the double-nested `data.data` from /episodes/:id/related. */
@Serializable
data class RelatedEpisodesDto(
    val data: List<EpisodeDto> = emptyList(),
)

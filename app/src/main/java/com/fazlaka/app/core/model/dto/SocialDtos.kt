package com.fazlaka.app.core.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LikeStatusDto(
    val liked: Boolean = false,
    val type: String? = null,
)

@Serializable
data class LikeUserDto(
    val id: String = "",
    val userId: String = "",
    val contentType: String = "",
    val contentId: String = "",
    val type: String = "like",
    val createdAt: String? = null,
    val user: AuthorDto? = null,
)

@Serializable
data class LikeCountDto(
    val count: Int = 0,
    val likes: List<LikeUserDto> = emptyList(),
)

@Serializable
data class LikeHistoryItemDto(
    val id: String = "",
    val contentType: String = "",
    val contentId: String = "",
    val type: String = "like",
    val title: String? = null,
    val coverImage: String? = null,
    val episode: EpisodeDto? = null,
    val likedAt: String? = null,
)

@Serializable
data class CommentCountDto(
    val replies: Int = 0,
)

@Serializable
data class CommentDto(
    val id: String = "",
    val userId: String = "",
    val contentType: String = "",
    val contentId: String = "",
    val parentId: String? = null,
    val body: String = "",
    val likesCount: Int = 0,
    val edited: Boolean = false,
    val status: String = "active",
    val platform: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val user: AuthorDto? = null,
    val likedByMe: Boolean = false,
    @SerialName("_count")
    val count: CommentCountDto? = null,
)

@Serializable
data class RatingDto(
    val id: String = "",
    val userId: String = "",
    val contentType: String = "",
    val contentId: String = "",
    val value: Int = 0,
    val comment: String? = null,
    val status: String = "approved",
    val platform: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val user: AuthorDto? = null,
)

@Serializable
data class RatingDistributionItemDto(
    val value: Int = 0,
    val count: Int = 0,
)

@Serializable
data class RatingSummaryDto(
    val average: Double? = null,
    val count: Int = 0,
    val distribution: List<RatingDistributionItemDto> = emptyList(),
)

@Serializable
data class RatingSummariesDto(
    val summaries: Map<String, RatingMiniDto> = emptyMap(),
)

@Serializable
data class RatingMiniDto(
    val average: Double? = null,
    val count: Int = 0,
)

@Serializable
data class FriendUserDto(
    val id: String = "",
    @SerialName("publicId")
    val publicId: String? = null,
    val name: String = "",
    val username: String = "",
    val avatarUrl: String? = null,
    val bio: String? = null,
    val relation: FriendRelationDto? = null,
)

@Serializable
data class FriendRelationDto(
    val status: String = "none",
    val id: String? = null,
    val incoming: Boolean = false,
)

@Serializable
data class FriendRequestDto(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val status: String = "pending",
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val sender: FriendUserDto? = null,
    val receiver: FriendUserDto? = null,
)

@Serializable
data class PublicProfileDto(
    val id: String = "",
    val publicId: String? = null,
    val name: String = "",
    val username: String = "",
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val bio: String? = null,
    val locale: String = "ar",
    val createdAt: String? = null,
    val verified: Boolean = false,
    val stats: PublicProfileStatsDto = PublicProfileStatsDto(),
)

@Serializable
data class PublicProfileStatsDto(
    val friendsCount: Int = 0,
    val ratingsCount: Int = 0,
    val articlesCount: Int = 0,
    val playlistsCount: Int = 0,
)

@Serializable
data class ReferralDto(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val avatarUrl: String? = null,
    val status: String = "active",
    val createdAt: String? = null,
    val lastActiveAt: String? = null,
)

@Serializable
data class ReferralsDto(
    val referralCode: String? = null,
    val referrals: List<ReferralDto> = emptyList(),
)

@Serializable
data class GeolocationDto(
    val id: String = "",
    val userId: String = "",
    val lat: String? = null,
    val lng: String? = null,
    val country: String? = null,
    val countryCode: String? = null,
    val region: String? = null,
    val city: String? = null,
    val platform: String? = null,
    val capturedAt: String? = null,
)

@Serializable
data class UploadResultDto(
    val url: String = "",
    val deleteUrl: String? = null,
    val width: Int? = null,
    val height: Int? = null,
)

@Serializable
data class UploadChatResultDto(
    val url: String = "",
    val deleteUrl: String? = null,
    val kind: String? = null,
    val mimeType: String? = null,
    val size: Long? = null,
    val durationSec: Int? = null,
)

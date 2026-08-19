package com.fazlaka.app.data.repository

import com.fazlaka.app.core.model.dto.CommentDto
import com.fazlaka.app.core.model.dto.CommentRequest
import com.fazlaka.app.core.model.dto.FriendRelationDto
import com.fazlaka.app.core.model.dto.FriendRequestDto
import com.fazlaka.app.core.model.dto.FriendUserDto
import com.fazlaka.app.core.model.dto.LikeHistoryItemDto
import com.fazlaka.app.core.model.dto.LikeStatusDto
import com.fazlaka.app.core.model.dto.LikeToggleRequest
import com.fazlaka.app.core.model.dto.Paginated
import com.fazlaka.app.core.model.dto.RatingDto
import com.fazlaka.app.core.model.dto.RatingRequest
import com.fazlaka.app.core.model.dto.RatingSummaryDto
import com.fazlaka.app.core.model.dto.SuccessDto
import com.fazlaka.app.core.model.dto.UpdateCommentRequest
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.core.network.ApiService
import com.fazlaka.app.core.network.OfflineManager
import com.fazlaka.app.core.network.safeApiCall
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialRepository @Inject constructor(
    private val api: ApiService,
    private val json: Json,
    private val offlineManager: OfflineManager,
) {

    // ---------------- Likes ----------------
    suspend fun toggleLike(contentType: String, contentId: String): ApiResult<LikeStatusDto> {
        val result = safeApiCall({ api.toggleLike(contentType, contentId, LikeToggleRequest()) }, json)
        if (result is ApiResult.Failure && result.message == "network.error") {
            offlineManager.enqueue(
                method = "POST",
                endpoint = "likes/$contentType/$contentId",
                actionType = "like",
                contentId = contentId,
            )
        }
        return result
    }

    suspend fun likeStatus(contentType: String, contentId: String): ApiResult<LikeStatusDto> =
        safeApiCall({ api.likeStatus(contentType, contentId) }, json)

    suspend fun likeHistory(page: Int = 1, limit: Int = 20): ApiResult<Paginated<LikeHistoryItemDto>> =
        safeApiCall({ api.likeHistory(page, limit, "ar") }, json)

    // ---------------- Comments ----------------
    suspend fun createComment(
        contentType: String,
        contentId: String,
        body: String,
        parentId: String? = null,
    ): ApiResult<CommentDto> =
        safeApiCall({ api.createComment(CommentRequest(contentType, contentId, body, parentId)) }, json)

    suspend fun comments(
        contentType: String,
        contentId: String,
        page: Int = 1,
        limit: Int = 20,
    ): ApiResult<Paginated<CommentDto>> =
        safeApiCall({ api.comments(contentType, contentId, page, limit) }, json)

    suspend fun replies(commentId: String, page: Int = 1, limit: Int = 20): ApiResult<Paginated<CommentDto>> =
        safeApiCall({ api.commentReplies(commentId, page, limit) }, json)

    suspend fun updateComment(id: String, body: String): ApiResult<CommentDto> =
        safeApiCall({ api.updateComment(id, UpdateCommentRequest(body)) }, json)

    suspend fun deleteComment(id: String): ApiResult<SuccessDto> =
        safeApiCall({ api.deleteComment(id) }, json)

    // ---------------- Ratings ----------------
    suspend fun rate(contentType: String, contentId: String, value: Int, comment: String?): ApiResult<RatingDto> =
        safeApiCall({ api.upsertRating(RatingRequest(contentType, contentId, value, comment)) }, json)

    suspend fun ratingSummary(contentType: String, contentId: String): ApiResult<RatingSummaryDto> =
        safeApiCall({ api.ratingSummary(contentType, contentId) }, json)

    suspend fun deleteRating(id: String): ApiResult<SuccessDto> =
        safeApiCall({ api.deleteRating(id) }, json)

    // ---------------- Friends ----------------
    suspend fun sendFriendRequest(userId: String): ApiResult<FriendRequestDto> =
        safeApiCall({ api.sendFriendRequest(userId) }, json)

    suspend fun acceptFriendRequest(requestId: String): ApiResult<FriendRequestDto> =
        safeApiCall({ api.acceptFriendRequest(requestId) }, json)

    suspend fun rejectFriendRequest(requestId: String): ApiResult<FriendRequestDto> =
        safeApiCall({ api.rejectFriendRequest(requestId) }, json)

    suspend fun friends(page: Int = 1, limit: Int = 20): ApiResult<Paginated<FriendUserDto>> =
        safeApiCall({ api.friends(page, limit) }, json)

    suspend fun incomingRequests(page: Int = 1, limit: Int = 20): ApiResult<Paginated<FriendRequestDto>> =
        safeApiCall({ api.incomingRequests(page, limit) }, json)

    suspend fun outgoingRequests(page: Int = 1, limit: Int = 20): ApiResult<Paginated<FriendRequestDto>> =
        safeApiCall({ api.outgoingRequests(page, limit) }, json)

    suspend fun friendSuggestions(limit: Int = 20): ApiResult<List<FriendUserDto>> =
        safeApiCall({ api.friendSuggestions(limit) }, json)

    suspend fun searchFriends(q: String): ApiResult<List<FriendUserDto>> =
        safeApiCall({ api.searchFriends(q) }, json)

    suspend fun relationship(userId: String): ApiResult<FriendRelationDto> =
        safeApiCall({ api.friendRelationship(userId) }, json)

    suspend fun removeFriend(friendId: String): ApiResult<SuccessDto> =
        safeApiCall({ api.removeFriend(friendId) }, json)

    suspend fun blockUser(userId: String): ApiResult<SuccessDto> =
        safeApiCall({ api.blockUser(userId) }, json)

    suspend fun unblockUser(userId: String): ApiResult<SuccessDto> =
        safeApiCall({ api.unblockUser(userId) }, json)
}

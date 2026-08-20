package com.fazlaka.app.core.network

import com.fazlaka.app.core.model.dto.AcceptTermsRequest
import com.fazlaka.app.core.model.dto.AppVersionData
import com.fazlaka.app.core.model.dto.AddGroupMembersRequest
import com.fazlaka.app.core.model.dto.ApiEnvelope
import com.fazlaka.app.core.model.dto.AddUserEmailRequest
import com.fazlaka.app.core.model.dto.ArticleDto
import com.fazlaka.app.core.model.dto.AuthEventDto
import com.fazlaka.app.core.model.dto.AuthResultDto
import com.fazlaka.app.core.model.dto.BannerDto
import com.fazlaka.app.core.model.dto.ChangeEmailConfirmRequest
import com.fazlaka.app.core.model.dto.ChangeEmailRequest
import com.fazlaka.app.core.model.dto.ChangePasswordRequest
import com.fazlaka.app.core.model.dto.CommentDto
import com.fazlaka.app.core.model.dto.CommentRequest
import com.fazlaka.app.core.model.dto.ConversationDetailDto
import com.fazlaka.app.core.model.dto.ConversationSummaryDto
import com.fazlaka.app.core.model.dto.CreateConversationRequest
import com.fazlaka.app.core.model.dto.CreateGroupRequest
import com.fazlaka.app.core.model.dto.CreatePlaylistRequest
import com.fazlaka.app.core.model.dto.CreateTicketRequest
import com.fazlaka.app.core.model.dto.EpisodeDto
import com.fazlaka.app.core.model.dto.ExpiresAtDto
import com.fazlaka.app.core.model.dto.FriendRelationDto
import com.fazlaka.app.core.model.dto.FriendRequestDto
import com.fazlaka.app.core.model.dto.FriendUserDto
import com.fazlaka.app.core.model.dto.GeolocationDto
import com.fazlaka.app.core.model.dto.GeolocationRequest
import com.fazlaka.app.core.model.dto.GroupMemberDto
import com.fazlaka.app.core.model.dto.LikeCountDto
import com.fazlaka.app.core.model.dto.LikeHistoryItemDto
import com.fazlaka.app.core.model.dto.LikeStatusDto
import com.fazlaka.app.core.model.dto.LikeToggleRequest
import com.fazlaka.app.core.model.dto.LinkStatusDto
import com.fazlaka.app.core.model.dto.LogoutRequest
import com.fazlaka.app.core.model.dto.MakePrimaryEmailRequest
import com.fazlaka.app.core.model.dto.MarkReadRequest
import com.fazlaka.app.core.model.dto.MarkReadResultDto
import com.fazlaka.app.core.model.dto.MessageDto
import com.fazlaka.app.core.model.dto.NotificationDto
import com.fazlaka.app.core.model.dto.OtpRequest
import com.fazlaka.app.core.model.dto.Paginated
import com.fazlaka.app.core.model.dto.PhoneChallengeDto
import com.fazlaka.app.core.model.dto.PhoneCompleteRequest
import com.fazlaka.app.core.model.dto.PlatformConfigDto
import com.fazlaka.app.core.model.dto.PlaylistDto
import com.fazlaka.app.core.model.dto.PlaylistItemRequest
import com.fazlaka.app.core.model.dto.PlaylistItemDto
import com.fazlaka.app.core.model.dto.PreferenceDto
import com.fazlaka.app.core.model.dto.ProgressItemDto
import com.fazlaka.app.core.model.dto.ProgressRequest
import com.fazlaka.app.core.model.dto.PublicProfileDto
import com.fazlaka.app.core.model.dto.PusherAuthDto
import com.fazlaka.app.core.model.dto.PusherAuthRequest
import com.fazlaka.app.core.model.dto.RatingDto
import com.fazlaka.app.core.model.dto.RatingRequest
import com.fazlaka.app.core.model.dto.RatingSummariesDto
import com.fazlaka.app.core.model.dto.RatingSummaryDto
import com.fazlaka.app.core.model.dto.RecommendationsDto
import com.fazlaka.app.core.model.dto.RegisterPhoneRequest
import com.fazlaka.app.core.model.dto.RegisterRequest
import com.fazlaka.app.core.model.dto.RelatedEpisodesDto
import com.fazlaka.app.core.model.dto.ResetPasswordRequest
import com.fazlaka.app.core.model.dto.SearchResponseDto
import com.fazlaka.app.core.model.dto.SeasonDto
import com.fazlaka.app.core.model.dto.SendMessageDto
import com.fazlaka.app.core.model.dto.SessionDto
import com.fazlaka.app.core.model.dto.SimpleMessageDto
import com.fazlaka.app.core.model.dto.SuccessDto
import com.fazlaka.app.core.model.dto.SupportMessageDto
import com.fazlaka.app.core.model.dto.SupportMessageRequest
import com.fazlaka.app.core.model.dto.SupportTicketDto
import com.fazlaka.app.core.model.dto.SuggestionsDto
import com.fazlaka.app.core.model.dto.TokenPairDto
import com.fazlaka.app.core.model.dto.TotpRequest
import com.fazlaka.app.core.model.dto.TotpSetupDto
import com.fazlaka.app.core.model.dto.TwoFactorVerifyRequest
import com.fazlaka.app.core.model.dto.UnlinkProviderRequest
import com.fazlaka.app.core.model.dto.UnreadCountDto
import com.fazlaka.app.core.model.dto.UpdateCommentRequest
import com.fazlaka.app.core.model.dto.UpdatePreferencesRequest
import com.fazlaka.app.core.model.dto.UpdateProfileRequest
import com.fazlaka.app.core.model.dto.UpdateTicketRequest
import com.fazlaka.app.core.model.dto.RemoveUserEmailRequest
import com.fazlaka.app.core.model.dto.UploadChatResultDto
import com.fazlaka.app.core.model.dto.UploadResultDto
import com.fazlaka.app.core.model.dto.UserDto
import com.fazlaka.app.core.model.dto.UserEmailDto
import com.fazlaka.app.core.model.dto.UserEmailsDto
import com.fazlaka.app.core.model.dto.VerifyUserEmailRequest
import com.fazlaka.app.core.model.dto.ViewHistoryItemDto
import com.fazlaka.app.core.model.dto.ViewTrackRequest
import kotlinx.serialization.json.JsonObject
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.HTTP
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface ApiService {

    // ---------------- Auth ----------------
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): ApiEnvelope<AuthResultDto>

    @POST("auth/login")
    suspend fun login(@Body body: com.fazlaka.app.core.model.dto.LoginRequest): ApiEnvelope<AuthResultDto>

    @POST("auth/google/native")
    suspend fun googleNativeLogin(@Body body: com.fazlaka.app.core.model.dto.GoogleNativeLoginRequest): ApiEnvelope<AuthResultDto>

    @POST("auth/login/2fa")
    suspend fun loginTwoFactor(@Body body: TwoFactorVerifyRequest): ApiEnvelope<AuthResultDto>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): ApiEnvelope<TokenPairDto>

    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutRequest): ApiEnvelope<SimpleMessageDto>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: com.fazlaka.app.core.model.dto.ForgotPasswordRequest): ApiEnvelope<SimpleMessageDto>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): ApiEnvelope<SuccessDto>

    @POST("auth/change-password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): ApiEnvelope<SuccessDto>

    @POST("auth/terms-accept")
    suspend fun acceptTerms(@Body body: AcceptTermsRequest): ApiEnvelope<AuthResultDto>

    @GET("auth/me")
    suspend fun getAuthMe(): ApiEnvelope<UserDto>

    @POST("auth/register-phone")
    suspend fun registerPhone(@Body body: RegisterPhoneRequest): ApiEnvelope<PhoneChallengeDto>

    @POST("auth/phone/login")
    suspend fun requestPhoneLogin(@Body body: RegisterPhoneRequest): ApiEnvelope<PhoneChallengeDto>

    @POST("auth/phone/complete")
    suspend fun completePhoneAuth(@Body body: PhoneCompleteRequest): ApiEnvelope<AuthResultDto>

    @POST("auth/phone/resend")
    suspend fun resendPhoneCode(@Body body: PhoneCompleteRequest): ApiEnvelope<PhoneChallengeDto>

    @GET("auth/phone/status")
    suspend fun phoneStatus(@Query("phone") phone: String): ApiEnvelope<com.fazlaka.app.core.model.dto.PhoneLinkStatusDto>

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body body: com.fazlaka.app.core.model.dto.VerifyEmailRequest): ApiEnvelope<SuccessDto>

    @POST("auth/resend-verification")
    suspend fun resendVerification(@Body body: com.fazlaka.app.core.model.dto.ResendVerificationRequest): ApiEnvelope<SimpleMessageDto>

    @GET("auth/sessions")
    suspend fun sessions(@Header("x-refresh-token") refreshToken: String?): ApiEnvelope<List<SessionDto>>

    @GET("auth/security/events")
    suspend fun securityEvents(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
    ): ApiEnvelope<Paginated<AuthEventDto>>

    @DELETE("auth/sessions")
    suspend fun revokeOtherSessions(@Body body: LogoutRequest): ApiEnvelope<SuccessDto>

    @DELETE("auth/sessions/{id}")
    suspend fun revokeSession(@Path("id") id: String): ApiEnvelope<SuccessDto>

    @GET("auth/link/status")
    suspend fun linkStatus(): ApiEnvelope<LinkStatusDto>

    @POST("auth/link/unlink")
    suspend fun unlinkProvider(@Body body: UnlinkProviderRequest): ApiEnvelope<LinkStatusDto>

    @POST("auth/link/start")
    suspend fun startOauthLinkRaw(
        @Body body: com.fazlaka.app.core.model.dto.LinkStartRequest,
    ): retrofit2.Response<ApiEnvelope<com.fazlaka.app.core.model.dto.LinkStartResultDto>>

    @POST("auth/link/otp")
    suspend fun confirmOauthLinkOtpRaw(
        @Body body: com.fazlaka.app.core.model.dto.LinkOtpRequest,
    ): retrofit2.Response<ApiEnvelope<com.fazlaka.app.core.model.dto.LinkStartResultDto>>

    @POST("phone/remove")
    suspend fun removePhone(): ApiEnvelope<SuccessDto>

    @GET("auth/2fa/totp/setup")
    suspend fun setupTotp(): ApiEnvelope<TotpSetupDto>

    @POST("auth/2fa/totp/enable")
    suspend fun enableTotp(@Body body: TotpRequest): ApiEnvelope<SuccessDto>

    @POST("auth/2fa/totp/disable")
    suspend fun disableTotp(@Body body: TotpRequest): ApiEnvelope<SuccessDto>

    @POST("auth/change-email/request")
    suspend fun requestChangeEmail(@Body body: ChangeEmailRequest): ApiEnvelope<com.fazlaka.app.core.model.dto.ExpiresAtDto>

    @POST("auth/change-email")
    suspend fun confirmChangeEmail(@Body body: ChangeEmailConfirmRequest): ApiEnvelope<SuccessDto>

    // ---------------- Secondary emails ----------------
    @GET("user-emails")
    suspend fun userEmails(): ApiEnvelope<UserEmailsDto>

    @POST("user-emails")
    suspend fun addUserEmail(@Body body: AddUserEmailRequest): ApiEnvelope<UserEmailDto>

    @POST("user-emails/verify")
    suspend fun verifyUserEmail(@Body body: VerifyUserEmailRequest): ApiEnvelope<SuccessDto>

    @POST("user-emails/primary/request")
    suspend fun requestPrimaryEmail(@Body body: AddUserEmailRequest): ApiEnvelope<ExpiresAtDto>

    @PATCH("user-emails/primary")
    suspend fun makePrimaryEmail(@Body body: MakePrimaryEmailRequest): ApiEnvelope<SuccessDto>

    @DELETE("user-emails")
    suspend fun removeUserEmail(@Body body: RemoveUserEmailRequest): ApiEnvelope<SuccessDto>

    // ---------------- Users ----------------
    @GET("users/me")
    suspend fun getMe(): ApiEnvelope<UserDto>

    @PATCH("users/me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): ApiEnvelope<UserDto>

    @POST("users/me/onboarded")
    suspend fun markOnboarded(): ApiEnvelope<SuccessDto>

    @DELETE("users/me")
    suspend fun deactivateAccount(): ApiEnvelope<SuccessDto>

    @GET("users/me/preferences")
    suspend fun getPreferences(): ApiEnvelope<PreferenceDto>

    @PATCH("users/me/preferences")
    suspend fun updatePreferences(@Body body: UpdatePreferencesRequest): ApiEnvelope<PreferenceDto>

    @POST("users/me/geolocation")
    suspend fun saveGeolocation(@Body body: GeolocationRequest): ApiEnvelope<GeolocationDto>

    @POST("users/me/avatar")
    @Multipart
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): ApiEnvelope<UserDto>

    @POST("users/me/banner")
    @Multipart
    suspend fun uploadBanner(@Part file: MultipartBody.Part): ApiEnvelope<UserDto>

    @GET("users/profile/{identifier}")
    suspend fun publicProfile(@Path("identifier") identifier: String): ApiEnvelope<PublicProfileDto>

    @GET("users/search")
    suspend fun searchUsers(
        @Query("q") q: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<Paginated<FriendUserDto>>

    @GET("users/me/referrals")
    suspend fun myReferrals(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<com.fazlaka.app.core.model.dto.ReferralsDto>

    // ---------------- Episodes ----------------
    @GET("episodes")
    suspend fun episodes(
        @Query("locale") locale: String = "ar",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("seasonId") seasonId: String? = null,
        @Query("search") search: String? = null,
        @Query("platform") platform: String? = null,
    ): ApiEnvelope<Paginated<EpisodeDto>>

    @GET("episodes/{idOrSlug}")
    suspend fun episode(
        @Path("idOrSlug") idOrSlug: String,
        @Query("locale") locale: String = "ar",
    ): ApiEnvelope<EpisodeDto>

    @GET("episodes/{idOrSlug}/related")
    suspend fun relatedEpisodes(
        @Path("idOrSlug") idOrSlug: String,
        @Query("locale") locale: String = "ar",
        @Query("limit") limit: Int = 10,
    ): ApiEnvelope<RelatedEpisodesDto>

    // ---------------- Seasons ----------------
    @GET("seasons")
    suspend fun seasons(
        @Query("locale") locale: String = "ar",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("platform") platform: String? = null,
    ): ApiEnvelope<Paginated<SeasonDto>>

    @GET("seasons/{idOrSlug}")
    suspend fun season(
        @Path("idOrSlug") idOrSlug: String,
        @Query("locale") locale: String = "ar",
    ): ApiEnvelope<SeasonDto>

    // ---------------- Playlists ----------------
    @GET("playlists")
    suspend fun playlists(
        @Query("locale") locale: String = "ar",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("platform") platform: String? = null,
    ): ApiEnvelope<Paginated<PlaylistDto>>

    @GET("playlists/{idOrSlug}")
    suspend fun playlist(
        @Path("idOrSlug") idOrSlug: String,
        @Query("locale") locale: String = "ar",
    ): ApiEnvelope<PlaylistDto>

    @POST("playlists")
    suspend fun createPlaylist(@Body body: CreatePlaylistRequest): ApiEnvelope<PlaylistDto>

    @POST("playlists/{id}/items")
    suspend fun addPlaylistItem(@Path("id") id: String, @Body body: PlaylistItemRequest): ApiEnvelope<PlaylistItemDto>

    @DELETE("playlists/{id}/items/{episodeId}")
    suspend fun removePlaylistItem(@Path("id") id: String, @Path("episodeId") episodeId: String): ApiEnvelope<SuccessDto>

    // ---------------- Articles ----------------
    @GET("articles")
    suspend fun articles(
        @Query("locale") locale: String = "ar",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("category") category: String? = null,
        @Query("platform") platform: String? = null,
    ): ApiEnvelope<Paginated<ArticleDto>>

    @GET("articles/{idOrSlug}")
    suspend fun article(
        @Path("idOrSlug") idOrSlug: String,
        @Query("locale") locale: String = "ar",
    ): ApiEnvelope<ArticleDto>

    // ---------------- Search ----------------
    @GET("search")
    suspend fun globalSearch(
        @Query("q") q: String,
        @Query("locale") locale: String = "ar",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("type") type: String? = null,
        @Query("category") category: String? = null,
        @Query("platform") platform: String? = null,
        @Query("sort") sort: String? = null,
    ): ApiEnvelope<SearchResponseDto>

    @GET("search/suggestions")
    suspend fun suggestions(
        @Query("q") q: String,
        @Query("locale") locale: String = "ar",
        @Query("limit") limit: Int = 6,
    ): ApiEnvelope<SuggestionsDto>

    @GET("search/recommendations")
    suspend fun recommendations(
        @Query("locale") locale: String = "ar",
        @Query("limit") limit: Int = 10,
    ): ApiEnvelope<RecommendationsDto>

    // ---------------- Banners ----------------
    @GET("banners")
    suspend fun banners(
        @Query("locale") locale: String = "ar",
        @Query("position") position: String? = null,
    ): ApiEnvelope<List<BannerDto>>

    @POST("banners/{id}/impression")
    suspend fun bannerImpression(@Path("id") id: String): ApiEnvelope<SuccessDto>

    @POST("banners/{id}/click")
    suspend fun bannerClick(@Path("id") id: String): ApiEnvelope<SuccessDto>

    // ---------------- Likes ----------------
    @POST("likes/{contentType}/{contentId}")
    suspend fun toggleLike(
        @Path("contentType") contentType: String,
        @Path("contentId") contentId: String,
        @Body body: LikeToggleRequest,
    ): ApiEnvelope<LikeStatusDto>

    @GET("likes/{contentType}/{contentId}/status")
    suspend fun likeStatus(
        @Path("contentType") contentType: String,
        @Path("contentId") contentId: String,
    ): ApiEnvelope<LikeStatusDto>

    @GET("likes/{contentType}/{contentId}")
    suspend fun likeCount(
        @Path("contentType") contentType: String,
        @Path("contentId") contentId: String,
    ): ApiEnvelope<LikeCountDto>

    @GET("likes/history")
    suspend fun likeHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("locale") locale: String = "ar",
    ): ApiEnvelope<Paginated<LikeHistoryItemDto>>

    // ---------------- Comments ----------------
    @POST("comments")
    suspend fun createComment(@Body body: CommentRequest): ApiEnvelope<CommentDto>

    @GET("comments/{contentType}/{contentId}")
    suspend fun comments(
        @Path("contentType") contentType: String,
        @Path("contentId") contentId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<Paginated<CommentDto>>

    @GET("comments/replies/{commentId}")
    suspend fun commentReplies(
        @Path("commentId") commentId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<Paginated<CommentDto>>

    @PATCH("comments/{id}")
    suspend fun updateComment(@Path("id") id: String, @Body body: UpdateCommentRequest): ApiEnvelope<CommentDto>

    @DELETE("comments/{id}")
    suspend fun deleteComment(@Path("id") id: String): ApiEnvelope<SuccessDto>

    @POST("comments/{id}/hide")
    suspend fun hideComment(@Path("id") id: String): ApiEnvelope<SuccessDto>

    // ---------------- Ratings ----------------
    @POST("ratings")
    suspend fun upsertRating(@Body body: RatingRequest): ApiEnvelope<RatingDto>

    @GET("ratings/mine")
    suspend fun myRatings(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<Paginated<RatingDto>>

    @GET("ratings/summaries")
    suspend fun ratingSummaries(
        @Query("contentType") contentType: String,
        @Query("ids") ids: String,
    ): ApiEnvelope<RatingSummariesDto>

    @GET("ratings/content/{contentType}/{contentId}/summary")
    suspend fun ratingSummary(
        @Path("contentType") contentType: String,
        @Path("contentId") contentId: String,
    ): ApiEnvelope<RatingSummaryDto>

    @GET("ratings/content/{contentType}/{contentId}")
    suspend fun ratingsForContent(
        @Path("contentType") contentType: String,
        @Path("contentId") contentId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<Paginated<RatingDto>>

    @PATCH("ratings/{id}")
    suspend fun updateRating(@Path("id") id: String, @Body body: RatingRequest): ApiEnvelope<RatingDto>

    @DELETE("ratings/{id}")
    suspend fun deleteRating(@Path("id") id: String): ApiEnvelope<SuccessDto>

    // ---------------- Friends ----------------
    @POST("friends/request/{userId}")
    suspend fun sendFriendRequest(@Path("userId") userId: String): ApiEnvelope<FriendRequestDto>

    @POST("friends/requests/{requestId}/accept")
    suspend fun acceptFriendRequest(@Path("requestId") requestId: String): ApiEnvelope<FriendRequestDto>

    @POST("friends/requests/{requestId}/reject")
    suspend fun rejectFriendRequest(@Path("requestId") requestId: String): ApiEnvelope<FriendRequestDto>

    @GET("friends")
    suspend fun friends(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<Paginated<FriendUserDto>>

    @GET("friends/requests/incoming")
    suspend fun incomingRequests(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<Paginated<FriendRequestDto>>

    @GET("friends/requests/outgoing")
    suspend fun outgoingRequests(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<Paginated<FriendRequestDto>>

    @GET("friends/suggestions")
    suspend fun friendSuggestions(@Query("limit") limit: Int = 20): ApiEnvelope<List<FriendUserDto>>

    @GET("friends/search")
    suspend fun searchFriends(@Query("q") q: String): ApiEnvelope<List<FriendUserDto>>

    @GET("friends/relationship/{userId}")
    suspend fun friendRelationship(@Path("userId") userId: String): ApiEnvelope<FriendRelationDto>

    @DELETE("friends/{friendId}")
    suspend fun removeFriend(@Path("friendId") friendId: String): ApiEnvelope<SuccessDto>

    @POST("friends/block/{userId}")
    suspend fun blockUser(@Path("userId") userId: String): ApiEnvelope<SuccessDto>

    @POST("friends/unblock/{userId}")
    suspend fun unblockUser(@Path("userId") userId: String): ApiEnvelope<SuccessDto>

    // ---------------- Messages ----------------
    @POST("messages/conversations")
    suspend fun getOrCreateConversation(@Body body: CreateConversationRequest): ApiEnvelope<ConversationDetailDto>

    @GET("messages/conversations")
    suspend fun conversations(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<Paginated<ConversationSummaryDto>>

    @GET("messages/conversations/{id}")
    suspend fun conversationDetail(
        @Path("id") id: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
    ): ApiEnvelope<ConversationDetailDto>

    @POST("messages/conversations/{id}/messages")
    suspend fun sendMessage(
        @Path("id") id: String,
        @Body body: SendMessageDto,
    ): ApiEnvelope<MessageDto>

    @PATCH("messages/conversations/{id}/read")
    suspend fun markConversationRead(@Path("id") id: String): ApiEnvelope<MarkReadResultDto>

    @POST("messages/groups")
    suspend fun createGroup(@Body body: CreateGroupRequest): ApiEnvelope<ConversationDetailDto>

    @GET("messages/groups/{id}/members")
    suspend fun groupMembers(@Path("id") id: String): ApiEnvelope<List<GroupMemberDto>>

    @POST("messages/groups/{id}/members")
    suspend fun addGroupMembers(
        @Path("id") id: String,
        @Body body: AddGroupMembersRequest,
    ): ApiEnvelope<com.fazlaka.app.core.model.dto.AddMembersResultDto>

    @DELETE("messages/groups/{id}/members/{userId}")
    suspend fun removeGroupMember(
        @Path("id") id: String,
        @Path("userId") userId: String,
    ): ApiEnvelope<SuccessDto>

    // ---------------- Progress ----------------
    @GET("progress")
    suspend fun progressList(@Query("locale") locale: String = "ar"): ApiEnvelope<List<ProgressItemDto>>

    @GET("progress/{episodeId}")
    suspend fun progressFor(@Path("episodeId") episodeId: String): ApiEnvelope<ProgressItemDto?>

    @PATCH("progress/{episodeId}")
    suspend fun updateProgress(
        @Path("episodeId") episodeId: String,
        @Body body: ProgressRequest,
    ): ApiEnvelope<ProgressItemDto>

    @DELETE("progress/{episodeId}")
    suspend fun removeProgress(@Path("episodeId") episodeId: String): ApiEnvelope<SuccessDto>

    // ---------------- Notifications ----------------
    @GET("notifications")
    suspend fun notifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<Paginated<NotificationDto>>

    @GET("notifications/unread-count")
    suspend fun unreadCount(): ApiEnvelope<UnreadCountDto>

    @PATCH("notifications/read")
    suspend fun markNotificationsRead(@Body body: MarkReadRequest): ApiEnvelope<SuccessDto>

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: String): ApiEnvelope<SuccessDto>

    // ---------------- Views ----------------
    @POST("views/track")
    suspend fun trackView(@Body body: ViewTrackRequest): ApiEnvelope<SuccessDto>

    @GET("views/history")
    suspend fun viewHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("locale") locale: String = "ar",
    ): ApiEnvelope<Paginated<ViewHistoryItemDto>>

    @DELETE("views/history")
    suspend fun clearViewHistory(): ApiEnvelope<SuccessDto>

    // ---------------- Support ----------------
    @POST("support/tickets")
    suspend fun createTicket(@Body body: CreateTicketRequest): ApiEnvelope<SupportTicketDto>

    @GET("support/tickets")
    suspend fun myTickets(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<Paginated<SupportTicketDto>>

    @GET("support/tickets/{id}")
    suspend fun ticketDetail(@Path("id") id: String): ApiEnvelope<SupportTicketDto>

    @POST("support/tickets/{id}/messages")
    suspend fun addTicketMessage(
        @Path("id") id: String,
        @Body body: SupportMessageRequest,
    ): ApiEnvelope<SupportMessageDto>

    @PATCH("support/tickets/{id}/status")
    suspend fun updateTicket(
        @Path("id") id: String,
        @Body body: UpdateTicketRequest,
    ): ApiEnvelope<SupportTicketDto>

    // ---------------- Upload ----------------
    @POST("upload/image")
    @Multipart
    suspend fun uploadImage(@Part file: MultipartBody.Part): ApiEnvelope<UploadResultDto>

    @POST("upload/chat")
    @Multipart
    suspend fun uploadChatMedia(
        @Part file: MultipartBody.Part,
        @Query("kind") kind: String,
        @Query("durationSec") durationSec: Int? = null,
    ): ApiEnvelope<UploadChatResultDto>

    @POST("upload/public")
    @Multipart
    suspend fun uploadPublic(@Part file: MultipartBody.Part): ApiEnvelope<UploadResultDto>

    // ---------------- Platforms / Settings ----------------
    @GET("platforms")
    suspend fun platforms(): ApiEnvelope<List<PlatformConfigDto>>

    @GET("settings/public")
    suspend fun publicSettings(@QueryMap query: Map<String, String> = emptyMap()): ApiEnvelope<JsonObject>

    // ---------------- Realtime auth ----------------
    @POST("realtime/pusher/auth")
    suspend fun pusherAuth(@Body body: PusherAuthRequest): ApiEnvelope<PusherAuthDto>

    // ---------------- Push Devices ----------------
    @POST("push/devices")
    suspend fun registerDevice(@Body body: Map<String, String>): ApiEnvelope<Any>

    @HTTP(method = "DELETE", path = "push/devices", hasBody = true)
    suspend fun unregisterDevice(@Body body: Map<String, String>): ApiEnvelope<Any>

    // ---------------- App Update ----------------
    @GET("app-version/latest")
    suspend fun getLatestAppVersion(): AppVersionData
}

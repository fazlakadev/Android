package com.fazlaka.app.data.repository

import com.fazlaka.app.core.datastore.SessionManager
import com.fazlaka.app.core.model.dto.CreateTicketRequest
import com.fazlaka.app.core.model.dto.GeolocationRequest
import com.fazlaka.app.core.model.dto.NotificationDto
import com.fazlaka.app.core.model.dto.Paginated
import com.fazlaka.app.core.model.dto.PlatformConfigDto
import com.fazlaka.app.core.model.dto.PreferenceDto
import com.fazlaka.app.core.model.dto.PublicProfileDto
import com.fazlaka.app.core.model.dto.ReferralsDto
import com.fazlaka.app.core.model.dto.SuccessDto
import com.fazlaka.app.core.model.dto.SupportMessageDto
import com.fazlaka.app.core.model.dto.SupportMessageRequest
import com.fazlaka.app.core.model.dto.SupportTicketDto
import com.fazlaka.app.core.model.dto.UnreadCountDto
import com.fazlaka.app.core.model.dto.UpdatePreferencesRequest
import com.fazlaka.app.core.model.dto.UpdateProfileRequest
import com.fazlaka.app.core.model.dto.UpdateTicketRequest
import com.fazlaka.app.core.model.dto.UserDto
import com.fazlaka.app.core.model.dto.ViewHistoryItemDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.core.network.ApiService
import com.fazlaka.app.core.network.safeApiCall
import kotlinx.serialization.json.Json
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val api: ApiService,
    private val session: SessionManager,
    private val json: Json,
) {

    private suspend fun locale(): String = session.localeValue()

    suspend fun me(): ApiResult<UserDto> = safeApiCall({ api.getMe() }, json)

    suspend fun updateProfile(request: UpdateProfileRequest): ApiResult<UserDto> =
        safeApiCall({ api.updateProfile(request) }, json)

    suspend fun markOnboarded(): ApiResult<SuccessDto> =
        safeApiCall({ api.markOnboarded() }, json)

    suspend fun deactivateAccount(): ApiResult<SuccessDto> =
        safeApiCall({ api.deactivateAccount() }, json)

    suspend fun preferences(): ApiResult<PreferenceDto> =
        safeApiCall({ api.getPreferences() }, json)

    suspend fun updatePreferences(request: UpdatePreferencesRequest): ApiResult<PreferenceDto> =
        safeApiCall({ api.updatePreferences(request) }, json)

    suspend fun saveGeolocation(request: GeolocationRequest) =
        safeApiCall({ api.saveGeolocation(request) }, json)

    suspend fun uploadAvatar(file: MultipartBody.Part): ApiResult<UserDto> =
        safeApiCall({ api.uploadAvatar(file) }, json)

    suspend fun uploadBanner(file: MultipartBody.Part): ApiResult<UserDto> =
        safeApiCall({ api.uploadBanner(file) }, json)

    suspend fun publicProfile(identifier: String): ApiResult<PublicProfileDto> =
        safeApiCall({ api.publicProfile(identifier) }, json)

    suspend fun referrals(page: Int = 1, limit: Int = 20): ApiResult<ReferralsDto> =
        safeApiCall({ api.myReferrals(page, limit) }, json)

    // ---------------- Notifications ----------------
    suspend fun notifications(page: Int = 1, limit: Int = 20): ApiResult<Paginated<NotificationDto>> =
        safeApiCall({ api.notifications(page, limit) }, json)

    suspend fun unreadCount(): ApiResult<UnreadCountDto> =
        safeApiCall({ api.unreadCount() }, json)

    suspend fun markNotificationsRead(id: String?): ApiResult<SuccessDto> =
        safeApiCall({ api.markNotificationsRead(com.fazlaka.app.core.model.dto.MarkReadRequest(id)) }, json)

    suspend fun deleteNotification(id: String): ApiResult<SuccessDto> =
        safeApiCall({ api.deleteNotification(id) }, json)

    // ---------------- Views history ----------------
    suspend fun viewHistory(page: Int = 1, limit: Int = 20): ApiResult<Paginated<ViewHistoryItemDto>> =
        safeApiCall({ api.viewHistory(page, limit, locale()) }, json)

    suspend fun clearViewHistory(): ApiResult<SuccessDto> =
        safeApiCall({ api.clearViewHistory() }, json)

    // ---------------- Support ----------------
    suspend fun createTicket(request: CreateTicketRequest): ApiResult<SupportTicketDto> =
        safeApiCall({ api.createTicket(request) }, json)

    suspend fun myTickets(page: Int = 1, limit: Int = 20): ApiResult<Paginated<SupportTicketDto>> =
        safeApiCall({ api.myTickets(page, limit) }, json)

    suspend fun ticketDetail(id: String): ApiResult<SupportTicketDto> =
        safeApiCall({ api.ticketDetail(id) }, json)

    suspend fun addTicketMessage(id: String, message: String): ApiResult<SupportMessageDto> =
        safeApiCall({ api.addTicketMessage(id, SupportMessageRequest(message)) }, json)

    suspend fun updateTicket(id: String, status: String?, priority: String?): ApiResult<SupportTicketDto> =
        safeApiCall({ api.updateTicket(id, UpdateTicketRequest(status, priority)) }, json)

    // ---------------- Platforms / Settings ----------------
    suspend fun platforms(): ApiResult<List<PlatformConfigDto>> =
        safeApiCall({ api.platforms() }, json)

    suspend fun updateLocale(locale: String) = session.setLocale(locale)
}

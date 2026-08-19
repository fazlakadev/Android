package com.fazlaka.app.data.repository

import android.content.Context
import android.net.Uri
import com.fazlaka.app.core.database.MessageDraftDao
import com.fazlaka.app.core.database.MessageDraftEntity
import com.fazlaka.app.core.model.dto.ConversationDetailDto
import com.fazlaka.app.core.model.dto.ConversationSummaryDto
import com.fazlaka.app.core.model.dto.CreateConversationRequest
import com.fazlaka.app.core.model.dto.CreateGroupRequest
import com.fazlaka.app.core.model.dto.GroupMemberDto
import com.fazlaka.app.core.model.dto.MarkReadResultDto
import com.fazlaka.app.core.model.dto.MessageDto
import com.fazlaka.app.core.model.dto.Paginated
import com.fazlaka.app.core.model.dto.SendMessageDto
import com.fazlaka.app.core.model.dto.SuccessDto
import com.fazlaka.app.core.model.dto.UploadChatResultDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.core.network.ApiService
import com.fazlaka.app.core.network.OfflineManager
import com.fazlaka.app.core.network.safeApiCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessagingRepository @Inject constructor(
    private val api: ApiService,
    private val json: Json,
    private val draftDao: MessageDraftDao,
    private val offlineManager: OfflineManager,
    @ApplicationContext private val context: Context,
) {

    suspend fun getOrCreateConversation(userId: String): ApiResult<ConversationDetailDto> =
        safeApiCall({ api.getOrCreateConversation(CreateConversationRequest(userId)) }, json)

    suspend fun conversations(page: Int = 1, limit: Int = 20): ApiResult<Paginated<ConversationSummaryDto>> =
        safeApiCall({ api.conversations(page, limit) }, json)

    suspend fun conversationDetail(id: String, page: Int = 1, limit: Int = 50): ApiResult<ConversationDetailDto> =
        safeApiCall({ api.conversationDetail(id, page, limit) }, json)

    suspend fun sendMessage(id: String, body: String): ApiResult<MessageDto> {
        val result = safeApiCall({ api.sendMessage(id, SendMessageDto(body = body)) }, json)
        if (result is ApiResult.Failure && result.message == "network.error") {
            offlineManager.enqueue(
                method = "POST",
                endpoint = "messages/$id",
                body = """{"body":"$body"}""",
                actionType = "message",
                contentId = id,
            )
        }
        return result
    }

    suspend fun sendMedia(
        id: String,
        type: String,
        attachmentUrl: String,
        attachmentMime: String?,
        attachmentName: String?,
        attachmentSize: Long?,
        durationSec: Int?,
    ): ApiResult<MessageDto> =
        safeApiCall(
            {
                api.sendMessage(
                    id,
                    SendMessageDto(
                        type = type,
                        attachmentUrl = attachmentUrl,
                        attachmentMime = attachmentMime,
                        attachmentName = attachmentName,
                        attachmentSize = attachmentSize,
                        durationSec = durationSec,
                    ),
                )
            },
            json,
        )

    suspend fun uploadChatMedia(
        uri: Uri,
        kind: String,
        durationSec: Int?,
    ): ApiResult<UploadChatResultDto> {
        val mime = context.contentResolver.getType(uri)
            ?: (if (kind == "image") "image/jpeg" else if (kind == "video") "video/mp4" else "audio/mpeg")
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return ApiResult.Failure(-2, "cannot.read.file")
        val size = bytes.size.toLong()
        val displayName = runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
                null, null, null,
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow(android.provider.OpenableColumns.DISPLAY_NAME))
                } else null
            }
        }.getOrNull()

        return safeApiCall(
            {
                val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData(
                    "file",
                    displayName ?: "upload.${mime.substringAfter('/')}",
                    body,
                )
                api.uploadChatMedia(part, kind, durationSec)
            },
            json,
        )
    }

    suspend fun markRead(id: String): ApiResult<MarkReadResultDto> =
        safeApiCall({ api.markConversationRead(id) }, json)

    suspend fun createGroup(request: CreateGroupRequest): ApiResult<ConversationDetailDto> =
        safeApiCall({ api.createGroup(request) }, json)

    suspend fun groupMembers(id: String): ApiResult<List<GroupMemberDto>> =
        safeApiCall({ api.groupMembers(id) }, json)

    suspend fun addGroupMembers(id: String, userIds: List<String>) =
        safeApiCall(
            { api.addGroupMembers(id, com.fazlaka.app.core.model.dto.AddGroupMembersRequest(userIds)) },
            json,
        )

    suspend fun removeGroupMember(id: String, userId: String): ApiResult<SuccessDto> =
        safeApiCall({ api.removeGroupMember(id, userId) }, json)

    // ---------------- Drafts ----------------
    suspend fun saveDraft(conversationId: String, draft: String) {
        draftDao.save(MessageDraftEntity(conversationId = conversationId, draft = draft))
    }

    suspend fun draft(conversationId: String): String? =
        draftDao.get(conversationId)?.draft?.takeIf { it.isNotBlank() }

    suspend fun clearDraft(conversationId: String) = draftDao.delete(conversationId)
}

package com.fazlaka.app.core.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class MessageDto(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val type: String = "text",
    val body: String = "",
    val attachmentUrl: String? = null,
    val attachmentMime: String? = null,
    val attachmentName: String? = null,
    val attachmentSize: Long? = null,
    val durationSec: Int? = null,
    val readAt: String? = null,
    val createdAt: String? = null,
    val sender: FriendUserDto? = null,
)

@Serializable
data class GroupMemberDto(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val avatarUrl: String? = null,
    val role: String = "member",
    val joinedAt: String? = null,
)

@Serializable
data class GroupInfoDto(
    val id: String = "",
    val name: String? = null,
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val createdById: String? = null,
    val memberCount: Int = 0,
    val members: List<GroupMemberDto> = emptyList(),
)

@Serializable
data class ConversationSummaryDto(
    val id: String = "",
    val kind: String = "direct",
    val other: FriendUserDto? = null,
    val group: GroupInfoDto? = null,
    val lastMessage: MessageDto? = null,
    val unreadCount: Int = 0,
    val updatedAt: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class ConversationDetailDto(
    val conversation: ConversationHeaderDto = ConversationHeaderDto(),
    val messages: List<MessageDto> = emptyList(),
)

@Serializable
data class ConversationHeaderDto(
    val id: String = "",
    val kind: String = "direct",
    val other: FriendUserDto? = null,
    val group: GroupInfoDto? = null,
)

@Serializable
data class SendMessageDto(
    val type: String = "text",
    val body: String? = null,
    val attachmentUrl: String? = null,
    val attachmentMime: String? = null,
    val attachmentName: String? = null,
    val attachmentSize: Long? = null,
    val durationSec: Int? = null,
)

@Serializable
data class MarkReadResultDto(
    val success: Boolean = false,
    val marked: Int = 0,
)

@Serializable
data class AddMembersResultDto(
    val added: Int = 0,
    val members: List<GroupMemberDto> = emptyList(),
)

@Serializable
data class NotificationDto(
    val id: String = "",
    val userId: String = "",
    val type: String = "system",
    val title: String = "",
    val body: String = "",
    val data: JsonObject? = null,
    val readAt: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class UnreadCountDto(
    val count: Int = 0,
)

@Serializable
data class ProgressItemDto(
    val id: String = "",
    val userId: String = "",
    val episodeId: String = "",
    val positionSeconds: Int = 0,
    val durationSeconds: Int? = null,
    val percent: Int = 0,
    val updatedAt: String? = null,
    val title: String? = null,
    val coverImage: String? = null,
    val seasonId: String? = null,
    val seasonTitle: String? = null,
    val episode: EpisodeDto? = null,
)

@Serializable
data class SupportTicketDto(
    val id: String = "",
    val userId: String = "",
    val subject: String = "",
    val status: String = "open",
    val priority: String = "medium",
    val platform: String? = null,
    val deviceInfo: String? = null,
    val resolvedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val messages: List<SupportMessageDto> = emptyList(),
    val user: AuthorDto? = null,
    @SerialName("_count")
    val count: SupportCountDto? = null,
    val activeCall: Boolean = false,
)

@Serializable
data class SupportCountDto(
    val messages: Int = 0,
)

@Serializable
data class SupportMessageDto(
    val id: String = "",
    val ticketId: String = "",
    val senderId: String? = null,
    val senderAdminId: String? = null,
    val isAdminReply: Boolean = false,
    val body: String = "",
    val isSystem: Boolean = false,
    val attachments: List<String> = emptyList(),
    val createdAt: String? = null,
)

@Serializable
data class ViewHistoryItemDto(
    val id: String = "",
    val contentType: String = "",
    val contentId: String = "",
    val title: String? = null,
    val coverImage: String? = null,
    val slug: String? = null,
    val episode: EpisodeDto? = null,
    val platform: String? = null,
    val durationSec: Int = 0,
    val completed: Boolean = false,
    val watchedAt: String? = null,
)

@Serializable
data class PlatformConfigDto(
    val platform: String = "WEB",
    val displayName: String? = null,
    val enabled: Boolean = true,
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String? = null,
    val minVersion: String? = null,
    val latestVersion: String? = null,
    val downloadUrl: String? = null,
)

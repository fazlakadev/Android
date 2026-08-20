package com.fazlaka.app.core.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class GoogleNativeLoginRequest(
    val idToken: String,
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val username: String,
    val locale: String = "ar",
    val referralCode: String? = null,
    val termsAccepted: Boolean = true,
)

@Serializable
data class RegisterPhoneRequest(
    val phone: String,
    val username: String,
    val name: String? = null,
    val locale: String = "ar",
    val termsAccepted: Boolean = true,
)

@Serializable
data class PhoneCompleteRequest(
    val phone: String,
    val verificationId: String,
    val code: String? = null,
)

@Serializable
data class TwoFactorVerifyRequest(
    val email: String,
    val otp: String,
)

@Serializable
data class OtpRequest(
    val otp: String,
)

@Serializable
data class TotpRequest(
    val code: String,
)

@Serializable
data class VerifyEmailRequest(
    val token: String? = null,
    val email: String? = null,
    val otp: String? = null,
)

@Serializable
data class ForgotPasswordRequest(
    val email: String,
)

@Serializable
data class ResetPasswordRequest(
    val password: String,
    val token: String? = null,
    val email: String? = null,
    val otp: String? = null,
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
)

@Serializable
data class ChangeEmailRequest(
    val newEmail: String,
)

@Serializable
data class ChangeEmailConfirmRequest(
    val newEmail: String,
    val otp: String,
)

@Serializable
data class AcceptTermsRequest(
    val termsAccepted: Boolean = true,
    val username: String? = null,
)

@Serializable
data class ResendVerificationRequest(
    val email: String,
)

@Serializable
data class LogoutRequest(
    val refreshToken: String,
)

@Serializable
data class MarkReadRequest(
    val id: String? = null,
)

@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val bio: String? = null,
    val username: String? = null,
    val locale: String? = null,
)

@Serializable
data class UpdatePreferencesRequest(
    val locale: String? = null,
    val notificationsEnabled: Boolean? = null,
    val emailNotifications: Boolean? = null,
    val loginAlerts: Boolean? = null,
    val pushNotifications: Boolean? = null,
    val newsletter: Boolean? = null,
    val darkMode: Boolean? = null,
    val showOnline: Boolean? = null,
    val timezone: String? = null,
)

@Serializable
data class GeolocationRequest(
    val lat: Double,
    val lng: Double,
    val country: String? = null,
    val countryCode: String? = null,
    val region: String? = null,
    val city: String? = null,
    val zip: String? = null,
)

@Serializable
data class LikeToggleRequest(
    val type: String = "like",
)

@Serializable
data class CommentRequest(
    val contentType: String,
    val contentId: String,
    val body: String,
    val parentId: String? = null,
)

@Serializable
data class UpdateCommentRequest(
    val body: String,
)

@Serializable
data class RatingRequest(
    val contentType: String,
    val contentId: String,
    val value: Int,
    val comment: String? = null,
)

@Serializable
data class CreateConversationRequest(
    val userId: String,
)

@Serializable
data class CreateGroupRequest(
    val name: String,
    val avatarUrl: String? = null,
    val memberIds: List<String> = emptyList(),
)

@Serializable
data class AddGroupMembersRequest(
    val userIds: List<String> = emptyList(),
)

@Serializable
data class CreatePlaylistRequest(
    val title: String,
    val description: String? = null,
    val locale: String = "ar",
    val isPublic: Boolean = true,
    val coverImage: String? = null,
)

@Serializable
data class PlaylistItemRequest(
    val episodeId: String,
)

@Serializable
data class ProgressRequest(
    val positionSeconds: Int,
    val durationSeconds: Int? = null,
)

@Serializable
data class ViewTrackRequest(
    val contentType: String,
    val contentId: String,
    val durationSec: Int? = null,
    val completed: Boolean = false,
)

@Serializable
data class CreateTicketRequest(
    val subject: String,
    val priority: String = "medium",
    val deviceInfo: String? = null,
    val message: String,
)

@Serializable
data class SupportMessageRequest(
    val message: String,
    val attachments: List<String> = emptyList(),
)

@Serializable
data class UpdateTicketRequest(
    val status: String? = null,
    val priority: String? = null,
)

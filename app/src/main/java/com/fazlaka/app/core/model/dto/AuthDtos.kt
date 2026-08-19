package com.fazlaka.app.core.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String = "",
    val email: String? = null,
    val emailVerified: String? = null,
    val name: String = "",
    val username: String = "",
    val publicId: String? = null,
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val bio: String? = null,
    val status: String = "active",
    val locale: String = "ar",
    val referralCode: String? = null,
    val onboardedAt: String? = null,
    val phone: String? = null,
    val phoneVerifiedAt: String? = null,
    val telegramUsername: String? = null,
    val twoFactorEnabled: Boolean = false,
    val twoFactorMethod: String? = null,
    val termsAcceptedAt: String? = null,
    val termsVersion: String? = null,
    val lastActiveAt: String? = null,
    val lastPlatform: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val preference: PreferenceDto? = null,
    val hasPassword: Boolean = false,
    val phoneLinked: Boolean = false,
    val googleLinked: Boolean = false,
    val githubLinked: Boolean = false,
    val facebookLinked: Boolean = false,
)

@Serializable
data class PreferenceDto(
    val id: String = "",
    val userId: String = "",
    val locale: String = "ar",
    val primaryLocale: String = "ar",
    val secondaryLocale: String? = null,
    val notificationsEnabled: Boolean = true,
    val emailNotifications: Boolean = true,
    val loginAlerts: Boolean = true,
    val pushNotifications: Boolean = true,
    val newsletter: Boolean = false,
    val darkMode: Boolean = true,
    val showOnline: Boolean = true,
    val timezone: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

/**
 * Union response for login/register/2FA/phone-auth/terms-accept.
 * Either token pair + user, or { requiresTwoFactor, email, method },
 * or { pending: true }.
 */
@Serializable
data class AuthResultDto(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val user: UserDto? = null,
    val requiresTwoFactor: Boolean = false,
    val email: String? = null,
    val method: String? = null,
    val pending: Boolean = false,
)

@Serializable
data class TokenPairDto(
    val accessToken: String? = null,
    val refreshToken: String? = null,
)

@Serializable
data class PhoneChallengeDto(
    val verificationId: String? = null,
    val phone: String? = null,
    val status: String? = null,
    val botUsername: String? = null,
    val botUrl: String? = null,
    val expiresIn: Long? = null,
    val resendAt: Long? = null,
)

@Serializable
data class PhoneLinkStatusDto(
    val phone: String? = null,
    val linked: Boolean = false,
    val botUsername: String? = null,
    val botUrl: String? = null,
)

@Serializable
data class SessionDto(
    val id: String = "",
    val platform: String? = null,
    val deviceType: String? = null,
    val deviceName: String? = null,
    val os: String? = null,
    val browser: String? = null,
    val ip: String? = null,
    val country: String? = null,
    val city: String? = null,
    val createdAt: String? = null,
    val lastUsedAt: String? = null,
    val expiresAt: String? = null,
    val isCurrent: Boolean = false,
)

@Serializable
data class LinkStatusDto(
    val password: Boolean = false,
    val phone: Boolean = false,
    val google: Boolean = false,
    val github: Boolean = false,
    val facebook: Boolean = false,
)

@Serializable
data class UnlinkProviderRequest(
    val provider: String = "",
)

@Serializable
data class LinkStartRequest(
    val provider: String = "",
    val currentPassword: String? = null,
)

@Serializable
data class LinkOtpRequest(
    val provider: String = "",
    val otp: String = "",
)

@Serializable
data class LinkStartResultDto(
    val redirectUrl: String? = null,
    val requiresOtp: Boolean = false,
    val expiresAt: String? = null,
)

@Serializable
data class ExpiresAtDto(
    val expiresAt: String? = null,
)

@Serializable
data class TotpSetupDto(
    val secret: String? = null,
    val otpauthUrl: String? = null,
    val qrDataUrl: String? = null,
)

@Serializable
data class AuthEventDto(
    val id: String = "",
    val userId: String = "",
    val eventType: String = "",
    val method: String? = null,
    val status: String = "success",
    val platform: String? = null,
    val ip: String? = null,
    val device: String? = null,
    val browser: String? = null,
    val os: String? = null,
    val country: String? = null,
    val city: String? = null,
    val metadata: kotlinx.serialization.json.JsonObject? = null,
    val createdAt: String? = null,
)

@Serializable
data class UserEmailDto(
    val id: String = "",
    val email: String = "",
    val isVerified: Boolean = false,
    val createdAt: String? = null,
)

@Serializable
data class UserEmailsDto(
    val primary: PrimaryEmailDto? = null,
    val secondary: List<UserEmailDto> = emptyList(),
)

@Serializable
data class PrimaryEmailDto(
    val email: String? = null,
    val isVerified: Boolean = false,
)

@Serializable
data class AddUserEmailRequest(
    val email: String = "",
)

@Serializable
data class VerifyUserEmailRequest(
    val email: String = "",
    val otp: String? = null,
    val token: String? = null,
)

@Serializable
data class MakePrimaryEmailRequest(
    val email: String = "",
    val otp: String = "",
)

@Serializable
data class RemoveUserEmailRequest(
    val email: String = "",
)

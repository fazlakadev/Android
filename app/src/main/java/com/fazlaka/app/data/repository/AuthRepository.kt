package com.fazlaka.app.data.repository

import com.fazlaka.app.core.datastore.SessionManager
import com.fazlaka.app.core.model.dto.AcceptTermsRequest
import com.fazlaka.app.core.model.dto.AuthEventDto
import com.fazlaka.app.core.model.dto.AuthResultDto
import com.fazlaka.app.core.model.dto.ChangeEmailConfirmRequest
import com.fazlaka.app.core.model.dto.ChangeEmailRequest
import com.fazlaka.app.core.model.dto.ChangePasswordRequest
import com.fazlaka.app.core.model.dto.ForgotPasswordRequest
import com.fazlaka.app.core.model.dto.LinkStatusDto
import com.fazlaka.app.core.model.dto.LoginRequest
import com.fazlaka.app.core.model.dto.LogoutRequest
import com.fazlaka.app.core.model.dto.Paginated
import com.fazlaka.app.core.model.dto.PhoneChallengeDto
import com.fazlaka.app.core.model.dto.PhoneCompleteRequest
import com.fazlaka.app.core.model.dto.RegisterPhoneRequest
import com.fazlaka.app.core.model.dto.RegisterRequest
import com.fazlaka.app.core.model.dto.ResetPasswordRequest
import com.fazlaka.app.core.model.dto.SessionDto
import com.fazlaka.app.core.model.dto.SuccessDto
import com.fazlaka.app.core.model.dto.SimpleMessageDto
import com.fazlaka.app.core.model.dto.TotpSetupDto
import com.fazlaka.app.core.model.dto.TwoFactorVerifyRequest
import com.fazlaka.app.core.model.dto.UnlinkProviderRequest
import com.fazlaka.app.core.model.dto.UserDto
import com.fazlaka.app.core.model.dto.UserEmailDto
import com.fazlaka.app.core.model.dto.UserEmailsDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.core.network.ApiService
import com.fazlaka.app.core.network.safeApiCall
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository(
    private val api: ApiService,
    private val session: SessionManager,
    private val json: Json,
) {

    suspend fun login(email: String, password: String): ApiResult<AuthResultDto> =
        safeApiCall({ api.login(LoginRequest(email, password)) }, json)

    suspend fun googleNativeLogin(idToken: String): ApiResult<AuthResultDto> =
        safeApiCall({ api.googleNativeLogin(com.fazlaka.app.core.model.dto.GoogleNativeLoginRequest(idToken)) }, json)

    suspend fun register(request: RegisterRequest): ApiResult<AuthResultDto> =
        safeApiCall({ api.register(request) }, json)

    suspend fun loginTwoFactor(email: String, otp: String): ApiResult<AuthResultDto> =
        safeApiCall({ api.loginTwoFactor(TwoFactorVerifyRequest(email, otp)) }, json)

    suspend fun registerPhone(request: RegisterPhoneRequest): ApiResult<PhoneChallengeDto> =
        safeApiCall({ api.registerPhone(request) }, json)

    suspend fun requestPhoneLogin(request: RegisterPhoneRequest): ApiResult<PhoneChallengeDto> =
        safeApiCall({ api.requestPhoneLogin(request) }, json)

    suspend fun completePhoneAuth(request: PhoneCompleteRequest): ApiResult<AuthResultDto> =
        safeApiCall({ api.completePhoneAuth(request) }, json)

    suspend fun resendPhoneCode(request: PhoneCompleteRequest): ApiResult<PhoneChallengeDto> =
        safeApiCall({ api.resendPhoneCode(request) }, json)

    suspend fun forgotPassword(email: String): ApiResult<SimpleMessageDto> =
        safeApiCall({ api.forgotPassword(ForgotPasswordRequest(email)) }, json)

    suspend fun resetPassword(request: ResetPasswordRequest): ApiResult<SuccessDto> =
        safeApiCall({ api.resetPassword(request) }, json)

    suspend fun verifyEmail(request: com.fazlaka.app.core.model.dto.VerifyEmailRequest): ApiResult<SuccessDto> =
        safeApiCall({ api.verifyEmail(request) }, json)

    suspend fun resendVerification(request: com.fazlaka.app.core.model.dto.ResendVerificationRequest): ApiResult<SimpleMessageDto> =
        safeApiCall({ api.resendVerification(request) }, json)

    suspend fun changePassword(current: String, new: String): ApiResult<SuccessDto> =
        safeApiCall({ api.changePassword(ChangePasswordRequest(current, new)) }, json)

    suspend fun acceptTerms(username: String?): ApiResult<AuthResultDto> =
        safeApiCall({ api.acceptTerms(AcceptTermsRequest(username = username)) }, json)

    suspend fun me(): ApiResult<UserDto> = safeApiCall({ api.getAuthMe() }, json)

    suspend fun requestChangeEmail(newEmail: String) =
        safeApiCall({ api.requestChangeEmail(ChangeEmailRequest(newEmail)) }, json)

    suspend fun confirmChangeEmail(newEmail: String, otp: String): ApiResult<SuccessDto> =
        safeApiCall({ api.confirmChangeEmail(ChangeEmailConfirmRequest(newEmail, otp)) }, json)

    suspend fun sessions(): ApiResult<List<SessionDto>> =
        safeApiCall({ api.sessions(session.refreshTokenValue()) }, json)

    suspend fun revokeSession(id: String): ApiResult<SuccessDto> =
        safeApiCall({ api.revokeSession(id) }, json)

    suspend fun revokeOtherSessions(): ApiResult<SuccessDto> =
        safeApiCall({ api.revokeOtherSessions(LogoutRequest(session.refreshTokenValue() ?: "")) }, json)

    suspend fun linkStatus(): ApiResult<LinkStatusDto> =
        safeApiCall({ api.linkStatus() }, json)

    suspend fun unlinkProvider(provider: String): ApiResult<LinkStatusDto> =
        safeApiCall({ api.unlinkProvider(UnlinkProviderRequest(provider)) }, json)

    /** Starts an OAuth link flow. Returns the parsed result plus the signed
     *  `fazlaka_link` intent cookie the WebView must carry to complete linking. */
    suspend fun startOauthLink(
        provider: String,
        currentPassword: String?,
    ): Pair<ApiResult<com.fazlaka.app.core.model.dto.LinkStartResultDto>, String?> {
        val resp = try {
            api.startOauthLinkRaw(
                com.fazlaka.app.core.model.dto.LinkStartRequest(
                    provider,
                    currentPassword?.takeIf { it.isNotBlank() },
                ),
            )
        } catch (e: Exception) {
            return ApiResult.Failure(-2, e.message) to null
        }
        val cookie = resp.headers().values("Set-Cookie")
            .firstOrNull { it.startsWith("fazlaka_link=") }
            ?.substringAfter("fazlaka_link=")
            ?.substringBefore(";")
        val result = safeApiCall({
            if (resp.isSuccessful) {
                resp.body() ?: throw java.io.IOException("empty")
            } else {
                throw retrofit2.HttpException(resp)
            }
        }, json)
        return result to cookie
    }

    /** Confirms an OTP for passwordless accounts before opening the link WebView. */
    suspend fun confirmOauthLinkOtp(
        provider: String,
        otp: String,
    ): Pair<ApiResult<com.fazlaka.app.core.model.dto.LinkStartResultDto>, String?> {
        val resp = try {
            api.confirmOauthLinkOtpRaw(
                com.fazlaka.app.core.model.dto.LinkOtpRequest(provider, otp),
            )
        } catch (e: Exception) {
            return ApiResult.Failure(-2, e.message) to null
        }
        val cookie = resp.headers().values("Set-Cookie")
            .firstOrNull { it.startsWith("fazlaka_link=") }
            ?.substringAfter("fazlaka_link=")
            ?.substringBefore(";")
        val result = safeApiCall({
            if (resp.isSuccessful) {
                resp.body() ?: throw java.io.IOException("empty")
            } else {
                throw retrofit2.HttpException(resp)
            }
        }, json)
        return result to cookie
    }

    suspend fun removePhone(): ApiResult<SuccessDto> =
        safeApiCall({ api.removePhone() }, json)

    suspend fun userEmails(): ApiResult<UserEmailsDto> =
        safeApiCall({ api.userEmails() }, json)

    suspend fun addUserEmail(email: String): ApiResult<UserEmailDto> =
        safeApiCall({ api.addUserEmail(com.fazlaka.app.core.model.dto.AddUserEmailRequest(email)) }, json)

    suspend fun verifyUserEmail(email: String, otp: String? = null, token: String? = null): ApiResult<SuccessDto> =
        safeApiCall({ api.verifyUserEmail(com.fazlaka.app.core.model.dto.VerifyUserEmailRequest(email, otp, token)) }, json)

    suspend fun requestPrimaryEmail(email: String): ApiResult<com.fazlaka.app.core.model.dto.ExpiresAtDto> =
        safeApiCall({ api.requestPrimaryEmail(com.fazlaka.app.core.model.dto.AddUserEmailRequest(email)) }, json)

    suspend fun makePrimaryEmail(email: String, otp: String): ApiResult<SuccessDto> =
        safeApiCall({ api.makePrimaryEmail(com.fazlaka.app.core.model.dto.MakePrimaryEmailRequest(email, otp)) }, json)

    suspend fun removeUserEmail(email: String): ApiResult<SuccessDto> =
        safeApiCall({ api.removeUserEmail(com.fazlaka.app.core.model.dto.RemoveUserEmailRequest(email)) }, json)

    suspend fun securityEvents(page: Int = 1, limit: Int = 50): ApiResult<Paginated<AuthEventDto>> =
        safeApiCall({ api.securityEvents(page, limit) }, json)

    suspend fun setupTotp(): ApiResult<TotpSetupDto> =
        safeApiCall({ api.setupTotp() }, json)

    suspend fun enableTotp(code: String): ApiResult<SuccessDto> =
        safeApiCall({ api.enableTotp(com.fazlaka.app.core.model.dto.TotpRequest(code)) }, json)

    suspend fun disableTotp(code: String): ApiResult<SuccessDto> =
        safeApiCall({ api.disableTotp(com.fazlaka.app.core.model.dto.TotpRequest(code)) }, json)

    suspend fun persistSession(result: AuthResultDto): Boolean {
        val access = result.accessToken ?: return false
        return runCatching {
            session.saveSession(result)
            true
        }.getOrDefault(false)
    }

    suspend fun logout() {
        val refresh = session.refreshTokenValue()
        if (!refresh.isNullOrEmpty()) {
            runCatching { api.logout(LogoutRequest(refresh)) }
        }
        session.clearSession()
    }
}

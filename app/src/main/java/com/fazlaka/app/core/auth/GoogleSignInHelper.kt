package com.fazlaka.app.core.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.fazlaka.app.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class GoogleSignInHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    suspend fun signIn(): String = withContext(Dispatchers.IO) {
        val serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        if (serverClientId.isBlank()) {
            throw GoogleSignInException("Google Sign-In is not configured. Please contact support.")
        }

        try {
            getGoogleIdToken(filterByAuthorizedAccounts = true, serverClientId)
        } catch (_: Exception) {
            getGoogleIdToken(filterByAuthorizedAccounts = false, serverClientId)
        }
    }

    private suspend fun getGoogleIdToken(
        filterByAuthorizedAccounts: Boolean,
        serverClientId: String,
    ): String {
        val option = if (filterByAuthorizedAccounts) {
            GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(serverClientId)
                .build()
        } else {
            GetSignInWithGoogleOption.Builder(serverClientId)
                .build()
        }

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        val credentialManager = CredentialManager.create(context)
        val executor = Executors.newSingleThreadExecutor()

        val response: GetCredentialResponse = suspendCancellableCoroutine { cont ->
            credentialManager.getCredentialAsync(
                context,
                request,
                null,
                executor,
                object : CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {
                    override fun onResult(result: GetCredentialResponse) {
                        if (cont.isActive) cont.resume(result)
                    }

                    override fun onError(e: GetCredentialException) {
                        if (cont.isActive) cont.resumeWithException(e)
                    }
                },
            )
        }

        val credential = response.credential
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        return googleIdTokenCredential.idToken
    }
}

class GoogleSignInException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

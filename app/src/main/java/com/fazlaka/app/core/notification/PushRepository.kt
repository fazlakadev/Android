package com.fazlaka.app.core.notification

import android.util.Log
import com.fazlaka.app.BuildConfig
import com.fazlaka.app.core.network.ApiService
import com.fazlaka.app.core.network.safeApiCall
import com.fazlaka.app.core.datastore.SessionManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushRepository @Inject constructor(
    private val api: ApiService,
    private val sessionManager: SessionManager,
    private val json: Json,
) {
    suspend fun registerToken(token: String) {
        val userId = sessionManager.currentUserId.first() ?: return
        val deviceName = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
        val os = "Android ${android.os.Build.VERSION.RELEASE}"
        val appVersion = BuildConfig.VERSION_NAME

        safeApiCall({
            api.registerDevice(
                mapOf(
                    "token" to token,
                    "platform" to "android",
                    "deviceName" to deviceName,
                    "os" to os,
                    "appVersion" to appVersion,
                )
            )
        }, json)
    }

    suspend fun unregisterToken(token: String) {
        safeApiCall({
            api.unregisterDevice(mapOf("token" to token))
        }, json)
    }

    suspend fun fetchAndRegisterToken() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d("FCM", "FCM Registration Token: $token")
            registerToken(token)
        } catch (e: Exception) {
            Log.e("FCM", "Failed to fetch FCM token", e)
        }
    }
}

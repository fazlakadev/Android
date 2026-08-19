package com.fazlaka.app.core.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fazlaka.app.MainActivity
import com.fazlaka.app.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FazlakaMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var pushRepository: PushRepository

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        android.util.Log.d("FCM", "onNewToken: $token")
        CoroutineScope(Dispatchers.IO).launch {
            pushRepository.registerToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        val title = message.notification?.title ?: data["title"] ?: return
        val body = message.notification?.body ?: data["body"] ?: ""
        val channelId = data["channelId"] ?: "general"
        val type = data["type"] ?: "system"
        val notificationId = data["notificationId"]?.toIntOrNull() ?: System.currentTimeMillis().toInt()

        notificationHelper.showNotification(
            title = title,
            body = body,
            channelId = channelId,
            notificationId = notificationId,
            data = data,
        )
    }
}

package com.fazlaka.app.core.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.fazlaka.app.FazlakaApp
import com.fazlaka.app.MainActivity
import com.fazlaka.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val notificationManager =
        context.getSystemService(NotificationManager::class.java)

    private val scope = CoroutineScope(Dispatchers.IO)

    fun showNotification(
        title: String,
        body: String,
        channelId: String = FazlakaApp.CHANNEL_GENERAL,
        notificationId: Int = System.currentTimeMillis().toInt(),
        data: Map<String, String> = emptyMap(),
    ) {
        val intent = createDeepLinkIntent(data)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val imageUrl = data["imageUrl"]
        if (!imageUrl.isNullOrBlank()) {
            scope.launch {
                try {
                    val url = URL(imageUrl)
                    val inputStream = url.openStream()
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()

                    withContext(Dispatchers.Main) {
                        builder
                            .setLargeIcon(bitmap)
                            .setStyle(
                                NotificationCompat.BigPictureStyle()
                                    .bigPicture(bitmap)
                                    .setBigContentTitle(title)
                                    .setSummaryText(body),
                            )
                        addTypeActions(builder, notificationId, data)
                        safeNotify(notificationId, builder.build())
                    }
                } catch (_: Exception) {
                    withContext(Dispatchers.Main) {
                        addTypeActions(builder, notificationId, data)
                        safeNotify(notificationId, builder.build())
                    }
                }
            }
        } else {
            addTypeActions(builder, notificationId, data)
            safeNotify(notificationId, builder.build())
        }
    }

    private fun addTypeActions(
        builder: NotificationCompat.Builder,
        notificationId: Int,
        data: Map<String, String>,
    ) {
        when (data["type"]) {
            "friend_request" -> {
                val viewIntent = createDeepLinkIntent(data + ("action" to "view"))
                val viewPending = PendingIntent.getActivity(
                    context,
                    notificationId + 1,
                    viewIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                builder.addAction(
                    R.mipmap.ic_launcher,
                    context.getString(R.string.notif_action_view),
                    viewPending,
                )
            }
            "comment", "like" -> {
                val viewIntent = createDeepLinkIntent(data + ("action" to "view"))
                val viewPending = PendingIntent.getActivity(
                    context,
                    notificationId + 1,
                    viewIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                builder.addAction(
                    R.mipmap.ic_launcher,
                    context.getString(R.string.notif_action_open),
                    viewPending,
                )
            }
            "announcement" -> {
                val deepLink = data["deepLink"]
                if (!deepLink.isNullOrBlank()) {
                    val linkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val linkPending = PendingIntent.getActivity(
                        context,
                        notificationId + 1,
                        linkIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )
                    builder.addAction(
                        R.mipmap.ic_launcher,
                        context.getString(R.string.notif_action_open),
                        linkPending,
                    )
                }
            }
            "app_update" -> {
                val mainIntent = createDeepLinkIntent(data + ("action" to "update"))
                val updatePending = PendingIntent.getActivity(
                    context,
                    notificationId + 1,
                    mainIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                builder
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .addAction(
                        R.mipmap.ic_launcher,
                        "تحديث",
                        updatePending,
                    )
            }
        }
    }

    private fun safeNotify(notificationId: Int, notification: android.app.Notification) {
        try {
            notificationManager.notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Notification permission not granted
        }
    }

    private fun createDeepLinkIntent(data: Map<String, String>): Intent {
        val deepLink = data["deepLink"]
        if (!deepLink.isNullOrBlank()) {
            return Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }

        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_type", data["type"])
            putExtra("notification_id", data["notificationId"])
            putExtra("content_type", data["contentType"])
            putExtra("content_id", data["contentId"])
            putExtra("conversation_id", data["conversationId"])
            putExtra("user_id", data["userId"])
        }
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}

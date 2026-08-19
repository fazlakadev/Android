package com.fazlaka.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.ImageRequest
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
@UnstableApi
class FazlakaApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .crossfade(true)
            .crossfade(300)
            .respectCacheHeaders(false)
            .build()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val mediaChannel = NotificationChannel(
            CHANNEL_MEDIA,
            getString(R.string.notification_channel_media),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.notification_channel_media)
            setShowBadge(false)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }

        val messagesChannel = NotificationChannel(
            CHANNEL_MESSAGES,
            getString(R.string.notification_channel_messages),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = getString(R.string.notification_channel_messages)
            enableVibration(true)
            setShowBadge(true)
        }

        val socialChannel = NotificationChannel(
            CHANNEL_SOCIAL,
            getString(R.string.notification_channel_social),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = getString(R.string.notification_channel_social)
            setShowBadge(true)
        }

        val contentChannel = NotificationChannel(
            CHANNEL_CONTENT,
            getString(R.string.notification_channel_content),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = getString(R.string.notification_channel_content)
            setShowBadge(true)
        }

        val generalChannel = NotificationChannel(
            CHANNEL_GENERAL,
            getString(R.string.notification_channel_general),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = getString(R.string.notification_channel_general)
            setShowBadge(true)
        }

        manager.createNotificationChannels(
            listOf(mediaChannel, messagesChannel, socialChannel, contentChannel, generalChannel)
        )
    }

    companion object {
        const val CHANNEL_MEDIA = "media"
        const val CHANNEL_MESSAGES = "messages"
        const val CHANNEL_SOCIAL = "social"
        const val CHANNEL_CONTENT = "content"
        const val CHANNEL_GENERAL = "general"
    }
}

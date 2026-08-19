package com.fazlaka.app.core.update

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.fazlaka.app.MainActivity
import com.fazlaka.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed class DownloadState {
    data object Idle : DownloadState()
    data object Downloading : DownloadState()
    data class Progress(val percent: Int) : DownloadState()
    data class Completed(val file: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

@AndroidEntryPoint
class UpdateDownloadService : Service() {

    @Inject
    lateinit var okHttpClient: OkHttpClient

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var downloadJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra(EXTRA_URL) ?: run {
            stopSelf()
            return START_NOT_STICKY
        }

        downloadJob?.cancel()
        downloadJob = scope.launch {
            acquireWakeLock()
            updateState(DownloadState.Downloading)
            startForeground(NOTIFICATION_ID, buildNotification(0))

            try {
                val file = downloadApk(url)
                if (file != null && file.exists()) {
                    updateState(DownloadState.Completed(file))
                    updateNotificationComplete()
                    openInstaller(file)
                } else {
                    updateState(DownloadState.Error("Download failed"))
                    updateNotificationError()
                }
            } catch (e: Exception) {
                updateState(DownloadState.Error(e.message ?: "Download failed"))
                updateNotificationError()
            } finally {
                releaseWakeLock()
                stopForeground(STOP_FOREGROUND_DETACH)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        downloadJob?.cancel()
        scope.cancel()
        releaseWakeLock()
        super.onDestroy()
    }

    private suspend fun downloadApk(url: String): File? = withContext(Dispatchers.IO) {
        try {
            cleanupOldApks()
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
                .newCall(request)
                .execute()

            if (!response.isSuccessful) return@withContext null
            val body = response.body ?: return@withContext null
            val contentLength = body.contentLength()
            val outputFile = File(updatesDir, "fazlaka_update.apk")

            body.byteStream().use { input ->
                outputFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalRead = 0L
                    var lastPercent = 0

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        if (!isActive) {
                            outputFile.delete()
                            return@withContext null
                        }
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (contentLength > 0) {
                            val percent = (totalRead * 100 / contentLength).toInt()
                            if (percent != lastPercent) {
                                lastPercent = percent
                                updateState(DownloadState.Progress(percent))
                                updateNotificationProgress(percent)
                            }
                        }
                    }
                }
            }
            outputFile
        } catch (e: Exception) {
            null
        }
    }

    private fun openInstaller(file: File) {
        val uri: Uri = androidx.core.content.FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (packageManager.canRequestPackageInstalls()) {
                startActivity(intent)
            } else {
                val settingsIntent = Intent(
                    android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:$packageName"),
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(settingsIntent)
            }
        } else {
            startActivity(intent)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_UPDATE,
                getString(R.string.notification_channel_update),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = getString(R.string.notification_channel_update_desc)
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(progress: Int): android.app.Notification {
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_UPDATE)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.update_downloading_format, progress))
            .setProgress(100, progress, progress == 0)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .setOnlyAlertOnce(true)

        if (progress > 0) {
            builder.setProgress(100, progress, false)
        }

        return builder.build()
    }

    private fun updateNotificationProgress(percent: Int) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(percent))
    }

    private fun updateNotificationComplete() {
        val manager = getSystemService(NotificationManager::class.java)
        val intent = PendingIntent.getActivity(
            this, 0,
            Intent(Intent.ACTION_VIEW).apply {
                val file = File(updatesDir, "fazlaka_update.apk")
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    this@UpdateDownloadService,
                    "${packageName}.fileprovider",
                    file,
                )
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_UPDATE)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.update_install))
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun updateNotificationError() {
        val manager = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, CHANNEL_UPDATE)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.update_error))
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "fazlaka:update:download",
        ).apply {
            acquire(10 * 60 * 1000L)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }

    private fun cleanupOldApks() {
        updatesDir.listFiles()?.forEach { file ->
            if (file.name != "fazlaka_update.apk") file.delete()
        }
    }

    companion object {
        const val CHANNEL_UPDATE = "update_download"
        const val NOTIFICATION_ID = 9999
        const val EXTRA_URL = "download_url"

        private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
        val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

        internal fun updateState(state: DownloadState) {
            _downloadState.value = state
        }

        fun start(context: Context, url: String) {
            val intent = Intent(context, UpdateDownloadService::class.java).apply {
                putExtra(EXTRA_URL, url)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, UpdateDownloadService::class.java))
        }

        private val updatesDir: File
            get() = File(
                android.os.Environment.getDownloadCacheDirectory().parentFile
                    ?: File("/data/data/com.fazlaka.app/cache"),
                "fazlaka/updates",
            ).also { it.mkdirs() }

        fun getUpdateFile(context: Context): File {
            val dir = File(context.cacheDir, "updates").also { it.mkdirs() }
            return File(dir, "fazlaka_update.apk")
        }
    }
}

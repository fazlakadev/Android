package com.fazlaka.app.core.update

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.fazlaka.app.core.network.ApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class UpdateInfo(
    val version: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val publishedAt: String,
    val forceUpdate: Boolean,
    val forceUpdateMessage: String?,
    val minVersion: String?,
)

@Singleton
class UpdateChecker @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context,
) {
    fun getCurrentVersion(): String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
    } catch (_: PackageManager.NameNotFoundException) {
        "0.0.0"
    }

    fun getCurrentVersionCode(): Long = try {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0).let { info ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.longVersionCode
            } else {
                info.versionCode.toLong()
            }
        }
    } catch (_: PackageManager.NameNotFoundException) {
        0L
    }

    suspend fun checkForUpdate(): UpdateInfo? {
        return try {
            val response = apiService.getLatestAppVersion()
            if (!response.success) return null

            val data = response.data
            val remoteVersion = data.version
            val localVersion = getCurrentVersion()
            val forceUpdate = data.forceUpdate
            val minVersion = data.minVersion

            val belowMin = minVersion != null && isNewerVersion(minVersion, localVersion)
            val hasUpdate = isNewerVersion(remoteVersion, localVersion)

            if (hasUpdate || belowMin) {
                UpdateInfo(
                    version = data.version,
                    releaseNotes = data.releaseNotes,
                    downloadUrl = data.downloadUrl,
                    publishedAt = data.publishedAt,
                    forceUpdate = forceUpdate || belowMin,
                    forceUpdateMessage = data.forceUpdateMessage,
                    minVersion = data.minVersion,
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("UpdateChecker", "Failed to check for update", e)
            null
        }
    }

    private fun isNewerVersion(remote: String, local: String): Boolean {
        val remoteParts = remote.split(".").mapNotNull { it.toIntOrNull() }
        val localParts = local.split(".").mapNotNull { it.toIntOrNull() }

        val maxSize = maxOf(remoteParts.size, localParts.size)

        for (i in 0 until maxSize) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }

        return false
    }
}

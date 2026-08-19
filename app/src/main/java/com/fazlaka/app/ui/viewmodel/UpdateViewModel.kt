package com.fazlaka.app.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fazlaka.app.core.update.UpdateChecker
import com.fazlaka.app.core.update.UpdateInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Checking : UpdateState()
    data class Available(val info: UpdateInfo) : UpdateState()
    data object Downloading : UpdateState()
    data class DownloadProgress(val progress: Float) : UpdateState()
    data object Installing : UpdateState()
    data object UpToDate : UpdateState()
    data class Error(val message: String) : UpdateState()
}

@HiltViewModel
class UpdateViewModel @Inject constructor(
    application: Application,
    private val updateChecker: UpdateChecker,
) : AndroidViewModel(application) {

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _currentUpdateInfo = MutableStateFlow<UpdateInfo?>(null)
    val currentUpdateInfo: StateFlow<UpdateInfo?> = _currentUpdateInfo.asStateFlow()

    val currentVersion: String
        get() = updateChecker.getCurrentVersion()

    val isForceUpdate: Boolean
        get() = (_currentUpdateInfo.value?.forceUpdate == true)

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val updatesDir: File
        get() = File(getApplication<Application>().cacheDir, "updates").also { it.mkdirs() }

    fun checkForUpdates() {
        viewModelScope.launch {
            _updateState.value = UpdateState.Checking
            try {
                val updateInfo = updateChecker.checkForUpdate()
                if (updateInfo == null) {
                    _updateState.value = UpdateState.UpToDate
                    return@launch
                }
                _currentUpdateInfo.value = updateInfo
                _updateState.value = UpdateState.Available(updateInfo)
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun downloadAndInstall() {
        val current = _updateState.value
        if (current !is UpdateState.Available) return

        viewModelScope.launch {
            _updateState.value = UpdateState.Downloading
            try {
                val file = downloadApk(
                    url = current.info.downloadUrl,
                    onProgress = { progress ->
                        _updateState.value = UpdateState.DownloadProgress(progress)
                    },
                )
                if (file != null && file.exists()) {
                    _updateState.value = UpdateState.Installing
                    installApk(file)
                } else {
                    _updateState.value = UpdateState.Error("Download failed")
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.message ?: "Download failed")
            }
        }
    }

    private suspend fun downloadApk(
        url: String,
        onProgress: (Float) -> Unit,
    ): File? = withContext(Dispatchers.IO) {
        try {
            cleanupOldApks()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null
            val body = response.body ?: return@withContext null
            val contentLength = body.contentLength()
            val outputFile = File(updatesDir, "fazlaka_update.apk")

            body.byteStream().use { input ->
                outputFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalRead = 0L
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        kotlinx.coroutines.ensureActive()
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (contentLength > 0) {
                            onProgress(totalRead.toFloat() / contentLength.toFloat())
                        }
                    }
                }
            }
            outputFile
        } catch (e: Exception) {
            null
        }
    }

    private fun installApk(file: File) {
        val context = getApplication<Application>()
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (context.packageManager.canRequestPackageInstalls()) {
                context.startActivity(intent)
            } else {
                val settingsIntent = Intent(
                    android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}"),
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
            }
        } else {
            context.startActivity(intent)
        }
    }

    private fun cleanupOldApks() {
        updatesDir.listFiles()?.forEach { file ->
            if (file.name != "fazlaka_update.apk") file.delete()
        }
    }

    fun canInstallPackages(): Boolean {
        val app = getApplication<Application>()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            app.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }
}

package com.fazlaka.app.ui.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fazlaka.app.core.update.UpdateChecker
import com.fazlaka.app.core.update.UpdateDownloader
import com.fazlaka.app.core.update.UpdateInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Application.updateDataStore by preferencesDataStore(name = "fazlaka_update")

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Checking : UpdateState()
    data class Available(val info: UpdateInfo) : UpdateState()
    data object Downloading : UpdateState()
    data class DownloadProgress(val progress: Float) : UpdateState()
    data object Installing : UpdateState()
    data object UpToDate : UpdateState()
    data class Error(val message: String) : UpdateState()
    data object Dismissed : UpdateState()
}

@HiltViewModel
class UpdateViewModel @Inject constructor(
    application: Application,
    private val updateChecker: UpdateChecker,
    private val updateDownloader: UpdateDownloader,
) : AndroidViewModel(application) {

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _currentUpdateInfo = MutableStateFlow<UpdateInfo?>(null)
    val currentUpdateInfo: StateFlow<UpdateInfo?> = _currentUpdateInfo.asStateFlow()

    val currentVersion: String
        get() = updateChecker.getCurrentVersion()

    companion object {
        val KEY_SKIPPED_VERSION = stringPreferencesKey("skipped_update_version")
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _updateState.value = UpdateState.Checking

            val skippedVersion = getApplication<Application>()
                .updateDataStore.data.first()[KEY_SKIPPED_VERSION]

            try {
                val updateInfo = updateChecker.checkForUpdate()

                if (updateInfo == null) {
                    _updateState.value = UpdateState.UpToDate
                    return@launch
                }

                if (updateInfo.version == skippedVersion) {
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
                val file = updateDownloader.downloadApk(
                    url = current.info.downloadUrl,
                    onProgress = { progress ->
                        _updateState.value = UpdateState.DownloadProgress(progress)
                    },
                )

                if (file != null && file.exists()) {
                    _updateState.value = UpdateState.Installing
                    updateDownloader.installApk(file)
                } else {
                    _updateState.value = UpdateState.Error("Download failed")
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.message ?: "Download failed")
            }
        }
    }

    fun dismissUpdate() {
        _currentUpdateInfo.value = null
        _updateState.value = UpdateState.Dismissed
    }

    fun skipVersion() {
        viewModelScope.launch {
            val current = _updateState.value
            if (current is UpdateState.Available) {
                getApplication<Application>().updateDataStore.edit { prefs ->
                    prefs[KEY_SKIPPED_VERSION] = current.info.version
                }
            }
            _currentUpdateInfo.value = null
            _updateState.value = UpdateState.Dismissed
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

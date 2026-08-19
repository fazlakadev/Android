package com.fazlaka.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fazlaka.app.core.update.UpdateChecker
import com.fazlaka.app.core.update.UpdateDownloadService
import com.fazlaka.app.core.update.UpdateInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Checking : UpdateState()
    data class Available(val info: UpdateInfo) : UpdateState()
    data object Downloading : UpdateState()
    data class DownloadProgress(val progress: Int) : UpdateState()
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
        get() = _currentUpdateInfo.value?.forceUpdate == true

    init {
        viewModelScope.launch {
            UpdateDownloadService.downloadState.collectLatest { ds ->
                when (ds) {
                    is com.fazlaka.app.core.update.DownloadState.Downloading -> {
                        _updateState.value = UpdateState.Downloading
                    }
                    is com.fazlaka.app.core.update.DownloadState.Progress -> {
                        _updateState.value = UpdateState.DownloadProgress(ds.percent)
                    }
                    is com.fazlaka.app.core.update.DownloadState.Completed -> {
                        _updateState.value = UpdateState.Installing
                    }
                    is com.fazlaka.app.core.update.DownloadState.Error -> {
                        _updateState.value = UpdateState.Error(ds.message)
                    }
                    is com.fazlaka.app.core.update.DownloadState.Idle -> {}
                }
            }
        }
    }

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

        val context = getApplication<Application>()
        _updateState.value = UpdateState.Downloading
        UpdateDownloadService.start(context, current.info.downloadUrl)
    }

    fun retry() {
        _updateState.value = UpdateState.Idle
        checkForUpdates()
    }

    fun dismiss() {
        _updateState.value = UpdateState.Idle
    }
}

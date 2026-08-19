package com.fazlaka.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.fazlaka.app.core.network.NetworkMonitor
import com.fazlaka.app.core.network.NetworkStatus
import com.fazlaka.app.core.network.OfflineManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ConnectivityViewModel @Inject constructor(
    val networkMonitor: NetworkMonitor,
    val offlineManager: OfflineManager,
) : ViewModel() {
    val networkStatus: StateFlow<NetworkStatus> = networkMonitor.status
    val pendingCount = offlineManager.pendingCount
}

package com.fazlaka.app.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

enum class NetworkStatus {
    ONLINE,
    OFFLINE,
    METERED,
}

@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val connectivityManager =
        context.getSystemService(ConnectivityManager::class.java)

    private val _status = MutableStateFlow(determineCurrentStatus())
    val status: StateFlow<NetworkStatus> = _status.asStateFlow()

    val isOnline: Flow<Boolean> = status.map { it != NetworkStatus.OFFLINE }

    init {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val caps = connectivityManager.getNetworkCapabilities(network)
                val metered = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) == false
                _status.value = if (metered) NetworkStatus.METERED else NetworkStatus.ONLINE
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                val metered = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED).not()
                _status.value = if (metered) NetworkStatus.METERED else NetworkStatus.ONLINE
            }

            override fun onLost(network: Network) {
                // Check if any other network is available
                val activeNetwork = connectivityManager.activeNetwork
                if (activeNetwork == null) {
                    _status.value = NetworkStatus.OFFLINE
                } else {
                    val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
                    val hasInternet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                    _status.value = if (hasInternet) NetworkStatus.ONLINE else NetworkStatus.OFFLINE
                }
            }
        }

        connectivityManager.registerNetworkCallback(request, callback)
    }

    private fun determineCurrentStatus(): NetworkStatus {
        val activeNetwork = connectivityManager.activeNetwork ?: return NetworkStatus.OFFLINE
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return NetworkStatus.OFFLINE
        val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        if (!hasInternet) return NetworkStatus.OFFLINE
        val notMetered = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        return if (notMetered) NetworkStatus.ONLINE else NetworkStatus.METERED
    }
}

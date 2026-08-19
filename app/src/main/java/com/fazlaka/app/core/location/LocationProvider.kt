package com.fazlaka.app.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@EntryPoint
@InstallIn(SingletonComponent::class)
interface LocationEntryPoint {
    fun locationProvider(): LocationProvider
}

fun locationProvider(context: Context): LocationProvider {
    return EntryPointAccessors.fromApplication(
        context.applicationContext,
        LocationEntryPoint::class.java,
    ).locationProvider()
}

@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val fusedClient by lazy { LocationServices.getFusedLocationProviderClient(context) }

    @Volatile
    private var cachedLat: Double? = null

    @Volatile
    private var cachedLng: Double? = null

    @Volatile
    private var enabled = false

    val lat: Double?
        get() = cachedLat

    val lng: Double?
        get() = cachedLng

    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
    }

    fun start() {
        if (enabled) return
        enabled = true
        if (!hasPermission()) return
        scope.launch {
            refreshOnce()
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun refreshOnce(): Location? {
        if (!hasPermission()) return null
        val location = suspendCoroutine<Location?> { cont ->
            var done = false
            val resumeOnce: (Location?) -> Unit = { loc ->
                if (!done) {
                    done = true
                    cont.resume(loc)
                }
            }
            try {
                fusedClient.lastLocation.addOnCompleteListener { task ->
                    resumeOnce(task.result)
                }
                fusedClient.lastLocation.addOnFailureListener { resumeOnce(null) }
            } catch (e: Exception) {
                resumeOnce(null)
            }
        }
        if (location != null) {
            cachedLat = location.latitude
            cachedLng = location.longitude
        }
        return location
    }

    fun clear() {
        cachedLat = null
        cachedLng = null
    }
}

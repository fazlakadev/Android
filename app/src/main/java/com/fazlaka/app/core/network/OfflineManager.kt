package com.fazlaka.app.core.network

import android.util.Log
import com.fazlaka.app.BuildConfig
import com.fazlaka.app.core.database.PendingActionDao
import com.fazlaka.app.core.database.PendingActionEntity
import com.fazlaka.app.core.network.NetworkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "OfflineManager"
private const val MAX_RETRIES = 3

@Singleton
class OfflineManager @Inject constructor(
    private val pendingActionDao: PendingActionDao,
    private val networkMonitor: NetworkMonitor,
    private val okHttpClient: dagger.Lazy<OkHttpClient>,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    init {
        // Start observing pending actions count
        scope.launch {
            pendingActionDao.count().collect { _pendingCount.value = it }
        }

        // When network comes back, sync pending actions
        scope.launch {
            networkMonitor.status.collect { status ->
                if (status == NetworkStatus.ONLINE || status == NetworkStatus.METERED) {
                    syncPendingActions()
                }
            }
        }
    }

    suspend fun enqueue(
        method: String,
        endpoint: String,
        body: String? = null,
        contentType: String? = "application/json",
        actionType: String = "generic",
        contentId: String? = null,
    ): Long {
        val id = pendingActionDao.insert(
            PendingActionEntity(
                type = actionType,
                method = method.uppercase(),
                endpoint = endpoint,
                body = body,
                contentType = contentType,
                contentId = contentId,
            ),
        )
        Log.d(TAG, "Enqueued $actionType $method $endpoint (id=$id)")
        return id
    }

    suspend fun syncPendingActions() {
        val actions = pendingActionDao.getAll()
        if (actions.isEmpty()) return

        Log.d(TAG, "Syncing ${actions.size} pending action(s)...")
        val client = okHttpClient.get()

        for (action in actions) {
            try {
                val requestBuilder = Request.Builder()
                    .url("${BuildConfig.API_BASE_URL.trimEnd('/')}${action.endpoint}")
                    .method(
                        action.method,
                        action.body?.toRequestBody(
                            (action.contentType ?: "application/json").toMediaType()
                        ),
                    )

                // Add auth token if available (auth interceptor handles this on main client,
                // but here we build manually for the offline sync)
                val response = client.newCall(requestBuilder.build()).execute()
                response.close()

                if (response.isSuccessful) {
                    pendingActionDao.deleteById(action.id)
                    Log.d(TAG, "Synced ${action.type} ${action.endpoint} — ${response.code}")
                } else if (response.code in 400..499) {
                    // Client error — don't retry, remove
                    pendingActionDao.deleteById(action.id)
                    Log.w(TAG, "Dropped ${action.type} ${action.endpoint} — ${response.code}")
                } else if (action.retryCount >= MAX_RETRIES) {
                    pendingActionDao.deleteById(action.id)
                    Log.w(TAG, "Max retries for ${action.type} ${action.endpoint}")
                } else {
                    pendingActionDao.incrementRetry(action.id)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Sync failed for ${action.type}: ${e.message}")
                if (action.retryCount >= MAX_RETRIES) {
                    pendingActionDao.deleteById(action.id)
                } else {
                    pendingActionDao.incrementRetry(action.id)
                }
            }
        }
    }

    suspend fun hasPendingContent(contentType: String, contentId: String): Boolean {
        val actions = pendingActionDao.getAll()
        return actions.any { it.contentType == contentType && it.contentId == contentId }
    }

    suspend fun clearAll() {
        pendingActionDao.clear()
    }
}

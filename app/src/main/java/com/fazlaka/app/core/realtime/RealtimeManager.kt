package com.fazlaka.app.core.realtime

import android.util.Log
import com.fazlaka.app.core.datastore.SessionManager
import com.fazlaka.app.core.model.dto.MessageDto
import com.fazlaka.app.core.model.dto.PusherAuthRequest
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.core.network.ApiService
import com.fazlaka.app.core.network.safeApiCall
import com.fazlaka.app.core.notification.NotificationHelper
import com.pusher.client.ChannelAuthorizer
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import com.pusher.client.connection.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

sealed interface RealtimeEvent {
    data class NewMessage(val conversationId: String, val message: MessageDto) : RealtimeEvent
    data class SentMessage(val conversationId: String, val message: MessageDto) : RealtimeEvent
    data class GroupInvite(val conversationId: String) : RealtimeEvent
    data class GroupRemoved(val conversationId: String) : RealtimeEvent
    data class Notification(val id: String, val title: String, val body: String) : RealtimeEvent
}

private data class PusherConfig(
    val key: String,
    val cluster: String,
    val useTLS: Boolean,
)

/**
 * Owns the Pusher websocket connection for the signed-in user.
 * Subscribes to `private-user-{userId}` and re-broadcasts backend events
 * (message:new, message:sent, group:invite, group:removed, notification:new).
 */
@Singleton
class RealtimeManager @Inject constructor(
    private val api: ApiService,
    private val json: Json,
    private val session: SessionManager,
    private val notificationHelper: NotificationHelper,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _events = MutableSharedFlow<RealtimeEvent>(extraBufferCapacity = 128)
    val events: SharedFlow<RealtimeEvent> = _events.asSharedFlow()

    private var pusher: Pusher? = null
    private var subscribedChannel: String? = null

    init {
        scope.launch {
            session.currentUserId.collectLatest { userId ->
                if (userId.isNullOrEmpty()) {
                    disconnect()
                } else {
                    connectFor(userId)
                }
            }
        }
    }

    private fun connectFor(userId: String) {
        val channelName = "private-user-$userId"
        if (subscribedChannel == channelName &&
            pusher?.connection?.state == ConnectionState.CONNECTED
        ) return
        scope.launch {
            try {
                val config = loadConfig() ?: return@launch
                val options = PusherOptions()
                    .setCluster(config.cluster)
                    .setUseTLS(config.useTLS)
                    .setChannelAuthorizer(authorizer)
                val newPusher = Pusher(config.key, options)
                newPusher.connect()
                newPusher.subscribePrivate(channelName, listener)
                pusher?.let { runCatching { it.disconnect() } }
                pusher = newPusher
                subscribedChannel = channelName
            } catch (e: Exception) {
                Log.w(TAG, "Pusher connect failed", e)
            }
        }
    }

    private fun disconnect() {
        scope.launch {
            runCatching { pusher?.disconnect() }
            pusher = null
            subscribedChannel = null
        }
    }

    private val authorizer = ChannelAuthorizer { channelName, socketId ->
        runBlocking { authorize(channelName, socketId) }
    }

    private val listener = object : PrivateChannelEventListener {
        override fun onEvent(event: PusherEvent) {
            scope.launch {
                val parsed = parseEvent(event.eventName, event.data) ?: return@launch
                _events.emit(parsed)

                // Show system notification for notification events
                if (parsed is RealtimeEvent.Notification) {
                    notificationHelper.showNotification(
                        title = parsed.title,
                        body = parsed.body,
                        channelId = "social",
                        notificationId = parsed.id.hashCode(),
                    )
                }
            }
        }

        override fun onSubscriptionSucceeded(channelName: String) {
            Log.i(TAG, "Subscribed to $channelName")
        }

        override fun onAuthenticationFailure(message: String, e: Exception) {
            Log.w(TAG, "Pusher channel auth failed: $message", e)
        }
    }

    private suspend fun authorize(channelName: String, socketId: String): String {
        val auth = try {
            val result = safeApiCall(
                { api.pusherAuth(PusherAuthRequest(socketId, channelName)) },
                json,
            )
            (result as? ApiResult.Success)?.data?.auth ?: ""
        } catch (e: Exception) {
            Log.w(TAG, "Pusher auth failed for $channelName", e)
            ""
        }
        return JSONObject().put("auth", auth).toString()
    }

    private suspend fun loadConfig(): PusherConfig? {
        val result = safeApiCall({ api.publicSettings(emptyMap()) }, json)
        val data = (result as? ApiResult.Success)?.data ?: return null
        val key = data["pusherKey"]?.jsonPrimitive?.contentOrNull ?: return null
        val cluster = data["pusherCluster"]?.jsonPrimitive?.contentOrNull ?: "eu"
        val useTLS = data["pusherUseTLS"]?.jsonPrimitive?.booleanOrNull ?: true
        return PusherConfig(key, cluster, useTLS)
    }

    private fun parseEvent(eventName: String, rawData: String): RealtimeEvent? {
        val obj = runCatching { json.parseToJsonElement(rawData).jsonObject }.getOrNull() ?: return null
        return when (eventName) {
            "message:new" -> {
                val conversationId = obj["conversationId"]?.jsonPrimitive?.contentOrNull ?: return null
                val message = obj["message"]?.let {
                    runCatching { json.decodeFromJsonElement(MessageDto.serializer(), it) }.getOrNull()
                } ?: return null
                RealtimeEvent.NewMessage(conversationId, message)
            }
            "message:sent" -> {
                val conversationId = obj["conversationId"]?.jsonPrimitive?.contentOrNull ?: return null
                val message = obj["message"]?.let {
                    runCatching { json.decodeFromJsonElement(MessageDto.serializer(), it) }.getOrNull()
                } ?: return null
                RealtimeEvent.SentMessage(conversationId, message)
            }
            "group:invite" ->
                RealtimeEvent.GroupInvite(obj["conversationId"]?.jsonPrimitive?.contentOrNull ?: return null)
            "group:removed" ->
                RealtimeEvent.GroupRemoved(obj["conversationId"]?.jsonPrimitive?.contentOrNull ?: return null)
            "notification:new" -> RealtimeEvent.Notification(
                id = obj["id"]?.jsonPrimitive?.contentOrNull ?: "",
                title = obj["title"]?.jsonPrimitive?.contentOrNull ?: "",
                body = obj["body"]?.jsonPrimitive?.contentOrNull ?: "",
            )
            else -> null
        }
    }

    companion object {
        private const val TAG = "RealtimeManager"
    }
}

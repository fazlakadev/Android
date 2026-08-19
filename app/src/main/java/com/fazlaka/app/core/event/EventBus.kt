package com.fazlaka.app.core.event

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

data class AppEvent(val message: String, val actionLabel: String? = null)

object EventBus {
    private val _events = Channel<AppEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    suspend fun emit(message: String, actionLabel: String? = null) {
        _events.send(AppEvent(message, actionLabel))
    }
}

package com.fazlaka.app.core.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PusherAuthRequest(
    @SerialName("socket_id")
    val socketId: String,
    @SerialName("channel_name")
    val channelName: String,
)

@Serializable
data class PusherAuthDto(
    val auth: String = "",
)

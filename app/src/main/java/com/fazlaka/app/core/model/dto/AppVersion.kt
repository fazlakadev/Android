package com.fazlaka.app.core.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class AppVersionData(
    val version: String = "",
    val tagName: String = "",
    val releaseNotes: String = "",
    val downloadUrl: String = "",
    val publishedAt: String = "",
    val htmlUrl: String = "",
    val minVersion: String? = null,
    val forceUpdate: Boolean = false,
    val forceUpdateMessage: String? = null,
)

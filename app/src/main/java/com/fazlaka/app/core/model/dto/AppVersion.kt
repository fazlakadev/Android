package com.fazlaka.app.core.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class AppVersionResponse(
    val success: Boolean,
    val data: AppVersionData,
)

@Serializable
data class AppVersionData(
    val version: String,
    val tagName: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val publishedAt: String,
    val htmlUrl: String,
)

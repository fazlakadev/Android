package com.fazlaka.app.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "local_progress")
data class LocalProgressEntity(
    @PrimaryKey val episodeId: String,
    val positionSeconds: Int = 0,
    val durationSeconds: Int? = null,
    val title: String? = null,
    val coverImage: String? = null,
    val seasonId: String? = null,
    val seasonTitle: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "drafts")
data class MessageDraftEntity(
    @PrimaryKey val conversationId: String,
    val draft: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "pending_actions")
data class PendingActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val method: String,
    val endpoint: String,
    val body: String? = null,
    val contentType: String? = null,
    val contentId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
)

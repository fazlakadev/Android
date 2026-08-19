package com.fazlaka.app.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY createdAt DESC LIMIT :limit")
    fun recent(limit: Int = 10): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun delete(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clear()
}

@Dao
interface LocalProgressDao {
    @Query("SELECT * FROM local_progress ORDER BY updatedAt DESC")
    fun all(): Flow<List<LocalProgressEntity>>

    @Query("SELECT * FROM local_progress WHERE episodeId = :episodeId")
    fun forEpisode(episodeId: String): Flow<LocalProgressEntity?>

    @Query("SELECT * FROM local_progress WHERE episodeId = :episodeId")
    suspend fun getSuspend(episodeId: String): LocalProgressEntity?

    @Upsert
    suspend fun upsert(entity: LocalProgressEntity)

    @Delete
    suspend fun delete(entity: LocalProgressEntity)

    @Query("DELETE FROM local_progress WHERE episodeId = :episodeId")
    suspend fun deleteById(episodeId: String)

    @Query("DELETE FROM local_progress")
    suspend fun clear()
}

@Dao
interface MessageDraftDao {
    @Query("SELECT * FROM drafts WHERE conversationId = :conversationId")
    suspend fun get(conversationId: String): MessageDraftEntity?

    @Upsert
    suspend fun save(entity: MessageDraftEntity)

    @Query("DELETE FROM drafts WHERE conversationId = :conversationId")
    suspend fun delete(conversationId: String)
}

@Dao
interface PendingActionDao {
    @Insert
    suspend fun insert(action: PendingActionEntity): Long

    @Query("SELECT * FROM pending_actions ORDER BY createdAt ASC")
    suspend fun getAll(): List<PendingActionEntity>

    @Query("SELECT COUNT(*) FROM pending_actions")
    fun count(): Flow<Int>

    @Query("DELETE FROM pending_actions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE pending_actions SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetry(id: Long)

    @Query("DELETE FROM pending_actions")
    suspend fun clear()
}

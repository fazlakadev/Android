package com.fazlaka.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SearchHistoryEntity::class,
        LocalProgressEntity::class,
        MessageDraftEntity::class,
        PendingActionEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class FazlakaDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun localProgressDao(): LocalProgressDao
    abstract fun messageDraftDao(): MessageDraftDao
    abstract fun pendingActionDao(): PendingActionDao
}

package com.fazlaka.app.core.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FazlakaDatabase =
        Room.databaseBuilder(context, FazlakaDatabase::class.java, "fazlaka.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSearchHistoryDao(db: FazlakaDatabase) = db.searchHistoryDao()

    @Provides
    fun provideLocalProgressDao(db: FazlakaDatabase) = db.localProgressDao()

    @Provides
    fun provideMessageDraftDao(db: FazlakaDatabase) = db.messageDraftDao()

    @Provides
    fun providePendingActionDao(db: FazlakaDatabase) = db.pendingActionDao()
}

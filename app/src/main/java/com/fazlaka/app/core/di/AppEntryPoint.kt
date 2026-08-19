package com.fazlaka.app.core.di

import com.fazlaka.app.core.datastore.SessionManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {
    fun sessionManager(): SessionManager
}

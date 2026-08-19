package com.fazlaka.app.core.network

import android.content.Context
import com.fazlaka.app.BuildConfig
import com.fazlaka.app.core.datastore.SessionManager
import com.fazlaka.app.data.repository.AuthRepository
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    fun provideAuthRepository(
        api: ApiService,
        sessionManager: SessionManager,
        json: Json,
    ): AuthRepository =
        AuthRepository(api, sessionManager, json)

    @Provides
    @Singleton
    fun provideRefreshApi(json: Json): RefreshApi {
        val client = OkHttpClient.Builder()
            // The refresh rotation re-issues the session row, so it MUST carry
            // the same device identity headers as every other app request.
            .addInterceptor(PlatformHeadersInterceptor())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(RefreshApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: android.content.Context,
        sessionManager: SessionManager,
        tokenAuthenticator: TokenAuthenticator,
        logging: HttpLoggingInterceptor,
        locationProvider: com.fazlaka.app.core.location.LocationProvider,
        networkMonitor: NetworkMonitor,
    ): OkHttpClient {
        val dispatcher = okhttp3.Dispatcher().apply {
            maxRequests = 64
            maxRequestsPerHost = 10
        }
        val connectionPool = okhttp3.ConnectionPool(
            maxIdleConnections = 10,
            keepAliveDuration = 5,
            timeUnit = java.util.concurrent.TimeUnit.MINUTES,
        )
        return OkHttpClient.Builder()
            .cache(HttpCache.create(context))
            .addInterceptor(AuthInterceptor(sessionManager, locationProvider))
            .addInterceptor(OfflineCacheInterceptor(networkMonitor))
            .addNetworkInterceptor(CacheInterceptor())
            .authenticator(tokenAuthenticator)
            .addInterceptor(logging)
            .dispatcher(dispatcher)
            .connectionPool(connectionPool)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        json: Json,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)
}

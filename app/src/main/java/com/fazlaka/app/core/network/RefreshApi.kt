package com.fazlaka.app.core.network

import com.fazlaka.app.core.model.dto.ApiEnvelope
import com.fazlaka.app.core.model.dto.TokenPairDto
import retrofit2.http.Body
import retrofit2.http.POST

interface RefreshApi {
    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): ApiEnvelope<TokenPairDto>
}

package com.fazlaka.app.core.network

import kotlinx.serialization.Serializable

/** Result wrapper for all API calls. */
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Failure(
        val code: Int,
        val message: String?,
        val errors: Map<String, List<String>>? = null,
    ) : ApiResult<Nothing>
}

class ApiException(
    val code: Int,
    override val message: String?,
    val errors: Map<String, List<String>>? = null,
) : Exception(message)

@Serializable
data class RefreshRequest(
    val refreshToken: String,
)

package com.fazlaka.app.core.network

import com.fazlaka.app.core.model.dto.ApiEnvelope
import com.fazlaka.app.core.model.dto.ApiErrorBody
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

suspend fun <T> safeApiCall(
    call: suspend () -> ApiEnvelope<T>,
    json: Json,
): ApiResult<T> {
    return try {
        val envelope = call()
        @Suppress("UNCHECKED_CAST")
        ApiResult.Success(envelope.data as T)
    } catch (e: HttpException) {
        val body = e.response()?.errorBody()?.string()
        val parsed = body?.let { runCatching { json.decodeFromString<ApiErrorBody>(it) }.getOrNull() }
        ApiResult.Failure(
            code = e.code(),
            message = parsed?.message ?: parsed?.error ?: e.message(),
            errors = parsed?.errors,
        )
    } catch (e: SocketTimeoutException) {
        ApiResult.Failure(0, "network.timeout")
    } catch (e: IOException) {
        ApiResult.Failure(0, "network.error")
    } catch (e: kotlinx.serialization.SerializationException) {
        ApiResult.Failure(-1, "network.parseError")
    } catch (e: Exception) {
        ApiResult.Failure(-2, e.message)
    }
}

@Suppress("unused")
fun errorBodyToString(body: ResponseBody?): String? =
    body?.string()

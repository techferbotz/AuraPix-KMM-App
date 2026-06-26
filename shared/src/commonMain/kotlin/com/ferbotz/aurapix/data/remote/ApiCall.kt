package com.ferbotz.aurapix.data.remote

import com.ferbotz.aurapix.data.remote.dto.ApiResponse
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse

/**
 * Parses the `{ success, data, errorCode, message }` envelope.
 * Throws [ApiError] on `success=false`.
 */
suspend inline fun <reified T> HttpResponse.unwrap(): T {
    val envelope = body<ApiResponse<T>>()
    if (envelope.success && envelope.data != null) return envelope.data
    val code = envelope.errorCode ?: "INTERNAL_ERROR"
    val msg  = envelope.message  ?: "Unknown error"
    throw code.toApiError(msg)
}

/**
 * Executes [block], converts any [ApiError] or network exception into a failed [Result].
 */
suspend inline fun <reified T> safeApiCall(block: suspend () -> HttpResponse): Result<T> =
    try {
        Result.success(block().unwrap())
    } catch (e: ApiError) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(ApiError.NetworkError(e))
    }

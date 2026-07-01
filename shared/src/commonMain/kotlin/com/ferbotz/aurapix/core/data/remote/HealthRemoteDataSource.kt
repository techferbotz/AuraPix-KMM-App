package com.ferbotz.aurapix.core.data.remote

import com.ferbotz.aurapix.core.data.remote.dto.HealthDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse

class HealthRemoteDataSource(private val client: HttpClient) {

    /** §4.15 — connectivity check. Unversioned and NOT wrapped in the success/data envelope. */
    suspend fun health(): Result<HealthDto> =
        try {
            val response: HttpResponse = client.get { url(AURA_HEALTH_URL) }
            Result.success(response.body<HealthDto>())
        } catch (e: Exception) {
            Result.failure(ApiError.NetworkError(e))
        }
}

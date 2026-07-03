package com.ferbotz.aurapix.creation.data

import com.ferbotz.aurapix.core.data.remote.dto.PagedResponse
import com.ferbotz.aurapix.core.data.remote.safeApiCall
import com.ferbotz.aurapix.creation.data.dto.CreationDetailDto
import com.ferbotz.aurapix.creation.data.dto.CreationDto
import com.ferbotz.aurapix.creation.data.dto.GenerationStartDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

class CreationRemoteDataSource(private val client: HttpClient) {

    /** §4.10 — start a generation (multipart: templateId + one file per image slot). */
    suspend fun generate(templateId: String, images: List<ByteArray>): Result<GenerationStartDto> =
        safeApiCall {
            client.post("generate") {
                setBody(MultiPartFormDataContent(formData {
                    append("templateId", templateId)
                    images.forEach { bytes ->
                        append("images", bytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=image.jpg")
                        })
                    }
                }))
            }
        }

    /** §4.11 — paginated creation history (newest first). */
    suspend fun getCreations(page: Int = 1, limit: Int = 20): Result<PagedResponse<CreationDto>> =
        safeApiCall { client.get("creations") { parameter("page", page); parameter("limit", limit) } }

    /**
     * §4.12 — creation detail. `wait=true` long-polls server-side, so this call gets a 2-minute
     * socket + request timeout (overriding the client's 30s default) so a slow generation isn't
     * cut off by socket inactivity. The non-wait call keeps the default timeouts.
     */
    suspend fun getCreation(creationId: String, wait: Boolean = false): Result<CreationDetailDto> =
        safeApiCall {
            client.get("creations/$creationId") {
                if (wait) {
                    parameter("wait", true)
                    timeout {
                        requestTimeoutMillis = 120_000
                        socketTimeoutMillis = 120_000
                    }
                }
            }
        }
}

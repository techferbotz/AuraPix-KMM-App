package com.ferbotz.aurapix.data.remote

import com.ferbotz.aurapix.data.remote.dto.AuthResponseDto
import com.ferbotz.aurapix.data.remote.dto.CategorySummaryDto
import com.ferbotz.aurapix.data.remote.dto.CreditsDto
import com.ferbotz.aurapix.data.remote.dto.CreationDetailDto
import com.ferbotz.aurapix.data.remote.dto.CreationDto
import com.ferbotz.aurapix.data.remote.dto.GenerationStartDto
import com.ferbotz.aurapix.data.remote.dto.HealthDto
import com.ferbotz.aurapix.data.remote.dto.PagedResponse
import com.ferbotz.aurapix.data.remote.dto.ProfileDto
import com.ferbotz.aurapix.data.remote.dto.PurchaseDto
import com.ferbotz.aurapix.data.remote.dto.SubscriptionDetailDto
import com.ferbotz.aurapix.data.remote.dto.TemplateDetailDto
import com.ferbotz.aurapix.data.remote.dto.TemplateSummaryDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData

class AuraApi(private val client: HttpClient) {

    // ── Auth ──────────────────────────────────────────────────────────────────

    suspend fun googleAuth(idToken: String): Result<AuthResponseDto> =
        safeApiCall { client.post("auth/google") { contentType(ContentType.Application.Json); setBody(mapOf("idToken" to idToken)) } }

    // ── Profile ───────────────────────────────────────────────────────────────

    suspend fun getProfile(): Result<ProfileDto> =
        safeApiCall { client.get("profile") }

    suspend fun getCredits(): Result<CreditsDto> =
        safeApiCall { client.get("credits") }

    // ── Feed: see FeedRemoteDataSource ──────────────────────────────────────────

    // ── Categories ────────────────────────────────────────────────────────────

    suspend fun getCategories(): Result<List<CategorySummaryDto>> =
        safeApiCall { client.get("categories") }

    suspend fun getCategory(categoryId: String): Result<CategorySummaryDto> =
        safeApiCall { client.get("categories/$categoryId") }

    suspend fun getCategoryTemplates(categoryId: String, page: Int = 1, limit: Int = 20): Result<PagedResponse<TemplateSummaryDto>> =
        safeApiCall { client.get("categories/$categoryId/templates") { parameter("page", page); parameter("limit", limit) } }

    // ── Templates ─────────────────────────────────────────────────────────────

    suspend fun getTemplate(templateId: String): Result<TemplateDetailDto> =
        safeApiCall { client.get("templates/$templateId") }

    // ── Generate ──────────────────────────────────────────────────────────────

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

    // ── Creations ─────────────────────────────────────────────────────────────

    suspend fun getCreations(page: Int = 1, limit: Int = 20): Result<PagedResponse<CreationDto>> =
        safeApiCall { client.get("creations") { parameter("page", page); parameter("limit", limit) } }

    suspend fun getCreation(creationId: String, wait: Boolean = false): Result<CreationDetailDto> =
        safeApiCall {
            client.get("creations/$creationId") {
                if (wait) {
                    parameter("wait", true)
                    timeout { requestTimeoutMillis = 90_000 }
                }
            }
        }

    // ── Subscriptions ─────────────────────────────────────────────────────────

    suspend fun getSubscription(): Result<SubscriptionDetailDto> =
        safeApiCall { client.get("subscriptions") }

    // ── Purchases ─────────────────────────────────────────────────────────────

    suspend fun getPurchases(page: Int = 1, limit: Int = 20): Result<PagedResponse<PurchaseDto>> =
        safeApiCall { client.get("purchases") { parameter("page", page); parameter("limit", limit) } }

    // ── Health ────────────────────────────────────────────────────────────────

    suspend fun health(): Result<HealthDto> =
        try {
            val response: HttpResponse = client.get { url(AURA_HEALTH_URL) }
            Result.success(response.body<HealthDto>())
        } catch (e: Exception) {
            Result.failure(ApiError.NetworkError(e))
        }
}

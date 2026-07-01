package com.ferbotz.aurapix.template.data

import com.ferbotz.aurapix.template.data.dto.TemplateDetailDto
import com.ferbotz.aurapix.core.data.remote.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get

/**
 * Remote data source for a single template's detail (§4.9). Owns the template HTTP endpoint
 * over the shared [HttpClient] + `safeApiCall`; the repository turns the DTO into
 * [com.ferbotz.aurapix.core.data.DataState].
 */
class TemplateRemoteDataSource(private val client: HttpClient) {

    /** §4.9 — full template detail (preview images, image slots, categories). */
    suspend fun getTemplate(templateId: String): Result<TemplateDetailDto> =
        safeApiCall { client.get("templates/$templateId") }
}

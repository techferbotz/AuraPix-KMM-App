package com.ferbotz.aurapix.data.repository

import com.ferbotz.aurapix.data.remote.AuraApi
import com.ferbotz.aurapix.data.remote.dto.TemplateDetailDto

class TemplatesRepository(private val api: AuraApi) {
    suspend fun getTemplate(id: String): Result<TemplateDetailDto> = api.getTemplate(id)
}

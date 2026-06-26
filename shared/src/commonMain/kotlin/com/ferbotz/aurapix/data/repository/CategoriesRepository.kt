package com.ferbotz.aurapix.data.repository

import com.ferbotz.aurapix.data.remote.AuraApi
import com.ferbotz.aurapix.data.remote.dto.CategorySummaryDto
import com.ferbotz.aurapix.data.remote.dto.PagedResponse
import com.ferbotz.aurapix.data.remote.dto.TemplateSummaryDto

class CategoriesRepository(private val api: AuraApi) {
    suspend fun getCategories(): Result<List<CategorySummaryDto>> = api.getCategories()
    suspend fun getCategory(id: String): Result<CategorySummaryDto> = api.getCategory(id)
    suspend fun getCategoryTemplates(id: String, page: Int = 1): Result<PagedResponse<TemplateSummaryDto>> =
        api.getCategoryTemplates(id, page)
}

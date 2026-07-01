package com.ferbotz.aurapix.category.data

import com.ferbotz.aurapix.core.data.remote.dto.CategorySummaryDto
import com.ferbotz.aurapix.core.data.remote.dto.PagedResponse
import com.ferbotz.aurapix.core.data.remote.dto.TemplateSummaryDto
import com.ferbotz.aurapix.core.data.remote.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class CategoryRemoteDataSource(private val client: HttpClient) {

    /** §4.6 — all categories (ordered by displayOrder, not paginated). */
    suspend fun getCategories(): Result<List<CategorySummaryDto>> =
        safeApiCall { client.get("categories") }

    /** §4.7 — a category header. */
    suspend fun getCategory(categoryId: String): Result<CategorySummaryDto> =
        safeApiCall { client.get("categories/$categoryId") }

    /** §4.8 — paginated templates within a category. */
    suspend fun getCategoryTemplates(categoryId: String, page: Int = 1, limit: Int = 20): Result<PagedResponse<TemplateSummaryDto>> =
        safeApiCall { client.get("categories/$categoryId/templates") { parameter("page", page); parameter("limit", limit) } }
}

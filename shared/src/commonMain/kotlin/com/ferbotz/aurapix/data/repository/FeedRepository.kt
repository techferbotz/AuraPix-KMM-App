package com.ferbotz.aurapix.data.repository

import com.ferbotz.aurapix.data.remote.AuraApi
import com.ferbotz.aurapix.data.remote.dto.CategorySummaryDto
import com.ferbotz.aurapix.data.remote.dto.FeedResponseDto
import com.ferbotz.aurapix.data.remote.dto.PagedResponse
import com.ferbotz.aurapix.data.remote.dto.TemplateSummaryDto

class FeedRepository(private val api: AuraApi) {
    suspend fun getFeed(): Result<FeedResponseDto> = api.getFeed()

    suspend fun getTrayTemplates(trayId: String, page: Int = 1): Result<PagedResponse<TemplateSummaryDto>> =
        api.getTrayTemplates(trayId, page)

    suspend fun getTrayCategories(trayId: String, page: Int = 1): Result<PagedResponse<CategorySummaryDto>> =
        api.getTrayCategories(trayId, page)
}

package com.ferbotz.aurapix.feed.data

import com.ferbotz.aurapix.core.data.remote.dto.CategorySummaryDto
import com.ferbotz.aurapix.feed.data.dto.FeedResponseDto
import com.ferbotz.aurapix.core.data.remote.dto.PagedResponse
import com.ferbotz.aurapix.core.data.remote.dto.TemplateSummaryDto
import com.ferbotz.aurapix.core.data.remote.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Remote data source for the home feed. Owns the feed HTTP endpoints (over the shared
 * [HttpClient] + `safeApiCall`) and returns raw DTOs as [Result]; the repository turns these
 * into processed models + [com.ferbotz.aurapix.core.data.DataState].
 */
class FeedRemoteDataSource(private val client: HttpClient) {

    /** §4.4 — the whole home feed (trays in display order). */
    suspend fun getFeed(): Result<FeedResponseDto> =
        safeApiCall { client.get("feed") }

    /** §4.5 — a TEMPLATE tray's paginated "See All" list. */
    suspend fun getTrayTemplates(trayId: String, page: Int = 1, limit: Int = 20): Result<PagedResponse<TemplateSummaryDto>> =
        safeApiCall { client.get("feed/trays/$trayId") { parameter("page", page); parameter("limit", limit) } }

    /** §4.5 — a CATEGORY tray's paginated "See All" list. */
    suspend fun getTrayCategories(trayId: String, page: Int = 1, limit: Int = 20): Result<PagedResponse<CategorySummaryDto>> =
        safeApiCall { client.get("feed/trays/$trayId") { parameter("page", page); parameter("limit", limit) } }
}

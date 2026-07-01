package com.ferbotz.aurapix.billing.data

import com.ferbotz.aurapix.billing.data.dto.PurchaseDto
import com.ferbotz.aurapix.billing.data.dto.SubscriptionDetailDto
import com.ferbotz.aurapix.core.data.remote.dto.PagedResponse
import com.ferbotz.aurapix.core.data.remote.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class SubscriptionRemoteDataSource(private val client: HttpClient) {

    /** §4.13 — current subscription (status NONE when never subscribed). */
    suspend fun getSubscription(): Result<SubscriptionDetailDto> =
        safeApiCall { client.get("subscriptions") }

    /** §4.14 — paginated purchase history. */
    suspend fun getPurchases(page: Int = 1, limit: Int = 20): Result<PagedResponse<PurchaseDto>> =
        safeApiCall { client.get("purchases") { parameter("page", page); parameter("limit", limit) } }
}

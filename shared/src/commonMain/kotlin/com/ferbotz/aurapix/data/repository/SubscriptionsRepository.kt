package com.ferbotz.aurapix.data.repository

import com.ferbotz.aurapix.data.remote.AuraApi
import com.ferbotz.aurapix.data.remote.dto.PagedResponse
import com.ferbotz.aurapix.data.remote.dto.PurchaseDto
import com.ferbotz.aurapix.data.remote.dto.SubscriptionDetailDto

class SubscriptionsRepository(private val api: AuraApi) {
    suspend fun getSubscription(): Result<SubscriptionDetailDto> = api.getSubscription()
    suspend fun getPurchases(page: Int = 1): Result<PagedResponse<PurchaseDto>> = api.getPurchases(page)
}

package com.ferbotz.aurapix.data.repository

import com.ferbotz.aurapix.data.common.DataState
import com.ferbotz.aurapix.data.remote.AuraApi
import com.ferbotz.aurapix.data.remote.asApiError
import com.ferbotz.aurapix.data.remote.dto.PagedResponse
import com.ferbotz.aurapix.data.remote.dto.PurchaseDto
import com.ferbotz.aurapix.data.remote.dto.SubscriptionDetailDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SubscriptionsRepository(private val api: AuraApi) {

    fun getSubscription(): Flow<DataState<SubscriptionDetailDto>> = flow {
        emit(DataState.Loading)
        api.getSubscription().fold(
            onSuccess = { emit(DataState.Success(it)) },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)

    fun getPurchases(page: Int = 1): Flow<DataState<PagedResponse<PurchaseDto>>> = flow {
        emit(DataState.Loading)
        api.getPurchases(page).fold(
            onSuccess = { emit(DataState.Success(it)) },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)
}

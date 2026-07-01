package com.ferbotz.aurapix.billing.data

import com.ferbotz.aurapix.billing.data.dto.PurchaseDto
import com.ferbotz.aurapix.billing.data.dto.SubscriptionDetailDto
import com.ferbotz.aurapix.core.data.DataState
import com.ferbotz.aurapix.core.data.remote.asApiError
import com.ferbotz.aurapix.core.data.remote.dto.PagedResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SubscriptionsRepository(private val remote: SubscriptionRemoteDataSource) {

    fun getSubscription(): Flow<DataState<SubscriptionDetailDto>> = flow {
        emit(DataState.Loading)
        remote.getSubscription().fold(
            onSuccess = { emit(DataState.Success(it)) },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)

    fun getPurchases(page: Int = 1): Flow<DataState<PagedResponse<PurchaseDto>>> = flow {
        emit(DataState.Loading)
        remote.getPurchases(page).fold(
            onSuccess = { emit(DataState.Success(it)) },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)
}

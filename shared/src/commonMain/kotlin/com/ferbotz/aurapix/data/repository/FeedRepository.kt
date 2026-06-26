package com.ferbotz.aurapix.data.repository

import com.ferbotz.aurapix.data.common.DataState
import com.ferbotz.aurapix.data.model.FeedTray
import com.ferbotz.aurapix.data.model.toFeedTray
import com.ferbotz.aurapix.data.remote.asApiError
import com.ferbotz.aurapix.data.remote.auraJson
import com.ferbotz.aurapix.data.remote.datasource.FeedRemoteDataSource
import com.ferbotz.aurapix.data.remote.dto.CategorySummaryDto
import com.ferbotz.aurapix.data.remote.dto.PagedResponse
import com.ferbotz.aurapix.data.remote.dto.TemplateSummaryDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Feed repository: pulls raw DTOs from [FeedRemoteDataSource], **processes** them
 * (decodes each tray's polymorphic items into typed [FeedTray]s, sorts by display order)
 * and **emits** the result as [DataState].
 */
class FeedRepository(private val remote: FeedRemoteDataSource) {

    fun getFeed(): Flow<DataState<List<FeedTray>>> = flow {
        emit(DataState.Loading)
        remote.getFeed().fold(
            onSuccess = { response ->
                val trays = response.trays
                    .map { it.toFeedTray(auraJson) }
                    .sortedBy { it.displayOrder }
                emit(DataState.Success(trays))
            },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)

    fun getTrayTemplates(trayId: String, page: Int = 1): Flow<DataState<PagedResponse<TemplateSummaryDto>>> = flow {
        emit(DataState.Loading)
        remote.getTrayTemplates(trayId, page).fold(
            onSuccess = { emit(DataState.Success(it)) },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)

    fun getTrayCategories(trayId: String, page: Int = 1): Flow<DataState<PagedResponse<CategorySummaryDto>>> = flow {
        emit(DataState.Loading)
        remote.getTrayCategories(trayId, page).fold(
            onSuccess = { emit(DataState.Success(it)) },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)
}

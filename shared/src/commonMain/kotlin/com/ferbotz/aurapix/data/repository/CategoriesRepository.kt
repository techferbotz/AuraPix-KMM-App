package com.ferbotz.aurapix.data.repository

import com.ferbotz.aurapix.data.common.DataState
import com.ferbotz.aurapix.data.remote.AuraApi
import com.ferbotz.aurapix.data.remote.asApiError
import com.ferbotz.aurapix.data.remote.dto.CategorySummaryDto
import com.ferbotz.aurapix.data.remote.dto.PagedResponse
import com.ferbotz.aurapix.data.remote.dto.TemplateSummaryDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class CategoriesRepository(private val api: AuraApi) {

    fun getCategories(): Flow<DataState<List<CategorySummaryDto>>> = flow {
        emit(DataState.Loading)
        api.getCategories().fold(
            onSuccess = { emit(DataState.Success(it.sortedBy { c -> c.displayOrder })) },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)

    fun getCategory(id: String): Flow<DataState<CategorySummaryDto>> = flow {
        emit(DataState.Loading)
        api.getCategory(id).fold(
            onSuccess = { emit(DataState.Success(it)) },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)

    fun getCategoryTemplates(id: String, page: Int = 1): Flow<DataState<PagedResponse<TemplateSummaryDto>>> = flow {
        emit(DataState.Loading)
        api.getCategoryTemplates(id, page).fold(
            onSuccess = { emit(DataState.Success(it)) },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)
}

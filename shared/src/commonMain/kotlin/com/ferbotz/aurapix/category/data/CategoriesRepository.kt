package com.ferbotz.aurapix.category.data

import com.ferbotz.aurapix.core.data.DataState
import com.ferbotz.aurapix.core.data.remote.asApiError
import com.ferbotz.aurapix.core.data.remote.dto.CategorySummaryDto
import com.ferbotz.aurapix.core.data.remote.dto.PagedResponse
import com.ferbotz.aurapix.core.data.remote.dto.TemplateSummaryDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class CategoriesRepository(private val remote: CategoryRemoteDataSource) {

    fun getCategories(): Flow<DataState<List<CategorySummaryDto>>> = flow {
        emit(DataState.Loading)
        remote.getCategories().fold(
            onSuccess = { emit(DataState.Success(it.sortedBy { c -> c.displayOrder })) },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)

    fun getCategory(id: String): Flow<DataState<CategorySummaryDto>> = flow {
        emit(DataState.Loading)
        remote.getCategory(id).fold(
            onSuccess = { emit(DataState.Success(it)) },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)

    fun getCategoryTemplates(id: String, page: Int = 1): Flow<DataState<PagedResponse<TemplateSummaryDto>>> = flow {
        emit(DataState.Loading)
        remote.getCategoryTemplates(id, page).fold(
            onSuccess = { emit(DataState.Success(it)) },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)
}

package com.ferbotz.aurapix.template.data

import com.ferbotz.aurapix.core.data.DataState
import com.ferbotz.aurapix.core.data.remote.asApiError
import com.ferbotz.aurapix.template.data.dto.TemplateDetailDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class TemplatesRepository(private val remote: TemplateRemoteDataSource) {

    fun getTemplate(id: String): Flow<DataState<TemplateDetailDto>> = flow {
        emit(DataState.Loading)
        remote.getTemplate(id).fold(
            onSuccess = { emit(DataState.Success(it)) },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)
}

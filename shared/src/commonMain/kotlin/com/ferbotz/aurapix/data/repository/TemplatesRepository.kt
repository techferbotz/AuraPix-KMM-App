package com.ferbotz.aurapix.data.repository

import com.ferbotz.aurapix.data.common.DataState
import com.ferbotz.aurapix.data.remote.AuraApi
import com.ferbotz.aurapix.data.remote.asApiError
import com.ferbotz.aurapix.data.remote.dto.TemplateDetailDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class TemplatesRepository(private val api: AuraApi) {

    fun getTemplate(id: String): Flow<DataState<TemplateDetailDto>> = flow {
        emit(DataState.Loading)
        api.getTemplate(id).fold(
            onSuccess = { emit(DataState.Success(it)) },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)
}

package com.ferbotz.aurapix.template.data

import com.ferbotz.aurapix.core.data.DataState
import com.ferbotz.aurapix.core.data.remote.asApiError
import com.ferbotz.aurapix.template.data.dto.TemplateDetailDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class TemplatesRepository(private val remote: TemplateRemoteDataSource) {

    /**
     * A template by [ref]: its id, or — when the screen was opened from a link — possibly its
     * slug, since `https://aurapix.ferbotz.com/template/…` is shared in both forms (§4.9a). Every
     * id is a UUID (§2), so anything else is looked up as a slug.
     */
    fun getTemplate(ref: String): Flow<DataState<TemplateDetailDto>> = flow {
        emit(DataState.Loading)
        val result = if (UUID.matches(ref)) remote.getTemplate(ref) else remote.getTemplateBySlug(ref)
        result.fold(
            onSuccess = { emit(DataState.Success(it)) },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)

    private companion object {
        val UUID = Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
    }
}

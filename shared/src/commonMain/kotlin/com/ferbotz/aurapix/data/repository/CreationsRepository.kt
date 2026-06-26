package com.ferbotz.aurapix.data.repository

import com.ferbotz.aurapix.data.local.CreationDao
import com.ferbotz.aurapix.data.local.CreationEntity
import com.ferbotz.aurapix.data.remote.AuraApi
import com.ferbotz.aurapix.data.remote.dto.CreationDetailDto
import com.ferbotz.aurapix.data.remote.dto.CreationDto
import com.ferbotz.aurapix.data.remote.dto.GenerationStartDto
import com.ferbotz.aurapix.data.remote.dto.PagedResponse
import kotlinx.coroutines.flow.Flow

class CreationsRepository(
    private val api: AuraApi,
    private val dao: CreationDao,
) {
    fun observeCreations(): Flow<List<CreationEntity>> = dao.observeAll()

    suspend fun refreshCreations(page: Int = 1): Result<PagedResponse<CreationDto>> {
        val result = api.getCreations(page)
        result.onSuccess { paged ->
            if (page == 1) dao.clear()
            dao.upsertAll(paged.items.map { it.toEntity() })
        }
        return result
    }

    suspend fun generate(templateId: String, images: List<ByteArray>): Result<GenerationStartDto> =
        api.generate(templateId, images)

    suspend fun pollCreation(creationId: String): Result<CreationDetailDto> {
        val result = api.getCreation(creationId, wait = true)
        result.onSuccess { detail ->
            dao.upsertAll(listOf(detail.toEntity()))
        }
        return result
    }

    suspend fun getCreation(creationId: String): Result<CreationDetailDto> =
        api.getCreation(creationId, wait = false)

    suspend fun deleteCreation(id: String) = dao.deleteById(id)
}

private fun CreationDto.toEntity() = CreationEntity(
    id = id,
    status = status,
    generatedImageUrl = generatedImageUrl,
    templateTitleSnapshot = templateTitleSnapshot,
    templateThumbnailSnapshot = templateThumbnailSnapshot,
    createdAt = createdAt,
    templateId = "",
    failureReason = null,
)

private fun CreationDetailDto.toEntity() = CreationEntity(
    id = id,
    status = status,
    generatedImageUrl = generatedImageUrl,
    templateTitleSnapshot = templateTitleSnapshot,
    templateThumbnailSnapshot = templateThumbnailSnapshot,
    createdAt = createdAt,
    templateId = templateId,
    failureReason = failureReason,
)

package com.ferbotz.aurapix.creation.data

import com.ferbotz.aurapix.core.data.DataState
import com.ferbotz.aurapix.core.data.remote.asApiError
import com.ferbotz.aurapix.creation.data.dto.CreationDetailDto
import com.ferbotz.aurapix.creation.data.dto.CreationDto
import com.ferbotz.aurapix.creation.data.local.CreationDao
import com.ferbotz.aurapix.creation.data.local.CreationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * Offline-first creations + the generate/poll flow.
 *
 * [observeCreations] is the source of truth: it mirrors the Room cache (so the screen updates
 * live as rows change) and kicks off a network refresh, surfacing an error only when there is
 * nothing cached to show.
 */
class CreationsRepository(
    private val remote: CreationRemoteDataSource,
    private val dao: CreationDao,
) {
    fun observeCreations(): Flow<DataState<List<CreationEntity>>> = channelFlow {
        send(DataState.Loading)
        // Mirror the local cache — keeps emitting as the DB changes (refresh, poll updates).
        launch {
            dao.observeAll().collect { send(DataState.Success(it)) }
        }
        // Refresh from network; only surface the error if we have no cache to fall back on.
        remote.getCreations().fold(
            onSuccess = { paged ->
                dao.clear()
                dao.upsertAll(paged.items.map { it.toEntity() })
            },
            onFailure = { if (dao.count() == 0) send(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)

    /**
     * Starts a generation, then long-polls the creation until COMPLETED/FAILED, caching each
     * snapshot. [onStarted] fires with the new creation id the instant `/generate` succeeds — the
     * ViewModel keeps it so a failed poll can be retried against the SAME creation (via
     * [pollCreation]) instead of generating a second image. Emits the terminal creation as Success.
     */
    fun generateAndPoll(
        templateId: String,
        images: List<ByteArray>,
        onStarted: (creationId: String) -> Unit,
    ): Flow<DataState<CreationDetailDto>> = flow {
        emit(DataState.Loading)
        val start = remote.generate(templateId, images).fold(
            onSuccess = { it },
            onFailure = { emit(DataState.Error(it.asApiError())); null },
        ) ?: return@flow
        onStarted(start.creationId)
        emitAll(pollLoop(start.creationId))
    }.flowOn(Dispatchers.Default)

    /**
     * Long-polls an existing creation (`wait=true`) until COMPLETED/FAILED. Used by "retry": a
     * network / socket-timeout failure re-polls the same creation and never starts a new generation.
     */
    fun pollCreation(creationId: String): Flow<DataState<CreationDetailDto>> = flow {
        emit(DataState.Loading)
        emitAll(pollLoop(creationId))
    }.flowOn(Dispatchers.Default)

    /** Shared long-poll loop: caches + emits each terminal snapshot as Success, or the failure as Error. */
    private fun pollLoop(creationId: String): Flow<DataState<CreationDetailDto>> = flow {
        while (true) {
            val keepPolling = remote.getCreation(creationId, wait = true).fold(
                onSuccess = { detail ->
                    dao.upsertAll(listOf(detail.toEntity()))
                    when (detail.status) {
                        "COMPLETED", "FAILED" -> {
                            emit(DataState.Success(detail))
                            false
                        }
                        else -> true // still PROCESSING — long-poll returned early, keep going
                    }
                },
                onFailure = {
                    emit(DataState.Error(it.asApiError()))
                    false
                },
            )
            if (!keepPolling) return@flow
        }
    }

    /** One-shot creation detail (no long-poll) — used by the result/detail screen. */
    fun getCreation(creationId: String): Flow<DataState<CreationDetailDto>> = flow {
        emit(DataState.Loading)
        remote.getCreation(creationId, wait = false).fold(
            onSuccess = { emit(DataState.Success(it)) },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)

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

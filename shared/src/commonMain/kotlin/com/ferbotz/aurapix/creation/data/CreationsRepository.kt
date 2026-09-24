package com.ferbotz.aurapix.creation.data

import com.ferbotz.aurapix.core.data.DataState
import com.ferbotz.aurapix.core.data.remote.asApiError
import com.ferbotz.aurapix.creation.data.dto.CreationDetailDto
import com.ferbotz.aurapix.creation.data.dto.CreationDto
import com.ferbotz.aurapix.creation.data.local.CreationDao
import com.ferbotz.aurapix.creation.data.local.CreationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Offline-first creations + the generate/poll flow.
 *
 * The Room cache is the source of truth for My Creations: [observeCreations] mirrors it, so the
 * screen updates live as rows change, and [loadCreationsPage] fills it from the network a page at
 * a time.
 */
class CreationsRepository(
    private val remote: CreationRemoteDataSource,
    private val dao: CreationDao,
) {
    /** Every cached creation, newest first — all the pages loaded so far. */
    fun observeCreations(): Flow<List<CreationEntity>> = dao.observeAll()

    /**
     * Loads one page of `GET /creations` (§4.11) into the cache and says whether another follows.
     *
     * Page 1 replaces the cache only when the two disagree — rows from another account, or a
     * creation the server no longer has. When they agree, rows from later pages stay put, so
     * coming back to the list doesn't shrink it to one page under the user's thumb. The last page
     * also drops anything cached beyond it: past the end, the server has nothing.
     */
    suspend fun loadCreationsPage(page: Int): Result<Boolean> = withContext(Dispatchers.Default) {
        remote.getCreations(page).map { paged ->
            val rows = paged.items.map { it.toEntity() }
            if (page == 1 && (rows.isEmpty() || dao.newestIds(rows.size) != rows.map { it.id })) dao.clear()
            dao.upsertAll(rows)
            if (!paged.hasMore) rows.lastOrNull()?.let { dao.deleteOlderThan(it.createdAt) }
            paged.hasMore
        }
    }

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

    /** Drops every cached creation — for when the account that owned them has been deleted. */
    suspend fun clearCache() = dao.clear()
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

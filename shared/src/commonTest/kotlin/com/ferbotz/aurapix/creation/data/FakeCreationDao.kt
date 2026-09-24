package com.ferbotz.aurapix.creation.data

import com.ferbotz.aurapix.creation.data.local.CreationDao
import com.ferbotz.aurapix.creation.data.local.CreationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** An in-memory [CreationDao] with Room's ordering, so repository logic runs without a database. */
class FakeCreationDao(vararg initial: CreationEntity) : CreationDao {

    private val rows = MutableStateFlow(initial.associateBy { it.id })

    /** The cached ids, newest first — what My Creations would show. */
    val ids: List<String> get() = newestFirst(rows.value).map { it.id }

    override fun observeAll(): Flow<List<CreationEntity>> = rows.map { newestFirst(it) }

    override suspend fun count(): Int = rows.value.size

    override suspend fun newestIds(limit: Int): List<String> = ids.take(limit)

    override suspend fun upsertAll(items: List<CreationEntity>) = rows.update { it + items.associateBy { row -> row.id } }

    override suspend fun deleteById(id: String) = rows.update { it - id }

    override suspend fun deleteOlderThan(createdAt: String) = rows.update { all -> all.filterValues { it.createdAt >= createdAt } }

    override suspend fun clear() {
        rows.value = emptyMap()
    }

    private fun newestFirst(all: Map<String, CreationEntity>) = all.values.sortedByDescending { it.createdAt }
}

/** A completed creation made [minute] minutes past 10:00 on one day — later minutes are newer. */
fun cachedCreation(id: String, minute: Int) = CreationEntity(
    id = id,
    status = "COMPLETED",
    generatedImageUrl = null,
    templateTitleSnapshot = "Template",
    templateThumbnailSnapshot = null,
    createdAt = createdAt(minute),
    templateId = "",
    failureReason = null,
)

fun createdAt(minute: Int): String = "2026-09-24T10:${minute.toString().padStart(2, '0')}:00.000Z"

/** A `GET /creations` page body holding creations given as (id, minute), newest first. */
fun creationsPage(page: Int, hasMore: Boolean, vararg items: Pair<String, Int>): String {
    val rows = items.joinToString { (id, minute) ->
        """{ "id": "$id", "status": "COMPLETED", "templateTitleSnapshot": "Template", "createdAt": "${createdAt(minute)}" }"""
    }
    return """{ "success": true, "data": { "items": [$rows], "page": $page, "limit": 20, "hasMore": $hasMore } }"""
}

package com.ferbotz.aurapix.core.data.remote.dto

import kotlinx.serialization.Serializable

/** Success/error envelope: the real payload is always under [data]. */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val errorCode: String? = null,
    val message: String? = null,
)

/** Wrapper for paginated list endpoints. */
@Serializable
data class PagedResponse<T>(
    val items: List<T>,
    val page: Int,
    val limit: Int,
    val hasMore: Boolean,
)

package com.ferbotz.aurapix.core.data.remote.dto

import kotlinx.serialization.Serializable

/** Shared summary DTOs used across feed, category and template features. */

@Serializable
data class CategorySummaryDto(
    val id: String,
    val name: String,
    val slug: String,
    val iconUrl: String? = null,
    val bannerUrl: String? = null,
    val isFeatured: Boolean = false,
    val displayOrder: Int = 0,
)

@Serializable
data class TemplateSummaryDto(
    val id: String,
    val slug: String,
    val title: String,
    val shortDescription: String,
    val thumbnailImageUrl: String? = null,
    val isTrending: Boolean = false,
)

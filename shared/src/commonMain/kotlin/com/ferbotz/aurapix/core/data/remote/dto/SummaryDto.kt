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
    /** The <=512px card copy — what every list and grid draws. */
    val thumbnailImageUrl: String? = null,
    /**
     * The <=1280px display copy, the same object template detail serves. Only for surfaces that
     * render a summary large; `null` means it has not been generated, and falls back to
     * [thumbnailImageUrl] client-side rather than to the original upload (BE-006).
     */
    val thumbnailDisplayUrl: String? = null,
    val isTrending: Boolean = false,
)

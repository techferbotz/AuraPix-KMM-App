package com.ferbotz.aurapix.template.data.dto

import com.ferbotz.aurapix.core.data.remote.dto.CategorySummaryDto
import kotlinx.serialization.Serializable

@Serializable
data class TemplateDetailDto(
    val id: String,
    val slug: String,
    val title: String,
    val shortDescription: String,
    val longDescription: String? = null,
    val thumbnailImageUrl: String? = null,
    val isTrending: Boolean = false,
    val status: String = "ACTIVE",
    val categories: List<CategorySummaryDto> = emptyList(),
    val previewImages: List<PreviewImageDto> = emptyList(),
    val imageSlots: List<ImageSlotDto> = emptyList(),
    val version: Int = 1,
    val templateVersionId: String,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class ImageSlotDto(
    val id: String,
    val title: String,
    val description: String,
    val exampleImageUrl: String? = null,
    val displayOrder: Int,
    val isRequired: Boolean = true,
)

@Serializable
data class PreviewImageDto(
    val id: String,
    val imageUrl: String,
    val displayOrder: Int,
)

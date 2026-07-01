package com.ferbotz.aurapix.creation.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GenerationStartDto(
    val creationId: String,
    val status: String,
)

@Serializable
data class CreationDto(
    val id: String,
    val status: String,
    val generatedImageUrl: String? = null,
    val templateTitleSnapshot: String,
    val templateThumbnailSnapshot: String? = null,
    val createdAt: String,
)

@Serializable
data class CreationDetailDto(
    val id: String,
    val status: String,
    val generatedImageUrl: String? = null,
    val templateTitleSnapshot: String,
    val templateThumbnailSnapshot: String? = null,
    val createdAt: String,
    val templateId: String,
    val templateVersionId: String,
    val failureReason: String? = null,
    val updatedAt: String,
)

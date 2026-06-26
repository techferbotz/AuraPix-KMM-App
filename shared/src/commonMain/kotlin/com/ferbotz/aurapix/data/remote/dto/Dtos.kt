package com.ferbotz.aurapix.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// ── Envelope ──────────────────────────────────────────────────────────────────

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val errorCode: String? = null,
    val message: String? = null,
)

@Serializable
data class PagedResponse<T>(
    val items: List<T>,
    val page: Int,
    val limit: Int,
    val hasMore: Boolean,
)

// ── Auth ──────────────────────────────────────────────────────────────────────

@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val user: UserSummaryDto,
)

@Serializable
data class UserSummaryDto(
    val id: String,
    val email: String,
    val displayName: String,
    val profileImageUrl: String? = null,
    val role: String = "USER",
)

// ── Profile / Credits ─────────────────────────────────────────────────────────

@Serializable
data class ProfileDto(
    val id: String,
    val email: String,
    val displayName: String,
    val profileImageUrl: String? = null,
    val credits: CreditsDto,
    val subscription: SubscriptionDto,
)

@Serializable
data class CreditsDto(
    val subscriptionCredits: Int,
    val purchasedCredits: Int,
    val totalCredits: Int,
)

@Serializable
data class SubscriptionDto(
    val status: String,
    val productId: String? = null,
    val expiresAt: String? = null,
)

// ── Feed ──────────────────────────────────────────────────────────────────────

@Serializable
data class FeedResponseDto(val trays: List<FeedTrayDto>)

@Serializable
data class FeedTrayDto(
    val id: String,
    val type: String,
    val title: String,
    val displayOrder: Int,
    val items: List<JsonElement>,
)

// ── Categories ────────────────────────────────────────────────────────────────

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

// ── Templates ─────────────────────────────────────────────────────────────────

@Serializable
data class TemplateSummaryDto(
    val id: String,
    val slug: String,
    val title: String,
    val shortDescription: String,
    val thumbnailImageUrl: String? = null,
    val isTrending: Boolean = false,
)

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

// ── Generation ────────────────────────────────────────────────────────────────

@Serializable
data class GenerationStartDto(
    val creationId: String,
    val status: String,
)

// ── Creations ─────────────────────────────────────────────────────────────────

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

// ── Subscriptions ─────────────────────────────────────────────────────────────

@Serializable
data class SubscriptionDetailDto(
    val status: String,
    val productId: String? = null,
    val startedAt: String? = null,
    val expiresAt: String? = null,
)

// ── Purchases ─────────────────────────────────────────────────────────────────

@Serializable
data class PurchaseDto(
    val id: String,
    val productId: String,
    val creditsGranted: Int,
    val createdAt: String,
)

// ── Health ────────────────────────────────────────────────────────────────────

@Serializable
data class HealthDto(val status: String)

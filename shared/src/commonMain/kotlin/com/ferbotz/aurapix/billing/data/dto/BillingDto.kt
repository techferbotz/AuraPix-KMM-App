package com.ferbotz.aurapix.billing.data.dto

import kotlinx.serialization.Serializable

/** Full subscription detail from GET /subscriptions. */
@Serializable
data class SubscriptionDetailDto(
    val status: String,
    val productId: String? = null,
    val startedAt: String? = null,
    val expiresAt: String? = null,
)

@Serializable
data class PurchaseDto(
    val id: String,
    val productId: String,
    val creditsGranted: Int,
    val createdAt: String,
)

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

/** Returned by the billing verify endpoints (§3 BillingResult). */
@Serializable
data class BillingResultDto(
    val verified: Boolean,
    val credits: BillingCreditsDto = BillingCreditsDto(),
    val subscription: BillingSubscriptionDto? = null,
    val applied: BillingAppliedDto? = null,
)

@Serializable
data class BillingCreditsDto(
    val subscriptionCredits: Int = 0,
    val purchasedCredits: Int = 0,
    val totalCredits: Int = 0,
)

@Serializable
data class BillingSubscriptionDto(
    val status: String = "NONE",
    val productId: String? = null,
    val startedAt: String? = null,
    val expiresAt: String? = null,
)

@Serializable
data class BillingAppliedDto(
    val subscriptionsGranted: Int = 0,
    val purchasesApplied: Int = 0,
    val consumeBackfilled: Int = 0,
)

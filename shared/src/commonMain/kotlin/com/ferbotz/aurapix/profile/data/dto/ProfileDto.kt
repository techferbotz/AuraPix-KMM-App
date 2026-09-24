package com.ferbotz.aurapix.profile.data.dto

import kotlinx.serialization.Serializable

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

/** §4.2a — returned once the account and everything in it is gone. */
@Serializable
data class DeleteAccountDto(
    val deleted: Boolean,
    val deletedAt: String? = null,
)

/** Subscription snapshot embedded in the profile response. */
@Serializable
data class SubscriptionDto(
    val status: String,
    val productId: String? = null,
    val expiresAt: String? = null,
)

package com.ferbotz.aurapix.core.config

import kotlinx.serialization.Serializable

@Serializable
enum class OfferType { SUBSCRIPTION, ONE_TIME }

/** A single purchasable option shown in the paywall (subscription or one-time gem pack). */
@Serializable
data class PurchaseOffer(
    val productId: String,
    val type: OfferType,
    val title: String,
    val subtitle: String,
    val priceLabel: String,
    val gems: Int,
    val highlighted: Boolean = false,
)

/**
 * Store offers and price copy, delivered as a bundled JSON blob so prices can be adjusted without
 * touching code. `freeUserOffers`/`proUserOffers` are shown in the paywall depending on whether
 * the user has an active subscription.
 *
 * What a generation **costs** is deliberately not here: the server is what charges it, so it comes
 * from `RemoteConfig.generation.creditCost` (BE-005) and has one owner. Reintroducing it here
 * would give the paywall a second number that can silently disagree with the one being charged.
 */
@Serializable
data class MonetizationConfig(
    val currencySymbol: String = "₹",
    val freeUserOffers: List<PurchaseOffer> = emptyList(),
    val proUserOffers: List<PurchaseOffer> = emptyList(),
)

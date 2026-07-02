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
 * Remote-tunable monetization values. Delivered as a JSON blob so Firebase Remote Config can later
 * override the defaults without an app update. `freeUserOffers`/`proUserOffers` are shown in the
 * paywall depending on whether the user has an active subscription.
 */
@Serializable
data class MonetizationConfig(
    val generationCostGems: Int = 10,
    val currencySymbol: String = "₹",
    val freeUserOffers: List<PurchaseOffer> = emptyList(),
    val proUserOffers: List<PurchaseOffer> = emptyList(),
)

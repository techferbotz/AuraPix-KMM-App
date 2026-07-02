package com.ferbotz.aurapix.billing.data

/** A RevenueCat offering package, flattened for the paywall UI. */
data class RcPackage(
    val packageId: String,    // RevenueCat Package.identifier — used to purchase
    val productId: String,    // StoreProduct.id — used for backend verify + gem lookup
    val title: String,
    val priceLabel: String,   // localized StoreProduct.price.formatted
    val isSubscription: Boolean,
)

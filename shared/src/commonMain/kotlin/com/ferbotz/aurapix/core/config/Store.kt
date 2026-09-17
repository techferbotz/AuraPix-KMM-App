package com.ferbotz.aurapix.core.config

/**
 * The purchasable catalogue, served by `GET /config` (APP-002).
 *
 * Everything the store screens draw lives here — which products exist, their order, copy, gem
 * counts, perks and which one is pre-selected — so **adding or repricing a plan needs no app
 * release**. The one thing deliberately absent is the price: the store SDK returns the real
 * localized amount the user will be charged, and a price shipped in JSON would show every country
 * the same number. [Product.fallbackPriceLabel] is only for when the offering can't be loaded.
 *
 * The catalogue is authoritative over the store SDK for *what to show*: a product the SDK offers
 * but this list doesn't describe is hidden rather than drawn with blanks. That is recoverable
 * without a release — add it here — whereas a card that can't say what it sells is not.
 */
data class Store(
    /**
     * Pre-selected product. Each screen uses it when the id is in its own filtered list, and
     * otherwise falls back to the highlighted product, then the first.
     */
    val defaultProductId: String? = null,
    /**
     * The catalogue. A **missing** `store` section keeps this compiled-in list (protocol 11's
     * leaf-by-leaf defaulting); an **explicitly empty** list hides the store entirely, which is a
     * deliberate kill switch and distinct from "not configured".
     */
    val products: List<Product> = DEFAULT_PRODUCTS,
) {

    /** One purchasable product. [productId] must match the store/RevenueCat product id exactly. */
    data class Product(
        val productId: String,
        val kind: Kind,
        /** Who this is offered to — lets a subscriber be shown gem packs but not the plan again. */
        val audience: Audience = Audience.ALL,
        val displayOrder: Int = 0,
        val title: String,
        val subtitle: String? = null,
        /** Gems granted. For a subscription, granted per period. */
        val gems: Int = 0,
        /** "month" for a subscription; null for a one-time purchase. */
        val periodLabel: String? = null,
        /** Overrides the derived "≈ N images" cue. Null derives it from [gems] and the credit cost. */
        val imagesLabel: String? = null,
        /** Small pill on the card, e.g. "Best value". Null draws none. */
        val badge: String? = null,
        val highlighted: Boolean = false,
        /** Label for the confirm button when this product is selected. Null uses the screen default. */
        val ctaLabel: String? = null,
        /** Small print under the CTA, e.g. "Cancel anytime · billed monthly". */
        val footnote: String? = null,
        /** Bullet list for the subscription card. Empty for gem packs. */
        val perks: List<String> = emptyList(),
        /** Shown only when the store SDK's real price is unavailable. */
        val fallbackPriceLabel: String? = null,
    )

    enum class Kind { ONE_TIME, SUBSCRIPTION }

    /** Unknown wire values fall back to [ALL] rather than hiding a product the backend meant to show. */
    enum class Audience { ALL, FREE, PREMIUM }

    /** Everything this user may be offered, in the backend's order. */
    fun visibleProducts(isPremium: Boolean): List<Product> =
        products.filter { it.audience.isVisibleTo(isPremium) }.sortedBy { it.displayOrder }

    /** The products for one screen: the right kind, visible to this user, in the backend's order. */
    fun productsFor(kind: Kind, isPremium: Boolean): List<Product> =
        visibleProducts(isPremium).filter { it.kind == kind }

    /** Which product a screen starts on: the configured default, else highlighted, else the first. */
    fun defaultFor(candidates: List<Product>): Product? =
        candidates.firstOrNull { it.productId == defaultProductId }
            ?: candidates.firstOrNull { it.highlighted }
            ?: candidates.firstOrNull()

    companion object {
        /**
         * The catalogue as it stands today, and the fallback when `/config` has never been
         * reached. Product ids are the live RevenueCat ones — a mismatch here is what made the
         * purchase screen show "0 gems" before this section existed.
         */
        val DEFAULT_PRODUCTS: List<Product> = listOf(
            Product(
                productId = "aurapix_premium",
                kind = Kind.SUBSCRIPTION,
                audience = Audience.FREE,
                displayOrder = 1,
                title = "Premium",
                subtitle = "Go unlimited. Cancel anytime.",
                gems = 100,
                periodLabel = "month",
                highlighted = true,
                ctaLabel = "Go Premium",
                footnote = "Cancel anytime · billed monthly",
                perks = listOf(
                    "No ads",
                    "100 gems every month",
                    "Priority rendering (10× faster)",
                    "Exclusive premium styles",
                    "Commercial usage rights",
                ),
                fallbackPriceLabel = "₹100",
            ),
            Product(
                productId = "gem_value_pack",
                kind = Kind.ONE_TIME,
                displayOrder = 2,
                title = "Value Pack",
                gems = 120,
                badge = "Best value",
                highlighted = true,
                fallbackPriceLabel = "₹150",
            ),
            Product(
                productId = "gem_starter_pack",
                kind = Kind.ONE_TIME,
                displayOrder = 3,
                title = "Starter Pack",
                gems = 50,
                fallbackPriceLabel = "₹70",
            ),
            Product(
                productId = "gem_pocket_pack",
                kind = Kind.ONE_TIME,
                displayOrder = 4,
                title = "Pocket Pack",
                gems = 10,
                fallbackPriceLabel = "₹15",
            ),
        )
    }
}

private fun Store.Audience.isVisibleTo(isPremium: Boolean): Boolean = when (this) {
    Store.Audience.ALL -> true
    Store.Audience.FREE -> !isPremium
    Store.Audience.PREMIUM -> isPremium
}

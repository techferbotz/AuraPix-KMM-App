package com.ferbotz.aurapix.core.config

import com.ferbotz.aurapix.core.data.remote.auraJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The store catalogue's parse rules (APP-002). The catalogue decides what the purchase screens
 * show, so the cases that matter are the ones where the backend sends something unexpected: the
 * app must never end up drawing a product it cannot describe, and must never lose the whole
 * catalogue over one bad entry.
 */
class StoreParsingTest {

    private fun store(json: String): Store =
        auraJson.decodeFromString<RemoteConfigResponseDto>(json)
            .toDomain(0L, RemoteConfigSnapshot.Source.NETWORK)
            .config
            .store

    @Test
    fun anAbsentStoreKeepsTheCompiledInCatalogue() {
        assertEquals(Store.DEFAULT_PRODUCTS, store("""{ "config": {} }""").products)
    }

    @Test
    fun theCompiledInCatalogueMatchesTheLiveProductIds() {
        // The purchase screen showed "0 gems" because these drifted from the store's ids once.
        assertEquals(
            listOf("aurapix_premium", "gem_value_pack", "gem_starter_pack", "gem_pocket_pack"),
            Store.DEFAULT_PRODUCTS.map { it.productId },
        )
    }

    @Test
    fun aServedCatalogueReplacesTheCompiledInOne() {
        val s = store(
            """
            { "config": { "store": { "defaultProductId": "p2", "products": [
              { "productId": "p1", "kind": "ONE_TIME", "title": "Small", "gems": 10, "displayOrder": 2 },
              { "productId": "p2", "kind": "ONE_TIME", "title": "Big", "gems": 99, "displayOrder": 1,
                "badge": "Best value", "highlighted": true }
            ] } } }
            """
        )
        assertEquals(listOf("p1", "p2"), s.products.map { it.productId })
        assertEquals("p2", s.defaultProductId)
        // displayOrder, not payload order, decides what the screen shows first.
        assertEquals(listOf("p2", "p1"), s.productsFor(Store.Kind.ONE_TIME, isPremium = false).map { it.productId })
        assertEquals("Best value", s.products.first { it.productId == "p2" }.badge)
    }

    /** An explicitly empty list is a kill switch, and must not be confused with "not configured". */
    @Test
    fun anEmptyProductListHidesTheStore() {
        assertEquals(emptyList(), store("""{ "config": { "store": { "products": [] } } }""").products)
    }

    @Test
    fun anUndrawableEntryIsDroppedWithoutLosingTheRest() {
        val s = store(
            """
            { "config": { "store": { "products": [
              { "kind": "ONE_TIME", "title": "No id", "gems": 10 },
              { "productId": "no_title", "kind": "ONE_TIME", "gems": 10 },
              { "productId": "bad_kind", "kind": "LAYAWAY", "title": "Unknown kind", "gems": 10 },
              { "productId": "good", "kind": "ONE_TIME", "title": "Fine", "gems": 10 }
            ] } } }
            """
        )
        assertEquals(listOf("good"), s.products.map { it.productId })
    }

    @Test
    fun anUnknownAudienceFallsBackToEveryone() {
        val s = store(
            """
            { "config": { "store": { "products": [
              { "productId": "p", "kind": "ONE_TIME", "title": "T", "audience": "SOMEDAY" }
            ] } } }
            """
        )
        assertEquals(Store.Audience.ALL, s.products.single().audience)
        assertEquals(1, s.productsFor(Store.Kind.ONE_TIME, isPremium = true).size)
        assertEquals(1, s.productsFor(Store.Kind.ONE_TIME, isPremium = false).size)
    }

    @Test
    fun audienceDecidesWhoSeesAProduct() {
        val s = Store(
            products = listOf(
                Store.Product("free", Store.Kind.SUBSCRIPTION, Store.Audience.FREE, title = "Join"),
                Store.Product("paid", Store.Kind.ONE_TIME, Store.Audience.PREMIUM, title = "Top up"),
                Store.Product("all", Store.Kind.ONE_TIME, Store.Audience.ALL, title = "Pack"),
            )
        )
        assertEquals(listOf("free", "all"), s.visibleProducts(isPremium = false).map { it.productId })
        assertEquals(listOf("paid", "all"), s.visibleProducts(isPremium = true).map { it.productId })
    }

    @Test
    fun theDefaultFallsBackFromConfiguredToHighlightedToFirst() {
        val a = Store.Product("a", Store.Kind.ONE_TIME, title = "A")
        val b = Store.Product("b", Store.Kind.ONE_TIME, title = "B", highlighted = true)
        val all = listOf(a, b)

        assertEquals("a", Store(defaultProductId = "a", products = all).defaultFor(all)?.productId)
        assertEquals("b", Store(defaultProductId = "gone", products = all).defaultFor(all)?.productId)
        assertEquals("a", Store(products = all).defaultFor(listOf(a))?.productId)
        assertNull(Store(products = all).defaultFor(emptyList()))
    }

    @Test
    fun partialStoreKeysDefaultLeafByLeaf() {
        // Only defaultProductId sent: the catalogue itself must survive.
        val s = store("""{ "config": { "store": { "defaultProductId": "gem_pocket_pack" } } }""")
        assertEquals("gem_pocket_pack", s.defaultProductId)
        assertEquals(Store.DEFAULT_PRODUCTS, s.products)
    }

    @Test
    fun perksAndCopyRideAlongForTheSubscriptionCard() {
        val premium = Store.DEFAULT_PRODUCTS.first { it.kind == Store.Kind.SUBSCRIPTION }
        assertEquals("month", premium.periodLabel)
        assertEquals("Go Premium", premium.ctaLabel)
        assertTrue(premium.perks.isNotEmpty(), "the subscription card renders these")
        // No compiled-in price: BE-007 serves none either, and a stale one could contradict Play.
        assertNull(premium.fallbackPriceLabel)
    }
}

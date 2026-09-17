package com.ferbotz.aurapix.core.config

import kotlinx.serialization.json.Json

/**
 * Source of the store offers and price copy. Parses a bundled JSON so prices can be adjusted
 * without touching code.
 *
 * Deliberately NOT part of [RemoteConfig], the `GET /config` document: the backend does not serve
 * offers, and the products themselves come from RevenueCat. The one value the two used to share —
 * what a generation costs — now lives only in `RemoteConfig.generation.creditCost`, because the
 * server is what actually charges it (BE-005). See [MonetizationConfig.generationCostGems].
 */
interface MonetizationConfigProvider {
    val monetization: MonetizationConfig
}

class DefaultMonetizationConfigProvider : MonetizationConfigProvider {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override val monetization: MonetizationConfig by lazy {
        runCatching { json.decodeFromString<MonetizationConfig>(DEFAULT_MONETIZATION_JSON) }
            .getOrDefault(MonetizationConfig())
    }
}

/**
 * Default monetization values (mirrors what Firebase Remote Config will later serve as the
 * `monetization` JSON string). All prices/gems live here so they can be tuned without a release.
 */
private const val DEFAULT_MONETIZATION_JSON = """
{
  "currencySymbol": "₹",
  "freeUserOffers": [
    {
      "productId": "monthly",
      "type": "SUBSCRIPTION",
      "title": "Premium",
      "subtitle": "No ads + 100 gems every month",
      "priceLabel": "₹99/mo",
      "gems": 100,
      "highlighted": true
    },
    {
      "productId": "gem_one_time_purchase",
      "type": "ONE_TIME",
      "title": "1 Generation",
      "subtitle": "10 gems, one-time",
      "priceLabel": "₹15",
      "gems": 10,
      "highlighted": false
    }
  ],
  "proUserOffers": [
    {
      "productId": "gem_value_pack",
      "type": "ONE_TIME",
      "title": "Value Pack",
      "subtitle": "100 gems, one-time",
      "priceLabel": "₹100",
      "gems": 100,
      "highlighted": true
    },
    {
      "productId": "gem_one_time_purchase",
      "type": "ONE_TIME",
      "title": "1 Generation",
      "subtitle": "10 gems, one-time",
      "priceLabel": "₹15",
      "gems": 10,
      "highlighted": false
    }
  ]
}
"""

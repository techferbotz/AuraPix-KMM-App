package com.ferbotz.aurapix.core.config

import kotlinx.serialization.json.Json

/**
 * Source of remote-tunable values. [DefaultRemoteConfig] parses a bundled JSON; swap in a
 * Firebase Remote Config-backed implementation later (fetch → same JSON shape → parse).
 */
interface RemoteConfig {
    val monetization: MonetizationConfig
}

class DefaultRemoteConfig : RemoteConfig {
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
  "generationCostGems": 10,
  "currencySymbol": "₹",
  "freeUserOffers": [
    {
      "productId": "monthly",
      "type": "SUBSCRIPTION",
      "title": "Monthly Pass",
      "subtitle": "100 gems every month",
      "priceLabel": "₹100/mo",
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

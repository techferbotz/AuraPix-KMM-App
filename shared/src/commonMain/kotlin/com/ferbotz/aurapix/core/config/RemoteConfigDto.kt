package com.ferbotz.aurapix.core.config

import kotlinx.serialization.Serializable

/**
 * The `data` payload of `GET /config` (BE-005).
 *
 * Every field is nullable with a `null` default **on purpose** — the document is
 * forward-compatible on the wire. A key this build doesn't know is dropped by
 * `auraJson.ignoreUnknownKeys`; a key the backend hasn't sent parses as `null` and
 * [RemoteConfigMappers] fills it from the compiled-in default. Only a removed or renamed key
 * bumps [schemaVersion].
 *
 * This leniency is the deliberate mirror of the backend, which merges strictly and refuses to
 * start on a bad override: be strict where a mistake can still be fixed, lenient where it can't.
 * A client that threw on an unexpected document would brick installed builds nobody can fix.
 */
@Serializable
data class RemoteConfigResponseDto(
    val schemaVersion: Int? = null,
    /** Which resolution this install got — `"default"`, or `"default+<ruleId>+…"` once rules exist. */
    val variant: String? = null,
    /** Fingerprint of [config]; changes whenever any value changes. */
    val revision: String? = null,
    /** How long the document may be used before re-fetching. */
    val ttlSeconds: Long? = null,
    val config: RemoteConfigDto? = null,
)

/** The config document itself. Sections and leaves are all optional — see [RemoteConfigResponseDto]. */
@Serializable
data class RemoteConfigDto(
    val features: FeaturesDto? = null,
    val update: UpdatePolicyDto? = null,
    val maintenance: MaintenanceDto? = null,
    val generation: GenerationDto? = null,
    val pagination: PaginationDto? = null,
    val links: LinksDto? = null,
    val store: StoreDto? = null,
)

/**
 * The store catalogue. [products] absent keeps the compiled-in list; present-but-empty hides the
 * store, which is a deliberate kill switch rather than a missing value.
 */
@Serializable
data class StoreDto(
    val defaultProductId: String? = null,
    val products: List<StoreProductDto>? = null,
)

@Serializable
data class StoreProductDto(
    val productId: String? = null,
    val kind: String? = null,
    val audience: String? = null,
    val displayOrder: Int? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val gems: Int? = null,
    val periodLabel: String? = null,
    val imagesLabel: String? = null,
    val badge: String? = null,
    val highlighted: Boolean? = null,
    val ctaLabel: String? = null,
    val footnote: String? = null,
    val perks: List<String>? = null,
    val fallbackPriceLabel: String? = null,
)

@Serializable
data class FeaturesDto(
    val imageGeneration: Boolean? = null,
    val googleLogin: Boolean? = null,
    val premium: Boolean? = null,
    val creditPurchase: Boolean? = null,
    val creations: Boolean? = null,
    val categories: Boolean? = null,
)

@Serializable
data class UpdatePolicyDto(
    val minSupportedBuild: Long? = null,
    val recommendedBuild: Long? = null,
    val androidStoreUrl: String? = null,
    val iosStoreUrl: String? = null,
    val message: String? = null,
)

@Serializable
data class MaintenanceDto(
    val enabled: Boolean? = null,
    val message: String? = null,
)

@Serializable
data class GenerationDto(
    val creditCost: Int? = null,
    val maxImageBytes: Long? = null,
    val maxImagesPerRequest: Int? = null,
    val pollIntervalSeconds: Double? = null,
    val pollTimeoutSeconds: Double? = null,
)

@Serializable
data class PaginationDto(
    val defaultLimit: Int? = null,
    val maxLimit: Int? = null,
)

@Serializable
data class LinksDto(
    val privacyPolicy: String? = null,
    val terms: String? = null,
    val deleteAccount: String? = null,
    val supportEmail: String? = null,
)

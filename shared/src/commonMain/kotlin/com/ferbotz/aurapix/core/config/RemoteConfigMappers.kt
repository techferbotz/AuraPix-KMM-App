package com.ferbotz.aurapix.core.config

/**
 * Resolves a wire document into a [RemoteConfigSnapshot], filling every missing key from
 * [defaults] — the contract's "ignore unknown keys, default the missing ones" rule. (Unknown keys
 * never reach here; JSON decoding drops them.)
 */
fun RemoteConfigResponseDto.toDomain(
    fetchedAtEpochMillis: Long?,
    source: RemoteConfigSnapshot.Source,
    defaults: RemoteConfig = RemoteConfig.Default,
): RemoteConfigSnapshot {
    val base = RemoteConfigSnapshot.Defaults
    return RemoteConfigSnapshot(
        schemaVersion = schemaVersion ?: base.schemaVersion,
        variant = variant?.takeIf { it.isNotBlank() } ?: base.variant,
        revision = revision ?: base.revision,
        ttlSeconds = ttlSeconds?.takeIf { it > 0 } ?: base.ttlSeconds,
        config = config.toDomain(defaults),
        fetchedAtEpochMillis = fetchedAtEpochMillis,
        source = source,
    )
}

/**
 * Leaf-by-leaf merge of the wire document over [defaults]: a null section **or** a null leaf keeps
 * the default.
 *
 * The leaf-by-leaf part is the whole point and the easy thing to get wrong. Defaulting per
 * *section* — `features = dto.features ?: defaults.features` — silently wipes every unmentioned
 * leaf the moment the backend sends a partial section, so `{"features":{"premium":false}}` would
 * reset the other five switches instead of flipping one. `RemoteConfigParsingTest` pins this.
 */
fun RemoteConfigDto?.toDomain(defaults: RemoteConfig = RemoteConfig.Default): RemoteConfig {
    if (this == null) return defaults
    val f = features
    val fd = defaults.features
    val u = update
    val ud = defaults.update
    val m = maintenance
    val md = defaults.maintenance
    val g = generation
    val gd = defaults.generation
    val p = pagination
    val pd = defaults.pagination
    val k = links
    val kd = defaults.links
    val st = store
    val std = defaults.store
    return RemoteConfig(
        features = RemoteConfig.Features(
            imageGeneration = f?.imageGeneration ?: fd.imageGeneration,
            googleLogin = f?.googleLogin ?: fd.googleLogin,
            premium = f?.premium ?: fd.premium,
            creditPurchase = f?.creditPurchase ?: fd.creditPurchase,
            creations = f?.creations ?: fd.creations,
            categories = f?.categories ?: fd.categories,
            promptSharing = f?.promptSharing ?: fd.promptSharing,
        ),
        update = RemoteConfig.UpdatePolicy(
            minSupportedBuild = u?.minSupportedBuild ?: ud.minSupportedBuild,
            recommendedBuild = u?.recommendedBuild ?: ud.recommendedBuild,
            androidStoreUrl = u?.androidStoreUrl ?: ud.androidStoreUrl,
            iosStoreUrl = u?.iosStoreUrl ?: ud.iosStoreUrl,
            message = u?.message ?: ud.message,
        ),
        maintenance = RemoteConfig.Maintenance(
            enabled = m?.enabled ?: md.enabled,
            message = m?.message ?: md.message,
        ),
        generation = RemoteConfig.Generation(
            creditCost = g?.creditCost ?: gd.creditCost,
            maxImageBytes = g?.maxImageBytes ?: gd.maxImageBytes,
            maxImagesPerRequest = g?.maxImagesPerRequest ?: gd.maxImagesPerRequest,
            pollIntervalSeconds = g?.pollIntervalSeconds ?: gd.pollIntervalSeconds,
            pollTimeoutSeconds = g?.pollTimeoutSeconds ?: gd.pollTimeoutSeconds,
        ),
        pagination = RemoteConfig.Pagination(
            defaultLimit = p?.defaultLimit ?: pd.defaultLimit,
            maxLimit = p?.maxLimit ?: pd.maxLimit,
        ),
        links = RemoteConfig.Links(
            privacyPolicy = k?.privacyPolicy ?: kd.privacyPolicy,
            terms = k?.terms ?: kd.terms,
            deleteAccount = k?.deleteAccount ?: kd.deleteAccount,
            supportEmail = k?.supportEmail ?: kd.supportEmail,
        ),
        store = Store(
            defaultProductId = st?.defaultProductId ?: std.defaultProductId,
            // A present list wins whole, including an empty one; only an absent list defaults.
            products = st?.products?.mapNotNull { it.toDomain() } ?: std.products,
        ),
    )
}

/**
 * One catalogue entry, or null when it can't be drawn.
 *
 * An entry with no id, no title or an unrecognised `kind` is **dropped rather than guessed at**:
 * there is no screen to put it on and no way to describe it, and a half-rendered product is worse
 * than an absent one. Every other unknown value falls back, so one malformed entry never costs
 * the rest of the catalogue.
 */
private fun StoreProductDto.toDomain(): Store.Product? {
    val id = productId?.takeIf { it.isNotBlank() } ?: return null
    val name = title?.takeIf { it.isNotBlank() } ?: return null
    val productKind = when (kind?.uppercase()) {
        "ONE_TIME" -> Store.Kind.ONE_TIME
        "SUBSCRIPTION" -> Store.Kind.SUBSCRIPTION
        else -> return null
    }
    return Store.Product(
        productId = id,
        kind = productKind,
        audience = when (audience?.uppercase()) {
            "FREE" -> Store.Audience.FREE
            "PREMIUM" -> Store.Audience.PREMIUM
            else -> Store.Audience.ALL
        },
        displayOrder = displayOrder ?: 0,
        title = name,
        subtitle = subtitle?.takeIf { it.isNotBlank() },
        gems = gems?.takeIf { it >= 0 } ?: 0,
        periodLabel = periodLabel?.takeIf { it.isNotBlank() },
        imagesLabel = imagesLabel?.takeIf { it.isNotBlank() },
        badge = badge?.takeIf { it.isNotBlank() },
        highlighted = highlighted ?: false,
        ctaLabel = ctaLabel?.takeIf { it.isNotBlank() },
        footnote = footnote?.takeIf { it.isNotBlank() },
        perks = perks?.filter { it.isNotBlank() } ?: emptyList(),
        fallbackPriceLabel = fallbackPriceLabel?.takeIf { it.isNotBlank() },
    )
}

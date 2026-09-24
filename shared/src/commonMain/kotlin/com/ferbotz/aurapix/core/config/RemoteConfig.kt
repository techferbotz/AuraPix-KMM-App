package com.ferbotz.aurapix.core.config

/**
 * The remote config document (`GET /config`, BE-005) as the app uses it — every field non-null.
 *
 * The **constructor defaults are the compiled-in defaults** and equal the backend's own defaults,
 * i.e. today's behaviour: honouring the config changes nothing until a value is deliberately
 * flipped server-side. A key missing from the wire — an older backend, a partial document, a
 * half-written cache — resolves to the value here **leaf by leaf**; see [RemoteConfigMappers].
 *
 * Adding a key here is safe and needs no backend release; the backend adds keys without bumping
 * `schemaVersion`, so a key this build doesn't know is simply ignored.
 */
data class RemoteConfig(
    val features: Features = Features(),
    val update: UpdatePolicy = UpdatePolicy(),
    val maintenance: Maintenance = Maintenance(),
    val generation: Generation = Generation(),
    val pagination: Pagination = Pagination(),
    val links: Links = Links(),
    /** The purchasable catalogue — see [Store]. */
    val store: Store = Store(),
) {

    /**
     * Kill switches for UI **entry points**. Turning one off hides the surface; the API route
     * itself keeps working, because closing an API is a server decision, not a config flag.
     */
    data class Features(
        /** The generate button on a template. */
        val imageGeneration: Boolean = true,
        /** "Continue with Google". */
        val googleLogin: Boolean = true,
        /** The paywall and every "Go Premium" entry point. */
        val premium: Boolean = true,
        /** Buying gem packs. */
        val creditPurchase: Boolean = true,
        /** The Creations tab. */
        val creations: Boolean = true,
        /** Category browsing. */
        val categories: Boolean = true,
        /** The "Get prompt" button on a template, which shows the prompt for use in other apps. */
        val promptSharing: Boolean = true,
    )

    /**
     * Update policy. Thresholds are **integer build numbers** (Android `versionCode`, the iOS
     * build) — never version strings, which don't sort: `"1.10.0" < "1.9.0"` is true as a string.
     * `0` means no requirement.
     */
    data class UpdatePolicy(
        val minSupportedBuild: Long = 0,
        val recommendedBuild: Long = 0,
        val androidStoreUrl: String? = "https://play.google.com/store/apps/details?id=com.ferbotz.aurapix",
        /** Null until the App Store listing exists. */
        val iosStoreUrl: String? = null,
        /** Optional server copy for either prompt. */
        val message: String? = null,
    ) {
        /**
         * What this policy asks of [buildNumber]. An unknown build (`<= 0`, e.g. the package info
         * couldn't be read) is **never** blocked — a client-side hiccup must not lock a user out.
         */
        fun requirementFor(buildNumber: Long): UpdateRequirement = when {
            buildNumber <= 0 -> UpdateRequirement.NONE
            minSupportedBuild > 0 && buildNumber < minSupportedBuild -> UpdateRequirement.REQUIRED
            recommendedBuild > 0 && buildNumber < recommendedBuild -> UpdateRequirement.RECOMMENDED
            else -> UpdateRequirement.NONE
        }

        /** The store listing for [platform], or null while that listing doesn't exist. */
        fun storeUrlFor(platform: AppPlatform): String? = when (platform) {
            AppPlatform.ANDROID -> androidStoreUrl
            AppPlatform.IOS -> iosStoreUrl
        }
    }

    /** `enabled` ⇒ the app shows [message] instead of loading anything. */
    data class Maintenance(
        val enabled: Boolean = false,
        val message: String? = null,
    )

    /**
     * Generation limits, mirrored from what the server enforces — check them before uploading so
     * a doomed request never leaves the device, and show [creditCost] wherever a price is quoted.
     */
    data class Generation(
        val creditCost: Int = 10,
        val maxImageBytes: Long = 10_485_760,
        val maxImagesPerRequest: Int = 8,
        val pollIntervalSeconds: Double = 1.5,
        val pollTimeoutSeconds: Double = 30.0,
    )

    /** Page sizes the API accepts. */
    data class Pagination(
        val defaultLimit: Int = 20,
        val maxLimit: Int = 50,
    )

    /**
     * Absolute URLs for the legal pages, plus the support address. Use these rather than
     * hard-coding a host, so the pages can move without an app release.
     */
    data class Links(
        val privacyPolicy: String = "https://aurapix.ferbotz.com/privacy",
        val terms: String = "https://aurapix.ferbotz.com/terms",
        val deleteAccount: String = "https://aurapix.ferbotz.com/delete-account",
        val supportEmail: String = "support@ferbotz.com",
    )

    companion object {
        /** The compiled-in defaults — what the app runs on until a config has ever been fetched. */
        val Default: RemoteConfig = RemoteConfig()
    }
}

/**
 * The page size the paginated endpoints are called with, tied to the config document's own
 * default so the two can't drift.
 *
 * This is the **compiled-in** default rather than the live value: reading the live one would mean
 * plumbing the repository into every data source, and BE-005 lists `pagination.*` as informational
 * rather than something the app must honour dynamically. If the backend ever wants to move the
 * page size without a release, that plumbing is the change to make.
 */
val DEFAULT_PAGE_LIMIT: Int = RemoteConfig.Default.pagination.defaultLimit

/** How a [RemoteConfig.UpdatePolicy] applies to the running build. */
enum class UpdateRequirement {
    NONE,

    /** Below `recommendedBuild`: a dismissible "update available" nudge. */
    RECOMMENDED,

    /** Below `minSupportedBuild`: a hard "update to continue" block. */
    REQUIRED,
}

/**
 * A resolved config document plus its provenance: the [config], the backend's metadata about it,
 * and when this device fetched it (`null` for the compiled-in defaults). [source] is the first
 * thing worth looking at when a flipped switch "didn't work".
 */
data class RemoteConfigSnapshot(
    val schemaVersion: Int,
    val variant: String,
    val revision: String,
    val ttlSeconds: Long,
    val config: RemoteConfig,
    val fetchedAtEpochMillis: Long?,
    val source: Source,
) {
    enum class Source {
        /** Never fetched on this device — the compiled-in [RemoteConfig.Default]. */
        DEFAULTS,

        /** The last good document, restored from preferences at startup. */
        CACHE,

        /** Fetched from the backend during this process. */
        NETWORK,
    }

    /** True when the document is older than its [ttlSeconds], or was never fetched. */
    fun isStale(nowEpochMillis: Long): Boolean {
        val fetchedAt = fetchedAtEpochMillis ?: return true
        return nowEpochMillis - fetchedAt >= ttlSeconds * 1000
    }

    companion object {
        /** The schema this build was written against; bumped only on a removed/renamed key. */
        const val SUPPORTED_SCHEMA_VERSION: Int = 1

        /** Default TTL when the backend doesn't say (matches its current value). */
        const val DEFAULT_TTL_SECONDS: Long = 3600

        val Defaults: RemoteConfigSnapshot = RemoteConfigSnapshot(
            schemaVersion = SUPPORTED_SCHEMA_VERSION,
            variant = "default",
            revision = "",
            ttlSeconds = DEFAULT_TTL_SECONDS,
            config = RemoteConfig.Default,
            fetchedAtEpochMillis = null,
            source = Source.DEFAULTS,
        )
    }
}

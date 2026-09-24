package com.ferbotz.aurapix.core.config

import com.ferbotz.aurapix.core.data.remote.auraJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The app half of the backend's `check:config` suite (HOA protocol 11 §5). These fixtures are the
 * acceptance criteria for the `GET /config` client: they pin the lenient-parse contract, which is
 * the deliberate mirror of the backend's strict merge. A client that threw on an unexpected
 * document would brick installed builds nobody can fix.
 */
class RemoteConfigParsingTest {

    private fun parse(json: String, fetchedAt: Long? = 1_000L) =
        auraJson.decodeFromString<RemoteConfigResponseDto>(json)
            .toDomain(fetchedAt, RemoteConfigSnapshot.Source.NETWORK)

    /** The document exactly as BE-005 documents it live today. */
    private val fullDocument = """
    {
      "schemaVersion": 1, "variant": "default", "revision": "9947ce93235d", "ttlSeconds": 3600,
      "config": {
        "features": { "imageGeneration": true, "googleLogin": true, "premium": true,
                      "creditPurchase": true, "creations": true, "categories": true },
        "update": { "minSupportedBuild": 0, "recommendedBuild": 0,
                    "androidStoreUrl": "https://play.google.com/store/apps/details?id=com.ferbotz.aurapix",
                    "iosStoreUrl": null, "message": null },
        "maintenance": { "enabled": false, "message": null },
        "generation": { "creditCost": 10, "maxImageBytes": 10485760, "maxImagesPerRequest": 8,
                        "pollIntervalSeconds": 1.5, "pollTimeoutSeconds": 30 },
        "pagination": { "defaultLimit": 20, "maxLimit": 50 },
        "links": { "privacyPolicy": "https://aurapix.ferbotz.com/privacy",
                   "terms": "https://aurapix.ferbotz.com/terms",
                   "deleteAccount": "https://aurapix.ferbotz.com/delete-account",
                   "supportEmail": "support@ferbotz.com" }
      }
    }
    """

    @Test
    fun fullDocumentMapsEveryField() {
        val snap = parse(fullDocument)
        assertEquals(1, snap.schemaVersion)
        assertEquals("default", snap.variant)
        assertEquals("9947ce93235d", snap.revision)
        assertEquals(3600, snap.ttlSeconds)
        assertEquals(RemoteConfigSnapshot.Source.NETWORK, snap.source)

        val c = snap.config
        assertTrue(c.features.imageGeneration && c.features.googleLogin && c.features.premium)
        assertTrue(c.features.creditPurchase && c.features.creations && c.features.categories)
        assertEquals(0, c.update.minSupportedBuild)
        assertEquals(0, c.update.recommendedBuild)
        assertEquals(
            "https://play.google.com/store/apps/details?id=com.ferbotz.aurapix",
            c.update.androidStoreUrl,
        )
        assertNull(c.update.iosStoreUrl)
        assertFalse(c.maintenance.enabled)
        assertEquals(10, c.generation.creditCost)
        assertEquals(10_485_760, c.generation.maxImageBytes)
        assertEquals(8, c.generation.maxImagesPerRequest)
        assertEquals(1.5, c.generation.pollIntervalSeconds)
        assertEquals(30.0, c.generation.pollTimeoutSeconds)
        assertEquals(20, c.pagination.defaultLimit)
        assertEquals(50, c.pagination.maxLimit)
        assertEquals("https://aurapix.ferbotz.com/privacy", c.links.privacyPolicy)
        assertEquals("support@ferbotz.com", c.links.supportEmail)
    }

    /** The document the app must survive if the endpoint is ever served empty. */
    @Test
    fun emptyResponseFallsBackToCompiledInDefaults() {
        val snap = parse("{}")
        assertEquals(RemoteConfig.Default, snap.config)
        assertEquals("default", snap.variant)
        assertEquals(RemoteConfigSnapshot.DEFAULT_TTL_SECONDS, snap.ttlSeconds)
        assertEquals(RemoteConfigSnapshot.SUPPORTED_SCHEMA_VERSION, snap.schemaVersion)
    }

    @Test
    fun emptyConfigObjectFallsBackToCompiledInDefaults() {
        assertEquals(RemoteConfig.Default, parse("""{ "config": {} }""").config)
    }

    /**
     * The one that actually breaks in practice: a partial section must flip its own leaf and leave
     * every sibling at its default. Defaulting per *section* would silently wipe the other five
     * switches the moment the backend sent one of them.
     */
    @Test
    fun partialSectionsDefaultLeafByLeaf() {
        val c = parse("""{ "config": { "features": { "premium": false } } }""").config
        assertFalse(c.features.premium)
        assertTrue(c.features.imageGeneration, "an unmentioned leaf keeps its default")
        assertTrue(c.features.googleLogin)
        assertTrue(c.features.creditPurchase)
        assertTrue(c.features.creations)
        assertTrue(c.features.categories)
        assertTrue(c.features.promptSharing, "a switch today's backend doesn't send yet stays on")
        // …and an untouched section is entirely unaffected.
        assertEquals(RemoteConfig.Default.generation, c.generation)
        assertEquals(RemoteConfig.Default.links, c.links)
    }

    @Test
    fun unknownKeysAreIgnoredAtEveryLevelIncludingANewerSchema() {
        val snap = parse(
            """
            {
              "schemaVersion": 99, "variant": "default+some-rule", "somethingNew": { "a": 1 },
              "config": {
                "features": { "premium": false, "timeTravel": true },
                "brandNewSection": { "x": 1 }
              }
            }
            """
        )
        assertEquals(99, snap.schemaVersion, "a newer schema is reported, not rejected")
        assertEquals("default+some-rule", snap.variant)
        assertFalse(snap.config.features.premium)
        assertTrue(snap.config.features.imageGeneration)
    }

    @Test
    fun explicitNullsBehaveLikeMissingKeys() {
        val c = parse(
            """{ "config": { "features": { "premium": null }, "generation": null, "links": null } }"""
        ).config
        assertTrue(c.features.premium)
        assertEquals(RemoteConfig.Default.generation, c.generation)
        assertEquals(RemoteConfig.Default.links, c.links)
    }

    @Test
    fun invalidMetadataFallsBack() {
        val snap = parse("""{ "ttlSeconds": 0, "variant": "   " }""")
        assertEquals(RemoteConfigSnapshot.DEFAULT_TTL_SECONDS, snap.ttlSeconds)
        assertEquals("default", snap.variant)
    }

    /**
     * Integer builds, never version strings — `"1.10.0" < "1.9.0"` is true as a string. A build of
     * `0` means the package info couldn't be read, and must never be blocked.
     */
    @Test
    fun updateRequirementComparesIntegerBuilds() {
        val policy = RemoteConfig.UpdatePolicy(minSupportedBuild = 100, recommendedBuild = 120)
        assertEquals(UpdateRequirement.REQUIRED, policy.requirementFor(99))
        assertEquals(UpdateRequirement.RECOMMENDED, policy.requirementFor(100))
        assertEquals(UpdateRequirement.RECOMMENDED, policy.requirementFor(119))
        assertEquals(UpdateRequirement.NONE, policy.requirementFor(120))
        assertEquals(UpdateRequirement.NONE, policy.requirementFor(500))
        assertEquals(UpdateRequirement.NONE, policy.requirementFor(0), "unknown build is never blocked")
        assertEquals(UpdateRequirement.NONE, RemoteConfig.UpdatePolicy().requirementFor(1), "0 = no requirement")
    }

    @Test
    fun stalenessIsMeasuredEitherSideOfTheTtl() {
        val snap = parse(fullDocument, fetchedAt = 1_000L)          // ttl 3600s
        assertFalse(snap.isStale(1_000L))
        assertFalse(snap.isStale(1_000L + 3_599_999))
        assertTrue(snap.isStale(1_000L + 3_600_000))
        assertTrue(RemoteConfigSnapshot.Defaults.isStale(0), "never fetched is always stale")
    }

    @Test
    fun theStoreUrlIsChosenPerPlatform() {
        val policy = RemoteConfig.UpdatePolicy(androidStoreUrl = "play://x", iosStoreUrl = null)
        assertEquals("play://x", policy.storeUrlFor(AppPlatform.ANDROID))
        assertNull(policy.storeUrlFor(AppPlatform.IOS))
    }
}

package com.ferbotz.aurapix.core.config

import com.ferbotz.aurapix.InMemorySettings
import com.ferbotz.aurapix.runHttpTest
import com.ferbotz.aurapix.core.data.prefs.AppPreferences
import com.ferbotz.aurapix.core.data.remote.createHttpClient
import com.russhwolf.settings.Settings
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The cache-then-network behaviour (HOA protocol 11 §5). The rule every case here defends is that
 * **there is always a document and a failure never regresses it** — the app must open offline, on
 * a 5xx, and on a body it can't read.
 */
class RemoteConfigRepositoryTest {

    private val buildInfo = AppBuildInfo(AppPlatform.ANDROID, "1.0.0", 1L)

    private fun body(config: String) =
        """{ "success": true, "data": { "schemaVersion": 1, "variant": "default",
            "revision": "r1", "ttlSeconds": 3600, "config": $config } }"""

    private fun repository(
        settings: Settings,
        now: () -> Long = { 0L },
        handler: MockEngine.Companion.() -> MockEngine,
    ): Pair<RemoteConfigRepositoryImpl, AppPreferences> {
        val prefs = AppPreferences(settings)
        val client = createHttpClient(
            engine = MockEngine.handler(),
            preferences = prefs,
            buildInfo = buildInfo,
        )
        return RemoteConfigRepositoryImpl(RemoteConfigRemoteDataSource(client), prefs, now = now) to prefs
    }

    private fun jsonEngine(vararg bodies: String): MockEngine.Companion.() -> MockEngine = {
        var i = 0
        MockEngine {
            val b = bodies[minOf(i, bodies.lastIndex)]
            i++
            respond(b, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
    }

    @Test
    fun coldStartWithNoCacheUsesDefaultsThenAppliesAndCachesTheFetch() = runHttpTest {
        val (repo, prefs) = repository(
            InMemorySettings(),
            now = { 5_000L },
            handler = jsonEngine(body("""{ "features": { "premium": false } }""")),
        )

        // Before the network answers the app is already running on something.
        assertEquals(RemoteConfigSnapshot.Source.DEFAULTS, repo.snapshot.value.source)
        assertTrue(repo.snapshot.value.config.features.premium)

        val result = repo.refresh()

        assertTrue(result.isSuccess)
        assertEquals(RemoteConfigSnapshot.Source.NETWORK, repo.snapshot.value.source)
        assertFalse(repo.snapshot.value.config.features.premium)
        assertEquals(5_000L, repo.snapshot.value.fetchedAtEpochMillis)
        assertNotNull(prefs.remoteConfigJson, "the document is cached for the next launch")
        assertEquals(5_000L, prefs.remoteConfigFetchedAt)
    }

    @Test
    fun aSecondSessionStartsFromTheCacheWithNoNetworkCall() = runHttpTest {
        val settings = InMemorySettings()
        val (first, _) = repository(settings, handler = jsonEngine(body("""{ "features": { "creations": false } }""")))
        first.refresh()

        // A new instance over the same storage — i.e. the next cold start.
        var calls = 0
        val (second, _) = repository(settings, handler = {
            MockEngine { calls++; respond(body("{}"), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json")) }
        })

        assertEquals(0, calls, "the constructor must not hit the network")
        assertEquals(RemoteConfigSnapshot.Source.CACHE, second.snapshot.value.source)
        assertFalse(second.snapshot.value.config.features.creations, "yesterday's kill switch survives")
    }

    @Test
    fun aFailedFetchKeepsTheCurrentDocument() = runHttpTest {
        val settings = InMemorySettings()
        val (seeded, _) = repository(settings, handler = jsonEngine(body("""{ "features": { "premium": false } }""")))
        seeded.refresh()

        val (repo, _) = repository(settings, handler = { MockEngine { respondError(HttpStatusCode.InternalServerError) } })
        val before = repo.snapshot.value

        val result = repo.refresh()

        assertTrue(result.isFailure)
        assertEquals(before, repo.snapshot.value, "a 5xx is invisible to the user")
        assertFalse(repo.snapshot.value.config.features.premium)
    }

    @Test
    fun aMalformedBodyKeepsTheCurrentDocument() = runHttpTest {
        val (repo, prefs) = repository(InMemorySettings(), handler = {
            MockEngine { respond("not json at all", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json")) }
        })

        val result = repo.refresh()

        assertTrue(result.isFailure)
        assertEquals(RemoteConfigSnapshot.Source.DEFAULTS, repo.snapshot.value.source)
        assertNull(prefs.remoteConfigJson, "an unreadable payload must never poison the cache")
    }

    @Test
    fun anEmptyDocumentStillCountsAsFetched() = runHttpTest {
        val (repo, _) = repository(InMemorySettings(), now = { 9_000L }, handler = jsonEngine(body("{}")))

        repo.refresh()

        assertEquals(RemoteConfigSnapshot.Source.NETWORK, repo.snapshot.value.source)
        assertEquals(RemoteConfig.Default, repo.snapshot.value.config)
        assertEquals(9_000L, repo.snapshot.value.fetchedAtEpochMillis, "the TTL clock starts")
    }

    @Test
    fun refreshIfStaleHonoursTheTtlWhileRefreshIsUnconditional() = runHttpTest {
        var clock = 0L
        var calls = 0
        val (repo, _) = repository(InMemorySettings(), now = { clock }, handler = {
            MockEngine { calls++; respond(body("{}"), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json")) }
        })

        repo.refreshIfStale()
        assertEquals(1, calls, "never fetched counts as stale")

        clock = 3_599_000L
        repo.refreshIfStale()
        assertEquals(1, calls, "inside the TTL, nothing is fetched")

        clock = 3_600_000L
        repo.refreshIfStale()
        assertEquals(2, calls, "past the TTL it re-fetches")

        repo.refresh()
        assertEquals(3, calls, "refresh ignores the TTL entirely")
    }

    @Test
    fun anUnreadableCacheIsIgnoredRatherThanFatal() = runHttpTest {
        val settings = InMemorySettings().apply { putString("remote_config", "{{{ truncated") }
        val (repo, _) = repository(settings, handler = jsonEngine(body("{}")))

        assertEquals(RemoteConfigSnapshot.Source.DEFAULTS, repo.snapshot.value.source)
        assertEquals(RemoteConfig.Default, repo.snapshot.value.config)
    }
}

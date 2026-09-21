package com.ferbotz.aurapix

import com.ferbotz.aurapix.core.config.AppBuildInfo
import com.ferbotz.aurapix.core.config.AppPlatform
import com.ferbotz.aurapix.core.data.prefs.AppPreferences
import com.ferbotz.aurapix.core.data.remote.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * BE-008: the access token no longer expires, so a `401` means it was **rejected** — the signing
 * key rotated, or the stored value isn't one the server issued. Re-authenticating is the only
 * recovery, which makes this the sole path back to a signed-in state. It is pinned here because
 * nothing in the app exercises it until the day the key rotates, and on that day it fires for
 * every user at once.
 */
class UnauthorizedHandlingTest {

    private val buildInfo = AppBuildInfo(AppPlatform.ANDROID, "1.0", 1L)

    @Test
    fun aRejectedTokenDropsTheSession() = runHttpTest {
        var signedOut = false
        val prefs = AppPreferences(InMemorySettings()).apply { authToken = "stale-jwt" }
        val client = createHttpClient(
            engine = MockEngine { respondError(HttpStatusCode.Unauthorized) },
            preferences = prefs,
            buildInfo = buildInfo,
            onUnauthorized = { signedOut = true },
        )

        client.get("profile")
        client.close()

        assertTrue(signedOut, "a 401 on a request that carried a token must drop the session")
    }

    /** A guest hitting a protected route says nothing about the stored session — there isn't one. */
    @Test
    fun anAnonymous401DoesNotDropAnything() = runHttpTest {
        var signedOut = false
        val client = createHttpClient(
            engine = MockEngine { respondError(HttpStatusCode.Unauthorized) },
            preferences = AppPreferences(InMemorySettings()),
            buildInfo = buildInfo,
            onUnauthorized = { signedOut = true },
        )

        client.get("profile")
        client.close()

        assertFalse(signedOut, "no token was sent, so there is no session to drop")
    }

    @Test
    fun anOrdinaryFailureLeavesTheSessionAlone() = runHttpTest {
        var signedOut = false
        val prefs = AppPreferences(InMemorySettings()).apply { authToken = "good-jwt" }
        val client = createHttpClient(
            engine = MockEngine { respondError(HttpStatusCode.InternalServerError) },
            preferences = prefs,
            buildInfo = buildInfo,
            onUnauthorized = { signedOut = true },
        )

        client.get("profile")
        client.close()

        assertFalse(signedOut, "a 5xx is not a rejected token")
        assertEquals("good-jwt", prefs.authToken)
    }

    @Test
    fun aSuccessfulCallDropsNothing() = runHttpTest {
        var signedOut = false
        val prefs = AppPreferences(InMemorySettings()).apply { authToken = "good-jwt" }
        val client = createHttpClient(
            engine = MockEngine {
                respond(
                    """{ "success": true, "data": {} }""",
                    HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, "application/json"),
                )
            },
            preferences = prefs,
            buildInfo = buildInfo,
            onUnauthorized = { signedOut = true },
        )

        client.get("profile")
        client.close()

        assertFalse(signedOut)
    }
}

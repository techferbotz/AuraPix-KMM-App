package com.ferbotz.aurapix

import com.ferbotz.aurapix.core.config.AppBuildInfo
import com.ferbotz.aurapix.core.config.AppPlatform
import com.ferbotz.aurapix.core.data.prefs.AppPreferences
import com.ferbotz.aurapix.core.data.remote.HEADER_APP_BUILD
import com.ferbotz.aurapix.core.data.remote.HEADER_APP_PLATFORM
import com.ferbotz.aurapix.core.data.remote.HEADER_APP_VERSION
import com.ferbotz.aurapix.core.data.remote.HEADER_DEVICE_ID
import com.ferbotz.aurapix.core.data.remote.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * BE-005: every request carries the install id plus the three build headers, so the backend can
 * target this build with remote config later. A build that never sent them can never be placed
 * in a staged rollout, which is why this is pinned rather than left to inspection.
 */
class BuildHeadersTest {

    private val okJson = """{ "success": true, "data": {} }"""

    private fun mockEngine(capture: (Headers) -> Unit) = MockEngine { request ->
        capture(request.headers)
        respond(okJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
    }

    @Test
    fun everyRequestCarriesInstallIdAndBuildHeaders() = runHttpTest {
        var seen: Headers? = null
        val prefs = AppPreferences(InMemorySettings())
        val client = createHttpClient(
            engine = mockEngine { seen = it },
            preferences = prefs,
            buildInfo = AppBuildInfo(AppPlatform.ANDROID, versionName = "1.2.3", buildNumber = 42L),
        )

        client.get("feed")
        client.close()

        val headers = assertNotNull(seen)
        assertEquals(prefs.deviceId, headers[HEADER_DEVICE_ID])
        assertEquals("android", headers[HEADER_APP_PLATFORM])
        assertEquals("1.2.3", headers[HEADER_APP_VERSION])
        assertEquals("42", headers[HEADER_APP_BUILD])
        assertNull(headers[HttpHeaders.Authorization], "anonymous until a token is stored")
    }

    @Test
    fun bearerTokenIsAttachedOnceLoggedIn() = runHttpTest {
        var seen: Headers? = null
        val prefs = AppPreferences(InMemorySettings()).apply { authToken = "jwt-1" }
        val client = createHttpClient(
            engine = mockEngine { seen = it },
            preferences = prefs,
            buildInfo = AppBuildInfo(AppPlatform.IOS, versionName = "2.0", buildNumber = 7L),
        )

        client.get("profile")
        client.close()

        val headers = assertNotNull(seen)
        assertEquals("Bearer jwt-1", headers[HttpHeaders.Authorization])
        assertEquals("ios", headers[HEADER_APP_PLATFORM])
        assertEquals("7", headers[HEADER_APP_BUILD])
        assertEquals(prefs.deviceId, headers[HEADER_DEVICE_ID])
    }

    @Test
    fun theInstallIdIsStableAcrossRequestsAndSurvivesLogout() = runHttpTest {
        val prefs = AppPreferences(InMemorySettings()).apply { authToken = "jwt-1" }
        val first = prefs.deviceId

        prefs.clearSession()

        assertEquals(first, prefs.deviceId, "logging out must not re-bucket this install")
    }
}

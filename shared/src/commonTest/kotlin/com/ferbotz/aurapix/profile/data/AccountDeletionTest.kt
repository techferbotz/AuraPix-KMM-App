package com.ferbotz.aurapix.profile.data

import com.ferbotz.aurapix.InMemorySettings
import com.ferbotz.aurapix.core.config.AppBuildInfo
import com.ferbotz.aurapix.core.config.AppPlatform
import com.ferbotz.aurapix.core.data.prefs.AppPreferences
import com.ferbotz.aurapix.core.data.remote.ApiError
import com.ferbotz.aurapix.core.data.remote.createHttpClient
import com.ferbotz.aurapix.profile.ui.playSubscriptionsUrl
import com.ferbotz.aurapix.runHttpTest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * BE-010 / API.md §4.2a: how each answer to `DELETE /delete-account` is read. The endpoint
 * destroys an account, so none of these paths can be tried casually against the real server —
 * which is why they are pinned here. The client is wired the way `DataModule` wires it, so a 401
 * also drops the session in the HTTP layer, as it does in the app.
 */
class AccountDeletionTest {

    @Test
    fun a200DeletesAndSignsOut() = runHttpTest {
        val s = signedInSession { json(HttpStatusCode.OK, """{ "success": true, "data": { "deleted": true, "deletedAt": "2026-09-24T10:15:30.000Z" } }""") }

        assertEquals(AccountDeletion.Deleted, s.userManager.deleteAccount())
        assertNull(s.prefs.authToken, "the token must not outlive the account")
        assertFalse(s.userManager.current.isLoggedIn)
        assertEquals(0, s.userManager.current.credits)
    }

    @Test
    fun itSendsOneAuthenticatedDeleteWithNoBody() = runHttpTest {
        val s = signedInSession { json(HttpStatusCode.OK, """{ "success": true, "data": { "deleted": true } }""") }

        s.userManager.deleteAccount()

        val request = s.requests.single()
        assertEquals(HttpMethod.Delete, request.method)
        assertEquals("/api/v1/delete-account", request.url.encodedPath)
        assertEquals("Bearer jwt", request.headers[HttpHeaders.Authorization])
        assertEquals(0L, request.body.contentLength)
    }

    /** A repeated call, or a retry after a lost response: the account is already gone. */
    @Test
    fun unauthorizedMeansAlreadyDeleted() = runHttpTest {
        val s = signedInSession { json(HttpStatusCode.Unauthorized, """{ "success": false, "errorCode": "UNAUTHORIZED", "message": "Unauthorized" }""") }

        assertEquals(AccountDeletion.Deleted, s.userManager.deleteAccount())
        assertNull(s.prefs.authToken)
    }

    @Test
    fun invalidTokenDeletesNothingAndAsksForASignIn() = runHttpTest {
        val s = signedInSession { json(HttpStatusCode.Unauthorized, """{ "success": false, "errorCode": "INVALID_TOKEN", "message": "Invalid token" }""") }

        assertEquals(AccountDeletion.SignInRequired, s.userManager.deleteAccount())
        assertNull(s.prefs.authToken, "the rejected token is dropped, so signing in again is possible")
    }

    @Test
    fun aServerErrorKeepsTheSessionForARetry() = runHttpTest {
        val s = signedInSession { json(HttpStatusCode.InternalServerError, """{ "success": false, "errorCode": "INTERNAL_ERROR", "message": "boom" }""") }

        assertEquals(AccountDeletion.Failed(ApiError.InternalError), s.userManager.deleteAccount())
        assertEquals("jwt", s.prefs.authToken, "nothing was deleted, so the user has to be able to retry")
        assertTrue(s.userManager.current.isLoggedIn)
    }

    /** The outcome is unknown; keeping the token is what lets the retry find out. */
    @Test
    fun aNetworkFailureKeepsTheSessionForARetry() = runHttpTest {
        val s = signedInSession { throw IllegalStateException("connection reset") }

        val outcome = s.userManager.deleteAccount()

        assertIs<AccountDeletion.Failed>(outcome)
        assertIs<ApiError.NetworkError>(outcome.error)
        assertEquals("jwt", s.prefs.authToken)
    }

    /** Without a token the server's 401 UNAUTHORIZED would read as "already deleted". */
    @Test
    fun signedOutSendsNothing() = runHttpTest {
        val s = signedInSession { json(HttpStatusCode.Unauthorized, """{ "success": false, "errorCode": "UNAUTHORIZED", "message": "Unauthorized" }""") }
        s.userManager.logout()

        assertEquals(AccountDeletion.SignInRequired, s.userManager.deleteAccount())
        assertTrue(s.requests.isEmpty())
    }

    @Test
    fun playLinkIsTheOneTheContractGives() {
        assertEquals(
            "https://play.google.com/store/account/subscriptions?sku=aurapix_premium&package=com.ferbotz.aurapix",
            playSubscriptionsUrl("aurapix_premium"),
        )
        assertEquals("https://play.google.com/store/account/subscriptions", playSubscriptionsUrl(null))
    }

    private class Session(
        val userManager: UserManager,
        val prefs: AppPreferences,
        val requests: List<HttpRequestData>,
    )

    /** A signed-in user with gems and Premium, over a server that answers the delete with [onDelete]. */
    private fun signedInSession(
        onDelete: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): Session {
        val prefs = AppPreferences(InMemorySettings()).apply {
            authToken = "jwt"
            userId = "user-1"
            cachedCredits = 120
            subscriptionStatus = "ACTIVE"
        }
        val requests = mutableListOf<HttpRequestData>()
        lateinit var userManager: UserManager
        val client = createHttpClient(
            engine = MockEngine { request ->
                if (request.url.encodedPath.endsWith("/delete-account")) {
                    requests += request
                    onDelete(request)
                } else {
                    // UserManager warms the session with GET /profile when built; keep that inert.
                    respondError(HttpStatusCode.ServiceUnavailable)
                }
            },
            preferences = prefs,
            buildInfo = AppBuildInfo(AppPlatform.ANDROID, "1.0", 1L),
            onUnauthorized = { userManager.logout() },
        )
        userManager = UserManager(ProfileRemoteDataSource(client), prefs)
        return Session(userManager, prefs, requests)
    }

    private fun MockRequestHandleScope.json(status: HttpStatusCode, body: String): HttpResponseData =
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
}

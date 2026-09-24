package com.ferbotz.aurapix.creation.ui

import com.ferbotz.aurapix.InMemorySettings
import com.ferbotz.aurapix.core.config.AppBuildInfo
import com.ferbotz.aurapix.core.config.AppPlatform
import com.ferbotz.aurapix.core.data.prefs.AppPreferences
import com.ferbotz.aurapix.core.data.remote.ApiError
import com.ferbotz.aurapix.core.data.remote.createHttpClient
import com.ferbotz.aurapix.core.ui.base.UiState
import com.ferbotz.aurapix.creation.data.CreationRemoteDataSource
import com.ferbotz.aurapix.creation.data.CreationsRepository
import com.ferbotz.aurapix.creation.data.FakeCreationDao
import com.ferbotz.aurapix.creation.data.creationsPage
import com.ferbotz.aurapix.runHttpTest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * The failure screen's way out, and My Creations' paging, driven through the real view models
 * over a scripted server. View models run on Main, so Main is made immediate for the test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GenerationFlowTest {

    @BeforeTest
    fun useImmediateMain() = Dispatchers.setMain(Dispatchers.Unconfined)

    @AfterTest
    fun restoreMain() = Dispatchers.resetMain()

    /** Re-polling a FAILED creation only returns the same failure: retry must generate anew. */
    @Test
    fun retryAfterAFailedRunStartsANewGeneration() = runHttpTest {
        val server = Server(onPoll = { id -> if (id == "c1") failed(id, "No face found") else completed(id) })
        val vm = GenerationViewModel(server.repository())

        vm.generate("t1", listOf(byteArrayOf(1)))
        val failure = withTimeout(10_000) { vm.failure.first { it != null } }
        assertEquals(GenerationFailure.Failed("c1", "No face found"), failure)

        vm.retry()
        val done = withTimeout(10_000) { vm.state.first { it is UiState.Success } }
        assertEquals("COMPLETED", (done as UiState.Success).data.status)
        assertEquals(2, server.posts, "a failed run is retried with a new generation")
        vm.onCleared()
    }

    /** It started but the check failed: retrying re-polls the same creation and never pays twice. */
    @Test
    fun retryAfterALostConnectionChecksTheSameCreationAgain() = runHttpTest {
        var firstPoll = true
        val server = Server(onPoll = { id ->
            if (firstPoll) { firstPoll = false; throw IllegalStateException("connection reset") }
            completed(id)
        })
        val vm = GenerationViewModel(server.repository())

        vm.generate("t1", listOf(byteArrayOf(1)))
        val failure = withTimeout(10_000) { vm.failure.first { it != null } }
        assertIs<GenerationFailure.Unconfirmed>(failure)
        assertEquals("c1", failure.creationId)

        vm.retry()
        withTimeout(10_000) { vm.state.first { it is UiState.Success } }
        assertEquals(1, server.posts, "checking again must not start a second generation")
        assertEquals(listOf("c1", "c1"), server.polls)
        vm.onCleared()
    }

    @Test
    fun aRefusedRequestNeverStarted() = runHttpTest {
        val server = Server(onGenerate = {
            respond(
                """{ "success": false, "errorCode": "TEMPLATE_DISABLED", "message": "Template disabled." }""",
                HttpStatusCode.Conflict,
                headersOf(HttpHeaders.ContentType, "application/json"),
            )
        })
        val vm = GenerationViewModel(server.repository())

        vm.generate("t1", listOf(byteArrayOf(1)))
        val failure = withTimeout(10_000) { vm.failure.first { it != null } }
        assertEquals(GenerationFailure.NotStarted(ApiError.TemplateDisabled), failure)
        assertTrue(server.polls.isEmpty())
        vm.onCleared()
    }

    @Test
    fun myCreationsLoadsTheNextPageOnlyWhileThereIsOne() = runHttpTest {
        val server = Server(pages = mapOf(
            1 to creationsPage(1, hasMore = true, "c4" to 4, "c3" to 3),
            2 to creationsPage(2, hasMore = false, "c2" to 2, "c1" to 1),
        ))
        val vm = HistoryViewModel(server.repository())

        val first = withTimeout(10_000) { vm.state.first { (it as? UiState.Success)?.data?.loads == 1 } }
        assertEquals(listOf("c4", "c3"), (first as UiState.Success).data.items.map { it.id })
        assertTrue(first.data.hasMore)

        vm.loadMore()
        val second = withTimeout(10_000) { vm.state.first { (it as? UiState.Success)?.data?.loads == 2 } }
        assertEquals(listOf("c4", "c3", "c2", "c1"), (second as UiState.Success).data.items.map { it.id })
        assertFalse(second.data.hasMore)

        vm.loadMore()
        assertEquals(listOf(1, 2), server.pagesRequested, "nothing is requested past the last page")
        vm.onCleared()
    }

    /** A scripted AuraPix: each POST /generate starts creation c1, c2, …; polls answer [onPoll]. */
    private class Server(
        val onGenerate: (MockRequestHandleScope.() -> HttpResponseData)? = null,
        val onPoll: MockRequestHandleScope.(id: String) -> HttpResponseData = { completed(it) },
        val pages: Map<Int, String> = emptyMap(),
    ) {
        var posts = 0
        val polls = mutableListOf<String>()
        val pagesRequested = mutableListOf<Int>()

        fun repository() = CreationsRepository(
            CreationRemoteDataSource(
                createHttpClient(
                    engine = MockEngine { request ->
                        val path = request.url.encodedPath
                        when {
                            request.method == HttpMethod.Post && path.endsWith("/generate") -> {
                                posts++
                                onGenerate?.invoke(this) ?: json(
                                    """{ "success": true, "data": { "creationId": "c$posts", "status": "PROCESSING" } }""",
                                )
                            }
                            path.endsWith("/creations") -> {
                                val page = request.url.parameters["page"]!!.toInt()
                                pagesRequested += page
                                json(pages.getValue(page))
                            }
                            path.contains("/creations/") -> {
                                val id = path.substringAfterLast('/')
                                polls += id
                                onPoll(id)
                            }
                            else -> respondError(HttpStatusCode.NotFound)
                        }
                    },
                    preferences = AppPreferences(InMemorySettings()).apply { authToken = "jwt" },
                    buildInfo = AppBuildInfo(AppPlatform.ANDROID, "1.0", 1L),
                ),
            ),
            FakeCreationDao(),
        )
    }
}

private fun MockRequestHandleScope.json(body: String): HttpResponseData =
    respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))

private fun MockRequestHandleScope.completed(id: String) = json(detail(id, "COMPLETED", null))

private fun MockRequestHandleScope.failed(id: String, reason: String) = json(detail(id, "FAILED", reason))

private fun detail(id: String, status: String, reason: String?): String {
    val failureReason = reason?.let { "\"$it\"" } ?: "null"
    return """{ "success": true, "data": { "id": "$id", "status": "$status", "generatedImageUrl": null,
        "templateTitleSnapshot": "Template", "createdAt": "2026-09-24T10:00:00.000Z", "templateId": "t1",
        "templateVersionId": "v1", "failureReason": $failureReason, "updatedAt": "2026-09-24T10:00:05.000Z" } }"""
}

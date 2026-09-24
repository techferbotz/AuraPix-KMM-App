package com.ferbotz.aurapix.creation.data

import com.ferbotz.aurapix.InMemorySettings
import com.ferbotz.aurapix.core.config.AppBuildInfo
import com.ferbotz.aurapix.core.config.AppPlatform
import com.ferbotz.aurapix.core.data.prefs.AppPreferences
import com.ferbotz.aurapix.core.data.remote.createHttpClient
import com.ferbotz.aurapix.runHttpTest
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * My Creations keeps every page it has loaded in the cache. These pin when the cache is replaced,
 * kept, or trimmed, since getting it wrong either shows someone else's creations or collapses the
 * list under the user's thumb each time they come back to it.
 */
class CreationPagingTest {

    /** Coming back to the list: page 1 is unchanged, so the pages below it stay. */
    @Test
    fun aFirstPageThatAgreesWithTheCacheKeepsLaterPages() = runHttpTest {
        val dao = FakeCreationDao(cachedCreation("c4", 4), cachedCreation("c3", 3), cachedCreation("c2", 2), cachedCreation("c1", 1))
        val repo = repository(dao, 1 to creationsPage(1, hasMore = true, "c4" to 4, "c3" to 3))

        assertTrue(repo.loadCreationsPage(1).getOrThrow())
        assertEquals(listOf("c4", "c3", "c2", "c1"), dao.ids)
    }

    /** Another account's rows, or creations the server no longer has, are not the user's list. */
    @Test
    fun aFirstPageThatDisagreesReplacesTheCache() = runHttpTest {
        val dao = FakeCreationDao(cachedCreation("x2", 20), cachedCreation("x1", 10))
        val repo = repository(dao, 1 to creationsPage(1, hasMore = true, "c2" to 2, "c1" to 1))

        repo.loadCreationsPage(1)
        assertEquals(listOf("c2", "c1"), dao.ids)
    }

    @Test
    fun laterPagesAddToTheCache() = runHttpTest {
        val dao = FakeCreationDao(cachedCreation("c4", 4), cachedCreation("c3", 3))
        val repo = repository(dao, 2 to creationsPage(2, hasMore = true, "c2" to 2, "c1" to 1))

        assertTrue(repo.loadCreationsPage(2).getOrThrow())
        assertEquals(listOf("c4", "c3", "c2", "c1"), dao.ids)
    }

    /** Past the last page the server has nothing, so nothing cached below it is real. */
    @Test
    fun theLastPageDropsWhatTheServerNoLongerHas() = runHttpTest {
        val dao = FakeCreationDao(cachedCreation("c4", 4), cachedCreation("c3", 3), cachedCreation("c2", 2), cachedCreation("c1", 1))
        val repo = repository(dao, 2 to creationsPage(2, hasMore = false, "c3" to 3))

        assertFalse(repo.loadCreationsPage(2).getOrThrow())
        assertEquals(listOf("c4", "c3"), dao.ids)
    }

    @Test
    fun anEmptyFirstPageEmptiesTheCache() = runHttpTest {
        val dao = FakeCreationDao(cachedCreation("x1", 10))
        val repo = repository(dao, 1 to creationsPage(1, hasMore = false))

        repo.loadCreationsPage(1)
        assertEquals(emptyList(), dao.ids)
    }

    @Test
    fun aFailedPageLeavesTheCacheAlone() = runHttpTest {
        val dao = FakeCreationDao(cachedCreation("c1", 1))
        val repo = repository(dao)

        assertTrue(repo.loadCreationsPage(1).isFailure)
        assertEquals(listOf("c1"), dao.ids)
    }

    /** A repository over [dao] whose server answers `GET /creations?page=N` from [pages], else 500. */
    private fun repository(dao: FakeCreationDao, vararg pages: Pair<Int, String>): CreationsRepository {
        val byNumber = pages.toMap()
        return CreationsRepository(CreationRemoteDataSource(client(byNumber)), dao)
    }

    private fun client(pages: Map<Int, String>): HttpClient = createHttpClient(
        engine = MockEngine { request ->
            val body = pages[request.url.parameters["page"]?.toInt()]
            if (body == null) respondError(HttpStatusCode.InternalServerError)
            else respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        },
        preferences = AppPreferences(InMemorySettings()).apply { authToken = "jwt" },
        buildInfo = AppBuildInfo(AppPlatform.ANDROID, "1.0", 1L),
    )
}

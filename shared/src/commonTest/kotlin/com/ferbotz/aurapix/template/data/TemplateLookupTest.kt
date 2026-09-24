package com.ferbotz.aurapix.template.data

import com.ferbotz.aurapix.InMemorySettings
import com.ferbotz.aurapix.core.config.AppBuildInfo
import com.ferbotz.aurapix.core.config.AppPlatform
import com.ferbotz.aurapix.core.data.prefs.AppPreferences
import com.ferbotz.aurapix.core.data.remote.createHttpClient
import com.ferbotz.aurapix.runHttpTest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.toList
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `aurapix.ferbotz.com/template/…` links carry either the template's id or its slug, and only
 * the id works on `GET /templates/{id}` — a slug there is a 404. §4.9a is the slug route.
 */
class TemplateLookupTest {

    @Test
    fun anIdUsesTheIdRoute() = runHttpTest {
        assertEquals(
            listOf("/api/v1/templates/8f6be922-7f0a-4c95-95f5-52f21956a098"),
            lookUp("8f6be922-7f0a-4c95-95f5-52f21956a098"),
        )
    }

    @Test
    fun aSlugUsesTheSlugRoute() = runHttpTest {
        assertEquals(listOf("/api/v1/templates/slug/mountain-base-camp-trek"), lookUp("mountain-base-camp-trek"))
    }

    /** Slugs come from links anyone can craft; one must not reach past its own path segment. */
    @Test
    fun aSlugStaysOnePathSegment() = runHttpTest {
        assertEquals(listOf("/api/v1/templates/slug/a%20b%3F"), lookUp("a b?"))
    }

    /** Looks the template up and returns the request paths the server saw. */
    private suspend fun lookUp(ref: String): List<String> {
        val paths = mutableListOf<String>()
        val client = createHttpClient(
            engine = MockEngine { request ->
                paths += request.url.encodedPath
                respond(
                    """{ "success": false, "errorCode": "TEMPLATE_NOT_FOUND", "message": "Template not found." }""",
                    HttpStatusCode.NotFound,
                    headersOf(HttpHeaders.ContentType, "application/json"),
                )
            },
            preferences = AppPreferences(InMemorySettings()),
            buildInfo = AppBuildInfo(AppPlatform.ANDROID, "1.0", 1L),
        )
        TemplatesRepository(TemplateRemoteDataSource(client)).getTemplate(ref).toList()
        client.close()
        return paths
    }
}

package com.ferbotz.aurapix.core.data.remote

import co.touchlab.kermit.Logger as KermitLogger
import com.ferbotz.aurapix.core.config.AppBuildInfo
import com.ferbotz.aurapix.core.config.appBuildInfo
import com.ferbotz.aurapix.core.data.prefs.AppPreferences
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

const val AURA_BASE_URL = "https://aurapix.ferbotz.com/api/v1/"
//const val AURA_BASE_URL = "http://192.168.0.3:8080/api/v1/"
const val AURA_HEALTH_URL = "https://aurapix.ferbotz.com/health"

/** `GET /config` is public and **unversioned** — it does not live under `/api/v1` (BE-005). */
const val AURA_CONFIG_URL = "https://aurapix.ferbotz.com/config"

// The names are the shared House of Apps contract (protocol 10 §1) — keep these spellings exactly.
const val HEADER_DEVICE_ID = "X-Device-Id"
const val HEADER_APP_PLATFORM = "X-App-Platform"
const val HEADER_APP_VERSION = "X-App-Version"
const val HEADER_APP_BUILD = "X-App-Build"

val auraJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
}

/** Kermit logger for all networking. Logs to Logcat (Android) and the device console (iOS). */
val httpLogger: KermitLogger = KermitLogger.withTag("AuraPix-HTTP")

/**
 * @param onUnauthorized invoked when a request that carried a token comes back `401`. Since
 *   BE-008 the access token has no expiry, so this no longer means "aged out" — the token was
 *   rejected, in practice because the server rotated its signing key. Re-authenticating is the
 *   only recovery, and it is the caller's job to drop the session.
 */
fun createHttpClient(
    engine: HttpClientEngine = defaultHttpEngine(),
    preferences: AppPreferences,
    buildInfo: AppBuildInfo = appBuildInfo(),
    onUnauthorized: () -> Unit = {},
): HttpClient {
    val client = HttpClient(engine) {
        // Do NOT set expectSuccess=true — we must read the body on 4xx/5xx to parse errorCode.
        install(ContentNegotiation) { json(auraJson) }
        install(Logging) {
            // Pipe Ktor's request/response logging through Kermit.
            logger = object : Logger {
                override fun log(message: String) {
                    httpLogger.d { message }
                }
            }
            // ALL → request line, all headers, and request/response bodies…
            level = LogLevel.ALL
            // …except the bearer token. It never expires now (BE-008), so anything that reads a
            // log line holds a permanent credential rather than one that dies within the month.
            sanitizeHeader { it == HttpHeaders.Authorization }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
        }
        defaultRequest {
            url(AURA_BASE_URL)
            // Who is calling, on EVERY request rather than just /config: the backend targets
            // remote config on these, and a build that never sent them can never be placed in a
            // staged rollout or experiment later. Read per request, so an id generated on first
            // launch is picked up immediately.
            header(HEADER_DEVICE_ID, preferences.deviceId)
            header(HEADER_APP_PLATFORM, buildInfo.platform.wire)
            header(HEADER_APP_VERSION, buildInfo.versionName)
            header(HEADER_APP_BUILD, buildInfo.buildNumber.toString())
        }
        install(HttpSend)
    }
    // Ktor 3: intercept is called on the plugin instance after client creation.
    client.plugin(HttpSend).intercept { request ->
        val token = preferences.authToken
        if (token != null) {
            request.headers.append(HttpHeaders.Authorization, "Bearer $token")
            httpLogger.d { "→ ${request.method.value} ${request.url.buildString()} | (authenticated)" }
        } else {
            httpLogger.d { "→ ${request.method.value} ${request.url.buildString()} | (no auth token — guest)" }
        }
        val call = execute(request)
        // Only when we actually sent a token: an anonymous 401 says nothing about the session,
        // and signing out over one would be a bug of its own.
        if (token != null && call.response.status == HttpStatusCode.Unauthorized) {
            httpLogger.w { "401 on ${request.url.buildString()} with a stored token — dropping the session" }
            onUnauthorized()
        }
        call
    }
    return client
}

expect fun defaultHttpEngine(): HttpClientEngine

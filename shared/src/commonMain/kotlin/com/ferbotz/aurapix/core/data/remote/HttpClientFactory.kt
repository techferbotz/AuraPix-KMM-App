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

fun createHttpClient(
    engine: HttpClientEngine = defaultHttpEngine(),
    preferences: AppPreferences,
    buildInfo: AppBuildInfo = appBuildInfo(),
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
            // ALL → request line, all headers (incl. Authorization), and request/response bodies.
            level = LogLevel.ALL
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
            httpLogger.d { "→ ${request.method.value} ${request.url.buildString()} | Authorization: Bearer $token" }
        } else {
            httpLogger.d { "→ ${request.method.value} ${request.url.buildString()} | (no auth token — guest)" }
        }
        execute(request)
    }
    return client
}

expect fun defaultHttpEngine(): HttpClientEngine

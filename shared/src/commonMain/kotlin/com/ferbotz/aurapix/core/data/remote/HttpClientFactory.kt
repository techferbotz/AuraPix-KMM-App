package com.ferbotz.aurapix.core.data.remote

import co.touchlab.kermit.Logger as KermitLogger
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
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

const val AURA_BASE_URL = "http://13.205.128.80/api/v1/"
const val AURA_HEALTH_URL = "http://13.205.128.80/health"

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
        defaultRequest { url(AURA_BASE_URL) }
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

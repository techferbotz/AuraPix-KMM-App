package com.ferbotz.aurapix.data.remote

import com.ferbotz.aurapix.data.prefs.AppPreferences
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
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

fun createHttpClient(
    engine: HttpClientEngine = defaultHttpEngine(),
    preferences: AppPreferences,
): HttpClient {
    val client = HttpClient(engine) {
        // Do NOT set expectSuccess=true — we must read the body on 4xx/5xx to parse errorCode.
        install(ContentNegotiation) { json(auraJson) }
        install(Logging) { level = LogLevel.INFO }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
        }
        defaultRequest { url(AURA_BASE_URL) }
        install(HttpSend)
    }
    // Ktor 3: intercept is called on the plugin instance after client creation.
    client.plugin(HttpSend).intercept { request ->
        preferences.authToken?.let { token ->
            request.headers.append(HttpHeaders.Authorization, "Bearer $token")
        }
        execute(request)
    }
    return client
}

expect fun defaultHttpEngine(): HttpClientEngine

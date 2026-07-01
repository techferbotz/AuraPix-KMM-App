package com.ferbotz.aurapix.profile.data

import com.ferbotz.aurapix.core.data.remote.safeApiCall
import com.ferbotz.aurapix.profile.data.dto.AuthResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthRemoteDataSource(private val client: HttpClient) {

    /** §4.1 — exchange a Google ID token for the app JWT. */
    suspend fun googleAuth(idToken: String): Result<AuthResponseDto> =
        safeApiCall {
            client.post("auth/google") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("idToken" to idToken))
            }
        }
}

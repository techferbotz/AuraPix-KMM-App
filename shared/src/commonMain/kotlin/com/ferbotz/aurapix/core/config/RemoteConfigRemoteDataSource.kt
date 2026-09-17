package com.ferbotz.aurapix.core.config

import com.ferbotz.aurapix.core.data.remote.AURA_CONFIG_URL
import com.ferbotz.aurapix.core.data.remote.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.url

/**
 * `GET /config` — the remote config document resolved for this install and build.
 *
 * The route is **unversioned**, so it takes an absolute URL rather than the client's `/api/v1/`
 * base. It needs no identity: a first launch before sign-in, an expired token or a build sending
 * no headers all get a valid document. The targeting headers ride on every request anyway
 * (see the HTTP client factory), so there is nothing to attach here.
 */
class RemoteConfigRemoteDataSource(private val client: HttpClient) {

    suspend fun getConfig(): Result<RemoteConfigResponseDto> =
        safeApiCall { client.get { url(AURA_CONFIG_URL) } }
}

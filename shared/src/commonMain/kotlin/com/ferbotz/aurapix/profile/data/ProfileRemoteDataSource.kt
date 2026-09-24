package com.ferbotz.aurapix.profile.data

import com.ferbotz.aurapix.core.data.remote.safeApiCall
import com.ferbotz.aurapix.profile.data.dto.CreditsDto
import com.ferbotz.aurapix.profile.data.dto.DeleteAccountDto
import com.ferbotz.aurapix.profile.data.dto.ProfileDto
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get

class ProfileRemoteDataSource(private val client: HttpClient) {

    /** §4.2 — profile header (identity + credits + subscription). */
    suspend fun getProfile(): Result<ProfileDto> =
        safeApiCall { client.get("profile") }

    /** §4.3 — lightweight credit balance. */
    suspend fun getCredits(): Result<CreditsDto> =
        safeApiCall { client.get("credits") }

    /** §4.2a — permanently deletes the signed-in account. No body, no undo. */
    suspend fun deleteAccount(): Result<DeleteAccountDto> =
        safeApiCall { client.delete("delete-account") }
}

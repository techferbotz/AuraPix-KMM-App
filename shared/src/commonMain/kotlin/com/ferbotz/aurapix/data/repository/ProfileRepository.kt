package com.ferbotz.aurapix.data.repository

import com.ferbotz.aurapix.data.prefs.AppPreferences
import com.ferbotz.aurapix.data.remote.AuraApi
import com.ferbotz.aurapix.data.remote.dto.CreditsDto
import com.ferbotz.aurapix.data.remote.dto.ProfileDto

class ProfileRepository(
    private val api: AuraApi,
    private val preferences: AppPreferences,
) {
    suspend fun getProfile(): Result<ProfileDto> {
        val result = api.getProfile()
        result.onSuccess { preferences.cachedCredits = it.credits.totalCredits }
        return result
    }

    suspend fun getCredits(): Result<CreditsDto> {
        val result = api.getCredits()
        result.onSuccess { preferences.cachedCredits = it.totalCredits }
        return result
    }

    fun getCachedCredits(): Int = preferences.cachedCredits
}

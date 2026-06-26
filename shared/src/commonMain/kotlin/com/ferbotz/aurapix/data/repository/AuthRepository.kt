package com.ferbotz.aurapix.data.repository

import com.ferbotz.aurapix.data.prefs.AppPreferences
import com.ferbotz.aurapix.data.remote.AuraApi
import com.ferbotz.aurapix.data.remote.dto.UserSummaryDto

class AuthRepository(
    private val api: AuraApi,
    private val preferences: AppPreferences,
) {
    val isLoggedIn: Boolean get() = preferences.isLoggedIn

    suspend fun loginWithGoogle(idToken: String): Result<UserSummaryDto> {
        val result = api.googleAuth(idToken)
        result.onSuccess { auth ->
            preferences.authToken = auth.accessToken
        }
        return result.map { it.user }
    }

    fun logout() {
        preferences.authToken = null
        preferences.cachedCredits = 0
    }
}

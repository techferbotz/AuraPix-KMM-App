package com.ferbotz.aurapix.profile.data

import com.ferbotz.aurapix.core.data.DataState
import com.ferbotz.aurapix.core.data.prefs.AppPreferences
import com.ferbotz.aurapix.core.data.remote.asApiError
import com.ferbotz.aurapix.profile.data.dto.UserSummaryDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AuthRepository(
    private val remote: AuthRemoteDataSource,
    private val profileRemote: ProfileRemoteDataSource,
    private val prefs: AppPreferences,
) {
    val isLoggedIn: Boolean get() = prefs.isLoggedIn

    /**
     * Exchanges the Google ID token for our JWT, then hydrates the session from GET /profile
     * (identity + credits + subscription). A failed profile fetch does NOT fail the login — we
     * keep the basic user from the auth response and let screens refresh later.
     */
    fun loginWithGoogle(idToken: String): Flow<DataState<UserSummaryDto>> = flow {
        emit(DataState.Loading)

        val auth = remote.googleAuth(idToken).fold(
            onSuccess = { it },
            onFailure = { emit(DataState.Error(it.asApiError())); null },
        ) ?: return@flow

        prefs.authToken = auth.accessToken
        cacheUser(auth.user.id, auth.user.displayName, auth.user.email, auth.user.profileImageUrl)

        // Hydrate credits + subscription (+ canonical identity) from /profile.
        profileRemote.getProfile().onSuccess { profile ->
            cacheUser(profile.id, profile.displayName, profile.email, profile.profileImageUrl)
            prefs.cachedCredits = profile.credits.totalCredits
            prefs.subscriptionStatus = profile.subscription.status
        }

        emit(DataState.Success(auth.user))
    }.flowOn(Dispatchers.Default)

    fun logout() {
        prefs.clearSession()
    }

    private fun cacheUser(id: String, name: String, email: String, avatarUrl: String?) {
        prefs.userId = id
        prefs.displayName = name
        prefs.email = email
        prefs.avatarUrl = avatarUrl
    }
}

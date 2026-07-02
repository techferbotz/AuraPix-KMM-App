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
    private val prefs: AppPreferences,
) {
    val isLoggedIn: Boolean get() = prefs.isLoggedIn

    /**
     * Exchanges the Google ID token for our JWT and stores it. The caller (LoginViewModel) then
     * asks `UserManager` to hydrate the full session (identity + credits + subscription) from /profile.
     */
    fun loginWithGoogle(idToken: String): Flow<DataState<UserSummaryDto>> = flow {
        emit(DataState.Loading)
        remote.googleAuth(idToken).fold(
            onSuccess = {
                prefs.authToken = it.accessToken
                emit(DataState.Success(it.user))
            },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)

    fun logout() {
        prefs.clearSession()
    }
}

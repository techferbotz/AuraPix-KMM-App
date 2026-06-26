package com.ferbotz.aurapix.data.repository

import com.ferbotz.aurapix.data.common.DataState
import com.ferbotz.aurapix.data.prefs.AppPreferences
import com.ferbotz.aurapix.data.remote.AuraApi
import com.ferbotz.aurapix.data.remote.asApiError
import com.ferbotz.aurapix.data.remote.dto.UserSummaryDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AuthRepository(
    private val api: AuraApi,
    private val prefs: AppPreferences,
) {
    val isLoggedIn: Boolean get() = prefs.isLoggedIn

    fun loginWithGoogle(idToken: String): Flow<DataState<UserSummaryDto>> = flow {
        emit(DataState.Loading)
        api.googleAuth(idToken).fold(
            onSuccess = {
                prefs.authToken = it.accessToken
                emit(DataState.Success(it.user))
            },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)

    fun logout() {
        prefs.authToken = null
        prefs.cachedCredits = 0
    }
}

package com.ferbotz.aurapix.data.repository

import com.ferbotz.aurapix.data.common.DataState
import com.ferbotz.aurapix.data.prefs.AppPreferences
import com.ferbotz.aurapix.data.remote.AuraApi
import com.ferbotz.aurapix.data.remote.asApiError
import com.ferbotz.aurapix.data.remote.dto.CreditsDto
import com.ferbotz.aurapix.data.remote.dto.ProfileDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ProfileRepository(
    private val api: AuraApi,
    private val prefs: AppPreferences,
) {
    val cachedCredits: Int get() = prefs.cachedCredits

    fun getProfile(): Flow<DataState<ProfileDto>> = flow {
        emit(DataState.Loading)
        api.getProfile().fold(
            onSuccess = {
                prefs.cachedCredits = it.credits.totalCredits
                emit(DataState.Success(it))
            },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)

    fun getCredits(): Flow<DataState<CreditsDto>> = flow {
        emit(DataState.Loading)
        api.getCredits().fold(
            onSuccess = {
                prefs.cachedCredits = it.totalCredits
                emit(DataState.Success(it))
            },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)
}

package com.ferbotz.aurapix.profile.data

import com.ferbotz.aurapix.core.data.DataState
import com.ferbotz.aurapix.core.data.prefs.AppPreferences
import com.ferbotz.aurapix.core.data.remote.asApiError
import com.ferbotz.aurapix.profile.data.dto.CreditsDto
import com.ferbotz.aurapix.profile.data.dto.ProfileDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ProfileRepository(
    private val remote: ProfileRemoteDataSource,
    private val prefs: AppPreferences,
) {
    val cachedCredits: Int get() = prefs.cachedCredits

    fun getProfile(): Flow<DataState<ProfileDto>> = flow {
        emit(DataState.Loading)
        remote.getProfile().fold(
            onSuccess = {
                prefs.cachedCredits = it.credits.totalCredits
                emit(DataState.Success(it))
            },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)

    fun getCredits(): Flow<DataState<CreditsDto>> = flow {
        emit(DataState.Loading)
        remote.getCredits().fold(
            onSuccess = {
                prefs.cachedCredits = it.totalCredits
                emit(DataState.Success(it))
            },
            onFailure = { emit(DataState.Error(it.asApiError())) },
        )
    }.flowOn(Dispatchers.Default)
}

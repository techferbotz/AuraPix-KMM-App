package com.ferbotz.aurapix.ui.viewmodel

import com.ferbotz.aurapix.data.remote.dto.ProfileDto
import com.ferbotz.aurapix.data.repository.AuthRepository
import com.ferbotz.aurapix.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
) : AuraViewModel() {

    private val _profileState = MutableStateFlow<UiState<ProfileDto>>(UiState.Loading)
    val profileState: StateFlow<UiState<ProfileDto>> = _profileState.asStateFlow()

    init {
        load()
    }

    fun load() {
        scope.launch {
            _profileState.value = UiState.Loading
            profileRepository.getProfile().fold(
                onSuccess = { _profileState.value = UiState.Success(it) },
                onFailure = { _profileState.value = UiState.Error(it.toApiError()) },
            )
        }
    }

    fun logout() {
        authRepository.logout()
    }
}

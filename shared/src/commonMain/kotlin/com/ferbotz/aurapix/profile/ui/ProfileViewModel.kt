package com.ferbotz.aurapix.profile.ui

import com.ferbotz.aurapix.core.ui.base.AuraViewModel
import com.ferbotz.aurapix.core.ui.base.UiState
import com.ferbotz.aurapix.core.ui.base.toUiState

import com.ferbotz.aurapix.profile.data.dto.ProfileDto
import com.ferbotz.aurapix.profile.data.AuthRepository
import com.ferbotz.aurapix.profile.data.ProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
) : AuraViewModel() {

    private val refreshTrigger = MutableStateFlow(0)

    val profileState: StateFlow<UiState<ProfileDto>> =
        refreshTrigger
            .flatMapLatest { profileRepository.getProfile() }
            .map { it.toUiState() }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun refresh() {
        refreshTrigger.value += 1
    }

    fun logout() {
        authRepository.logout()
    }
}

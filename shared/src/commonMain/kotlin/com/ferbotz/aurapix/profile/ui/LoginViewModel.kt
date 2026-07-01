package com.ferbotz.aurapix.profile.ui

import com.ferbotz.aurapix.core.ui.base.AuraViewModel
import com.ferbotz.aurapix.core.ui.base.UiState
import com.ferbotz.aurapix.core.ui.base.toUiState

import com.ferbotz.aurapix.profile.data.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModel(
    authRepository: AuthRepository,
) : AuraViewModel() {

    private val idToken = MutableStateFlow<String?>(null)

    /** Idle until [login]; Loading while exchanging the token; Success once the JWT is stored. */
    val loginState: StateFlow<UiState<Unit>> =
        idToken
            .filterNotNull()
            .flatMapLatest { authRepository.loginWithGoogle(it) }
            .map { state -> state.toUiState { } }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), UiState.Idle)

    fun login(googleIdToken: String) {
        idToken.value = googleIdToken
    }
}

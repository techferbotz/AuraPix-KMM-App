package com.ferbotz.aurapix.profile.ui

import com.ferbotz.aurapix.core.auth.SignInCancelledException
import com.ferbotz.aurapix.core.data.DataState
import com.ferbotz.aurapix.core.ui.base.AuraViewModel
import com.ferbotz.aurapix.core.ui.base.userMessage
import com.ferbotz.aurapix.profile.data.AuthRepository
import com.ferbotz.aurapix.profile.data.UserManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Login screen state: the whole Google flow (acquire ID token → exchange for JWT → hydrate session). */
sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userManager: UserManager,
) : AuraViewModel() {

    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    /**
     * Runs the platform Google flow → exchanges the ID token for our JWT → asks [UserManager] to
     * hydrate the session (so credits/subscription are fresh before we emit [LoginUiState.Success]).
     */
    fun signIn(acquireIdToken: suspend () -> Result<String>) {
        scope.launch {
            _state.value = LoginUiState.Loading
            acquireIdToken()
                .onSuccess { idToken ->
                    authRepository.loginWithGoogle(idToken).collect { ds ->
                        when (ds) {
                            is DataState.Loading -> _state.value = LoginUiState.Loading
                            is DataState.Success -> {
                                userManager.onLoggedIn(ds.data)
                                userManager.refresh()
                                _state.value = LoginUiState.Success
                            }
                            is DataState.Error -> _state.value = LoginUiState.Error(ds.error.userMessage())
                        }
                    }
                }
                .onFailure { e ->
                    _state.value = if (e is SignInCancelledException) LoginUiState.Idle
                    else LoginUiState.Error("Google sign-in failed. Please try again.")
                }
        }
    }
}

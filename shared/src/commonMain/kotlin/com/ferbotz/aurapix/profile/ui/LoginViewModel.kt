package com.ferbotz.aurapix.profile.ui

import com.ferbotz.aurapix.core.auth.SignInCancelledException
import com.ferbotz.aurapix.core.data.DataState
import com.ferbotz.aurapix.core.ui.base.AuraViewModel
import com.ferbotz.aurapix.core.ui.base.userMessage
import com.ferbotz.aurapix.profile.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Login screen state: the whole Google flow (acquire ID token → exchange for our JWT). */
sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(
    private val authRepository: AuthRepository,
) : AuraViewModel() {

    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    /**
     * [acquireIdToken] runs the platform Google flow (returns the Google ID token); on success we
     * exchange it for our app JWT via [AuthRepository.loginWithGoogle].
     */
    fun signIn(acquireIdToken: suspend () -> Result<String>) {
        scope.launch {
            _state.value = LoginUiState.Loading
            acquireIdToken()
                .onSuccess { idToken ->
                    authRepository.loginWithGoogle(idToken).collect { ds ->
                        _state.value = when (ds) {
                            is DataState.Loading -> LoginUiState.Loading
                            is DataState.Success -> LoginUiState.Success
                            is DataState.Error -> LoginUiState.Error(ds.error.userMessage())
                        }
                    }
                }
                .onFailure { e ->
                    // User dismissing the sheet is not an error — just return to idle.
                    _state.value = if (e is SignInCancelledException) LoginUiState.Idle
                    else LoginUiState.Error("Google sign-in failed. Please try again.")
                }
        }
    }
}

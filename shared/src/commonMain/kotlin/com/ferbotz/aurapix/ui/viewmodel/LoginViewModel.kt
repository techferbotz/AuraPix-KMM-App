package com.ferbotz.aurapix.ui.viewmodel

import com.ferbotz.aurapix.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
) : AuraViewModel() {

    private val _loginState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val loginState: StateFlow<UiState<Unit>> = _loginState.asStateFlow()

    fun loginWithGoogle(idToken: String) {
        scope.launch {
            _loginState.value = UiState.Loading
            authRepository.loginWithGoogle(idToken).fold(
                onSuccess = { _loginState.value = UiState.Success(Unit) },
                onFailure = { _loginState.value = UiState.Error(it.toApiError()) },
            )
        }
    }
}

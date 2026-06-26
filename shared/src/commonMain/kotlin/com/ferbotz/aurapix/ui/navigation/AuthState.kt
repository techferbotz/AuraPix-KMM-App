package com.ferbotz.aurapix.ui.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ferbotz.aurapix.data.repository.AuthRepository

@Stable
class AuthState(private val authRepository: AuthRepository) {
    var isLoggedIn by mutableStateOf(authRepository.isLoggedIn)
        private set

    fun onLoginSuccess() {
        isLoggedIn = true
    }

    fun logout() {
        authRepository.logout()
        isLoggedIn = false
    }
}

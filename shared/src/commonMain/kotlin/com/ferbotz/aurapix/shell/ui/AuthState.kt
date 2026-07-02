package com.ferbotz.aurapix.shell.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ferbotz.aurapix.profile.data.UserManager

@Stable
class AuthState(private val userManager: UserManager) {
    var isLoggedIn by mutableStateOf(userManager.current.isLoggedIn)
        private set

    fun onLoginSuccess() {
        isLoggedIn = true
    }

    fun logout() {
        userManager.logout()
        isLoggedIn = false
    }
}

package com.ferbotz.aurapix.ui.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Minimal in-memory auth state. Drives the conditional Profile/Login tab: signed out by
 * default (guest browsing), flipped by Login's Google/Guest actions. Replace with a real
 * auth/session source later.
 */
@Stable
class AuthState {
    var isLoggedIn by mutableStateOf(false)
        private set

    fun login() {
        isLoggedIn = true
    }

    fun logout() {
        isLoggedIn = false
    }
}

package com.ferbotz.aurapix.shell.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ferbotz.aurapix.profile.data.UserManager

/**
 * Whether the app is signed in, for the navigation graph.
 *
 * [observeSession] keeps this in step with [UserManager], which is the single source of truth.
 * That matters because a sign-out is no longer always something the user asked for: a `401`
 * drops the session from the network layer (BE-008), and the UI has to notice.
 */
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

    /** Mirrors the session, including a sign-out this class didn't initiate. Collects forever. */
    suspend fun observeSession() {
        userManager.state.collect { isLoggedIn = it.isLoggedIn }
    }
}

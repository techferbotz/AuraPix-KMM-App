package com.ferbotz.aurapix.shell.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ferbotz.aurapix.core.di.DataModule
import com.ferbotz.aurapix.core.session.UserState

/**
 * Observes the app-wide single source of truth for the signed-in user
 * ([DataModule.userManager]) as Compose state. Every screen that shows user data — gem
 * balance, avatar, name — should read it from here so the screens never diverge.
 */
@Composable
fun currentUserState(): UserState {
    val user by DataModule.userManager.state.collectAsState()
    return user
}

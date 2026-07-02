package com.ferbotz.aurapix.profile.ui

import com.ferbotz.aurapix.core.session.UserState
import com.ferbotz.aurapix.core.ui.base.AuraViewModel
import com.ferbotz.aurapix.core.ui.base.UiState
import com.ferbotz.aurapix.profile.data.UserManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ProfileViewModel(
    private val userManager: UserManager,
) : AuraViewModel() {

    /** Mirrors the shared session; always renders cached data and refreshes in the background. */
    val state: StateFlow<UiState<ProfileUi>> =
        userManager.state
            .map<UserState, UiState<ProfileUi>> { UiState.Success(it.toProfileUi()) }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    init {
        userManager.refreshAsync()
    }

    fun refresh() {
        userManager.refreshAsync()
    }

    fun logout() {
        userManager.logout()
    }
}

private fun UserState.toProfileUi() = ProfileUi(
    name = name ?: "",
    email = email ?: "",
    avatarUrl = avatarUrl,
    totalCredits = credits,
    isPremium = isPremium,
    subscriptionLabel = when (subscriptionStatus) {
        "ACTIVE" -> "Premium"
        "EXPIRED" -> "Expired"
        "CANCELLED" -> "Cancelled"
        else -> "Free plan"
    },
)

package com.ferbotz.aurapix.profile.data

import com.ferbotz.aurapix.core.data.prefs.AppPreferences
import com.ferbotz.aurapix.core.session.UserState
import com.ferbotz.aurapix.profile.data.dto.UserSummaryDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Single source of truth for the signed-in user. Holds an observable [UserState] (seeded instantly
 * from prefs, refreshed from `GET /profile`) that any screen/VM can collect. Also caches to prefs so
 * the session survives restarts. Backend is the source of subscription/credits for now; RevenueCat
 * reconciliation is a future step (see PaymentManager).
 */
class UserManager(
    private val profileRemote: ProfileRemoteDataSource,
    private val prefs: AppPreferences,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(prefs.toUserState())
    val state: StateFlow<UserState> = _state.asStateFlow()
    val current: UserState get() = _state.value

    init {
        // Warm the session on launch if we already have a token.
        if (prefs.isLoggedIn) refreshAsync()
    }

    /** Fetches `/profile` (identity + credits + subscription) → updates state + prefs cache. */
    suspend fun refresh(): Result<Unit> {
        if (!prefs.isLoggedIn) return Result.success(Unit)
        return profileRemote.getProfile().map { profile ->
            prefs.userId = profile.id
            prefs.displayName = profile.displayName
            prefs.email = profile.email
            prefs.avatarUrl = profile.profileImageUrl
            prefs.cachedCredits = profile.credits.totalCredits
            prefs.subscriptionStatus = profile.subscription.status
            _state.value = prefs.toUserState()
        }
    }

    fun refreshAsync() {
        scope.launch { refresh() }
    }

    /** Seed the session immediately after login from the auth response's user summary. */
    fun onLoggedIn(user: UserSummaryDto) {
        prefs.userId = user.id
        prefs.displayName = user.displayName
        prefs.email = user.email
        prefs.avatarUrl = user.profileImageUrl
        _state.value = prefs.toUserState()
    }

    fun logout() {
        prefs.clearSession()
        _state.value = UserState()
    }

    private fun AppPreferences.toUserState() = UserState(
        isLoggedIn = isLoggedIn,
        id = userId,
        name = displayName,
        email = email,
        avatarUrl = avatarUrl,
        credits = cachedCredits,
        subscriptionStatus = subscriptionStatus,
    )
}

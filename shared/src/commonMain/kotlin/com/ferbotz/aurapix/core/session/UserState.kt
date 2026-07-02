package com.ferbotz.aurapix.core.session

/**
 * App-wide snapshot of the signed-in user. Held by `UserManager` as observable state and read
 * from anywhere (credits gate, paywall, profile, gem badge). `UserState()` = signed out.
 */
data class UserState(
    val isLoggedIn: Boolean = false,
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val avatarUrl: String? = null,
    val credits: Int = 0,
    val subscriptionStatus: String = "NONE",
) {
    val isPremium: Boolean get() = subscriptionStatus == "ACTIVE"
}

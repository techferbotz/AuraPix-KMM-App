package com.ferbotz.aurapix.data.prefs

import com.russhwolf.settings.Settings

/**
 * Typed wrapper around [Settings] (multiplatform-settings) for lightweight key-value
 * preferences. Backed by SharedPreferences on Android and NSUserDefaults on iOS.
 */
class AppPreferences(private val settings: Settings) {

    var authToken: String?
        get() = settings.getStringOrNull(KEY_AUTH_TOKEN)
        set(value) {
            if (value == null) settings.remove(KEY_AUTH_TOKEN) else settings.putString(KEY_AUTH_TOKEN, value)
        }

    var isOnboardingComplete: Boolean
        get() = settings.getBoolean(KEY_ONBOARDED, false)
        set(value) = settings.putBoolean(KEY_ONBOARDED, value)

    var cachedCredits: Int
        get() = settings.getInt(KEY_CREDITS, 0)
        set(value) = settings.putInt(KEY_CREDITS, value)

    var themeMode: String
        get() = settings.getString(KEY_THEME, DEFAULT_THEME)
        set(value) = settings.putString(KEY_THEME, value)

    val isLoggedIn: Boolean get() = authToken != null

    fun clear() = settings.clear()

    private companion object {
        const val KEY_AUTH_TOKEN = "auth_token"
        const val KEY_ONBOARDED = "onboarding_complete"
        const val KEY_CREDITS = "cached_credits"
        const val KEY_THEME = "theme_mode"
        const val DEFAULT_THEME = "system"
    }
}

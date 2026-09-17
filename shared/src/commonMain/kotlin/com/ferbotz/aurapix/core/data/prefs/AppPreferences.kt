package com.ferbotz.aurapix.core.data.prefs

import com.russhwolf.settings.Settings
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Typed wrapper around [Settings] (multiplatform-settings) for lightweight key-value
 * preferences. Backed by SharedPreferences on Android and NSUserDefaults on iOS.
 * The signed-in user's identity/credits/subscription are cached here at login (from GET /profile).
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

    // ── Cached signed-in user (hydrated at login) ──────────────────────────────

    var userId: String?
        get() = settings.getStringOrNull(KEY_USER_ID)
        set(value) { if (value == null) settings.remove(KEY_USER_ID) else settings.putString(KEY_USER_ID, value) }

    var displayName: String?
        get() = settings.getStringOrNull(KEY_DISPLAY_NAME)
        set(value) { if (value == null) settings.remove(KEY_DISPLAY_NAME) else settings.putString(KEY_DISPLAY_NAME, value) }

    var email: String?
        get() = settings.getStringOrNull(KEY_EMAIL)
        set(value) { if (value == null) settings.remove(KEY_EMAIL) else settings.putString(KEY_EMAIL, value) }

    var avatarUrl: String?
        get() = settings.getStringOrNull(KEY_AVATAR)
        set(value) { if (value == null) settings.remove(KEY_AVATAR) else settings.putString(KEY_AVATAR, value) }

    var cachedCredits: Int
        get() = settings.getInt(KEY_CREDITS, 0)
        set(value) = settings.putInt(KEY_CREDITS, value)

    var subscriptionStatus: String
        get() = settings.getString(KEY_SUBSCRIPTION, DEFAULT_SUBSCRIPTION)
        set(value) = settings.putString(KEY_SUBSCRIPTION, value)

    var themeMode: String
        get() = settings.getString(KEY_THEME, DEFAULT_THEME)
        set(value) = settings.putString(KEY_THEME, value)

    val isLoggedIn: Boolean get() = authToken != null

    // -- Per-install identity + remote config cache ---------------------------
    // Both are per INSTALL, not per session: [clearSession] deliberately leaves them alone, so
    // signing out can't throw away a kill switch or move this install into a different
    // experiment arm.

    /**
     * Stable per-install id, sent as `X-Device-Id` on every request. It is the key the backend
     * buckets an install into an experiment arm with, so it is generated and persisted on
     * **first access** — even the very first request of a fresh install carries one.
     */
    @OptIn(ExperimentalUuidApi::class)
    val deviceId: String
        get() = settings.getStringOrNull(KEY_DEVICE_ID) ?: Uuid.random().toString().also {
            settings.putString(KEY_DEVICE_ID, it)
        }

    /** The last good `GET /config` payload, as JSON, or null before the first successful fetch. */
    val remoteConfigJson: String?
        get() = settings.getStringOrNull(KEY_REMOTE_CONFIG)

    /** Epoch millis of the fetch that produced [remoteConfigJson], or null if never fetched. */
    val remoteConfigFetchedAt: Long?
        get() = settings.getLongOrNull(KEY_REMOTE_CONFIG_AT)

    fun cacheRemoteConfig(json: String, fetchedAtEpochMillis: Long) {
        settings.putString(KEY_REMOTE_CONFIG, json)
        settings.putLong(KEY_REMOTE_CONFIG_AT, fetchedAtEpochMillis)
    }

    /**
     * Clears the whole signed-in session (token + cached user/credits/subscription); keeps
     * theme/onboarding, and deliberately keeps [deviceId] and the remote config cache, which
     * belong to the install rather than the session.
     */
    fun clearSession() {
        authToken = null
        userId = null
        displayName = null
        email = null
        avatarUrl = null
        cachedCredits = 0
        subscriptionStatus = DEFAULT_SUBSCRIPTION
    }

    fun clear() = settings.clear()

    private companion object {
        const val KEY_AUTH_TOKEN = "auth_token"
        const val KEY_ONBOARDED = "onboarding_complete"
        const val KEY_USER_ID = "user_id"
        const val KEY_DISPLAY_NAME = "display_name"
        const val KEY_EMAIL = "email"
        const val KEY_AVATAR = "avatar_url"
        const val KEY_CREDITS = "cached_credits"
        const val KEY_SUBSCRIPTION = "subscription_status"
        const val KEY_THEME = "theme_mode"
        const val KEY_DEVICE_ID = "device_id"
        const val KEY_REMOTE_CONFIG = "remote_config"
        const val KEY_REMOTE_CONFIG_AT = "remote_config_fetched_at"
        const val DEFAULT_THEME = "system"
        const val DEFAULT_SUBSCRIPTION = "NONE"
    }
}

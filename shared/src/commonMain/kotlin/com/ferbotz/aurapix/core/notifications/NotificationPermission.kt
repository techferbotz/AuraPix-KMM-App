package com.ferbotz.aurapix.core.notifications

import androidx.compose.runtime.Composable

/**
 * Whether push notifications can reach this device, and the ways to change that.
 *
 * Android 13+ needs a runtime permission; older Android has notifications on unless they're
 * switched off in Settings. iOS isn't wired yet (no APNs key or Firebase Apple SDK), so there
 * [isSupported] is false and every notifications entry point stays hidden.
 *
 * The values are Compose state: reading them in composition recomposes when they change — after
 * the system prompt, or on coming back from the system settings.
 */
interface NotificationPermission {

    /** False where push isn't set up at all; the UI then shows nothing about notifications. */
    val isSupported: Boolean

    /** Notifications are allowed right now: permitted, and not switched off in Settings. */
    val isEnabled: Boolean

    /**
     * Asking would show the system prompt: Android 13+, not granted, and not refused for good.
     * When false and [isEnabled] is too, only [openSettings] can turn them on.
     */
    val canAsk: Boolean

    /** Whether the app has ever shown the prompt — the one-time ask checks this. */
    val askedBefore: Boolean

    /** Shows the system prompt; [onResult] then gets whether notifications are allowed. */
    fun ask(onResult: (Boolean) -> Unit = {})

    /** Opens this app's notification settings — to turn them off, or on after a firm "no". */
    fun openSettings()
}

@Composable
expect fun rememberNotificationPermission(): NotificationPermission

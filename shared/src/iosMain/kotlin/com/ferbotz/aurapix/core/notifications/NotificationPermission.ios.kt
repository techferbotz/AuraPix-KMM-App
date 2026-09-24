package com.ferbotz.aurapix.core.notifications

import androidx.compose.runtime.Composable

/**
 * iOS: push isn't wired yet — it needs an APNs key in Firebase and the Firebase Apple SDK (see
 * docs/FIREBASE.md). Until then nothing about notifications is shown.
 */
@Composable
actual fun rememberNotificationPermission(): NotificationPermission = NotWiredYet

private object NotWiredYet : NotificationPermission {
    override val isSupported: Boolean = false
    override val isEnabled: Boolean = false
    override val canAsk: Boolean = false
    override val askedBefore: Boolean = false
    override fun ask(onResult: (Boolean) -> Unit) = onResult(false)
    override fun openSettings() = Unit
}

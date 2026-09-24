package com.ferbotz.aurapix.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Receives Firebase Cloud Messaging traffic.
 *
 * A notification message sent while the app is in the background never reaches here: the system
 * shows it from the manifest defaults (icon, colour, the "Updates" channel). What does arrive is a
 * notification message while the app is open, which FCM leaves to the app, so it's shown the same
 * way; and data-only messages, which nothing sends, so they're ignored.
 */
class AuraPixMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val notification = message.notification ?: return
        PushNotifications.show(
            context = this,
            title = notification.title,
            body = notification.body,
            link = message.data[PushNotifications.EXTRA_LINK],
        )
    }

    // Sends come from the Firebase console, which targets the app rather than devices, so no
    // backend needs the token; a new one is only logged for testing.
    override fun onNewToken(token: String) {
        PushNotifications.logTokenForTesting(this, token)
    }
}

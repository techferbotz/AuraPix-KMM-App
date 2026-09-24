package com.ferbotz.aurapix

import android.app.Application
import com.ferbotz.aurapix.push.PushNotifications
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Process-wide setup that can't wait for the first screen: a push can start the process with no
 * activity at all, and its notification needs the channel to exist.
 *
 * Firebase (Crashlytics, Cloud Messaging) starts itself from `google-services.json` before this
 * runs; when the file is missing from the build it simply isn't there (see docs/FIREBASE.md), so
 * nothing here touches it without checking.
 */
class AuraPixApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PushNotifications.createChannel(this)
        if (FirebaseApp.getApps(this).isNotEmpty()) {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                PushNotifications.logTokenForTesting(this, token)
            }
        }
    }
}

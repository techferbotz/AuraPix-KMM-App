package com.ferbotz.aurapix.push

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ferbotz.aurapix.MainActivity
import com.ferbotz.aurapix.R

/**
 * Push notifications (Firebase Cloud Messaging). They're sent from the Firebase console, as
 * campaigns aimed at this app; nothing registers the device with our backend.
 *
 * A message may carry a **`link`** in its custom data — a template link such as
 * `https://aurapix.ferbotz.com/template/<id or slug>` — and tapping the notification then opens
 * that template. In the background the system shows the notification itself and hands its data
 * to [MainActivity] as extras; in the foreground [AuraPixMessagingService] builds it here and puts
 * the link in the same extra, so both arrive the same way.
 */
object PushNotifications {

    /** The custom-data key (and so the intent extra) holding the link a notification opens. */
    const val EXTRA_LINK = "link"

    private const val LOG_TAG = "AuraPix-Push"

    /**
     * Creates the "Updates" channel — also the manifest's default for notifications shown while
     * the app is in the background. Idempotent, so it runs at every start.
     */
    fun createChannel(context: Context) {
        val channel = NotificationChannelCompat.Builder(
            context.getString(R.string.notification_channel_id),
            NotificationManagerCompat.IMPORTANCE_DEFAULT,
        )
            .setName(context.getString(R.string.notification_channel_name))
            .setDescription(context.getString(R.string.notification_channel_description))
            .build()
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }

    /**
     * Shows a notification that opens the app, at [link] when there is one. Does nothing when
     * notifications aren't allowed: turned off, or (Android 13+) the permission not granted.
     */
    fun show(context: Context, title: String?, body: String?, link: String?) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Each message gets its own notification (and its own PendingIntent, or a second one
        // would overwrite the first's link).
        val id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val open = Intent(context, MainActivity::class.java).apply {
            link?.takeIf { it.isNotBlank() }?.let { putExtra(EXTRA_LINK, it) }
        }
        val onTap = PendingIntent.getActivity(
            context,
            id,
            open,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, context.getString(R.string.notification_channel_id))
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.notification_accent))
            .setContentTitle(title ?: context.getString(R.string.app_name))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(onTap)
            .build()
        manager.notify(id, notification)
    }

    /**
     * Prints this device's FCM token, for the console's "Send test message" — in debug builds
     * only, so a release never writes it to the device log.
     */
    fun logTokenForTesting(context: Context, token: String) {
        if (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            Log.d(LOG_TAG, "FCM registration token: $token")
        }
    }
}

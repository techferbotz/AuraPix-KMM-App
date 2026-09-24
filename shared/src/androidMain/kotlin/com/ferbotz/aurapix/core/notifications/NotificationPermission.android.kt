package com.ferbotz.aurapix.core.notifications

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.ferbotz.aurapix.core.data.prefs.AppPreferences
import com.ferbotz.aurapix.core.di.DataModule
import com.ferbotz.aurapix.core.platform.findActivity

/** Android: the POST_NOTIFICATIONS permission (13+), and the app-wide switch in Settings. */
@Composable
actual fun rememberNotificationPermission(): NotificationPermission {
    val context = LocalContext.current
    val permission = remember(context) { AndroidNotificationPermission(context, DataModule.preferences) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permission.onAnswered()
    }
    SideEffect { permission.launcher = launcher }
    // Settings can change while the app is away; re-read on the way back.
    LifecycleResumeEffect(permission) {
        permission.refresh()
        onPauseOrDispose { }
    }
    return permission
}

private class AndroidNotificationPermission(
    private val context: Context,
    private val preferences: AppPreferences,
) : NotificationPermission {

    var launcher: ActivityResultLauncher<String>? = null

    private var enabled by mutableStateOf(readEnabled())
    private var asked by mutableStateOf(preferences.notificationPermissionAsked)
    private var pendingResult: ((Boolean) -> Unit)? = null

    override val isSupported: Boolean = true

    override val isEnabled: Boolean get() = enabled

    override val askedBefore: Boolean get() = asked

    override val canAsk: Boolean
        get() = Build.VERSION.SDK_INT >= 33 &&
            !enabled &&
            // Granted but still off means switched off in Settings: the prompt wouldn't appear.
            !isGranted() &&
            // Never asked, or asked and refused once — a second "no" is final, and then Android
            // stops showing the prompt (and stops wanting a rationale).
            (!asked || shouldShowRationale())

    override fun ask(onResult: (Boolean) -> Unit) {
        val launcher = launcher
        if (!canAsk || launcher == null) {
            onResult(enabled)
            return
        }
        preferences.notificationPermissionAsked = true
        asked = true
        pendingResult = onResult
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    override fun openSettings() {
        val intent = if (Build.VERSION.SDK_INT >= 26) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        }
        val activity = context.findActivity()
        if (activity == null) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        // A settings screen missing on some OEM build would otherwise crash the tap.
        runCatching { (activity ?: context).startActivity(intent) }
    }

    fun onAnswered() {
        refresh()
        pendingResult?.invoke(enabled)
        pendingResult = null
    }

    fun refresh() {
        enabled = readEnabled()
    }

    private fun readEnabled(): Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled()

    private fun isGranted(): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun shouldShowRationale(): Boolean {
        val activity = context.findActivity() ?: return false
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
    }
}

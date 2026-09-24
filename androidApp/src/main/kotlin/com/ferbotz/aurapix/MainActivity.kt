package com.ferbotz.aurapix

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.ferbotz.aurapix.core.di.DataModule
import com.ferbotz.aurapix.push.PushNotifications
import com.ferbotz.aurapix.shell.ui.DeepLinks
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Edge-to-edge for the whole activity with transparent system bars. The icon tint
        // follows the *app's* theme, never the system's night mode — AuraPixTheme stopped
        // consulting the device, so `auto` would put light icons on the light theme whenever the
        // phone was in dark mode. Applied here from the stored choice so the first frame is
        // right, then kept in sync below. Compose consumes the insets via statusBarsPadding /
        // the nav bar, so content never sits under the bars.
        applySystemBarStyle(DataModule.themeManager.current.isDark)
        super.onCreate(savedInstanceState)
        // Configure RevenueCat here (Android-only) with the backend user id, if signed in.
        DataModule.paymentManager.configure(DataModule.preferences.userId)
        // A cold start from a shared link or a tapped notification: prime the deep link bus before
        // composing so the NavHost picks it up on first frame. Only on a fresh start — an activity
        // recreated after process death gets the same intent again, and its restored back stack
        // already shows the link, so handling it twice would push the screen a second time.
        if (savedInstanceState == null) handleDeepLink(intent)
        tagCrashReportsWithAccount()
        setContent {
            val themeMode by DataModule.themeManager.mode.collectAsState()
            LaunchedEffect(themeMode) { applySystemBarStyle(themeMode.isDark) }
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    /** Light icons on the dark theme, dark icons on the light one; the bars stay transparent. */
    private fun applySystemBarStyle(dark: Boolean) {
        val bars = if (dark) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
    }

    private fun handleDeepLink(intent: Intent?) {
        intent?.let(::linkOf)?.let { DeepLinks.handleUrl(it) }
    }

    /**
     * The link an intent carries: the URL of a tapped App Link, or the `link` a tapped push
     * notification carries — FCM hands a background notification's data to this activity as
     * extras, and [PushNotifications] builds foreground ones the same way.
     */
    private fun linkOf(intent: Intent): String? =
        intent.data?.toString()
            ?: intent.getStringExtra(PushNotifications.EXTRA_LINK)?.takeIf { it.isNotBlank() }

    /**
     * Crash reports carry the signed-in account's id — an opaque uuid, never the email or name —
     * so a user's report to support can be matched to their crashes. Cleared on sign-out.
     */
    private fun tagCrashReportsWithAccount() {
        if (FirebaseApp.getApps(this).isEmpty()) return
        val crashlytics = FirebaseCrashlytics.getInstance()
        lifecycleScope.launch {
            DataModule.userManager.state.map { it.id }.distinctUntilChanged().collect { id ->
                crashlytics.setUserId(id.orEmpty())
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
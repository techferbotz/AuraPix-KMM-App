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
import com.ferbotz.aurapix.core.di.DataModule
import com.ferbotz.aurapix.shell.ui.DeepLinks

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
        // Prime the deep link bus before composing so the NavHost picks it up on first frame.
        handleDeepLink(intent)
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
        intent?.data?.toString()?.let { DeepLinks.handleUrl(it) }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
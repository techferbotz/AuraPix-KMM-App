package com.ferbotz.aurapix

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ferbotz.aurapix.core.di.DataModule
import com.ferbotz.aurapix.shell.ui.DeepLinks

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Edge-to-edge for the whole activity with transparent system bars. `auto` flips the
        // icon tint with the system's night mode, which is what AuraPixTheme follows — pinning
        // these to `dark` (light icons) would leave them invisible on the light theme.
        // Compose consumes the insets via statusBarsPadding / the floating nav, so content
        // never sits under the bars.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        // Configure RevenueCat here (Android-only) with the backend user id, if signed in.
        DataModule.paymentManager.configure(DataModule.preferences.userId)
        // Prime the deep link bus before composing so the NavHost picks it up on first frame.
        handleDeepLink(intent)
        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
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
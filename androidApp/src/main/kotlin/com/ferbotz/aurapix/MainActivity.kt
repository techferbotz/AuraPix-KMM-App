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
        // Edge-to-edge for the whole activity: transparent system bars with light icons
        // (the app is always dark). Compose consumes the insets via statusBarsPadding /
        // the M3 NavigationBar, so content never sits under the bars.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
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
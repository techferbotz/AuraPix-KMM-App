package com.ferbotz.aurapix

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.ferbotz.aurapix.core.di.DataModule
import com.ferbotz.aurapix.shell.ui.AuraNavHost
import com.ferbotz.aurapix.shell.ui.RemoteConfigGate
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme

@Composable
@Preview
fun App() {
    // The one place the app picks a theme: the user's stored choice, never the device's night
    // setting. Flipping the Settings switch re-composes everything under here.
    val themeMode by DataModule.themeManager.mode.collectAsState()
    AuraPixTheme(darkTheme = themeMode.isDark) {
        // Everything runs inside the remote-config gate: it publishes the live document to every
        // screen and is the single place maintenance or a forced update can replace the app.
        RemoteConfigGate {
            AuraNavHost()
        }
    }
}

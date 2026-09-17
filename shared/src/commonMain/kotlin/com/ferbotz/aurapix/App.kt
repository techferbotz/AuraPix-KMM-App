package com.ferbotz.aurapix

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ferbotz.aurapix.shell.ui.AuraNavHost
import com.ferbotz.aurapix.shell.ui.RemoteConfigGate
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme

@Composable
@Preview
fun App() {
    AuraPixTheme {
        // Everything runs inside the remote-config gate: it publishes the live document to every
        // screen and is the single place maintenance or a forced update can replace the app.
        RemoteConfigGate {
            AuraNavHost()
        }
    }
}

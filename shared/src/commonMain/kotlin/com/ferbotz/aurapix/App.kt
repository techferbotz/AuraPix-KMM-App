package com.ferbotz.aurapix

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ferbotz.aurapix.shell.ui.AuraNavHost
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme

@Composable
@Preview
fun App() {
    AuraPixTheme {
        AuraNavHost()
    }
}

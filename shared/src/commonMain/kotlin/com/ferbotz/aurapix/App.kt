package com.ferbotz.aurapix

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ferbotz.aurapix.ui.navigation.AuraNavHost
import com.ferbotz.aurapix.ui.theme.AuraPixTheme

@Composable
@Preview
fun App() {
    AuraPixTheme {
        AuraNavHost()
    }
}

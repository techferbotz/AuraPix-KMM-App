package com.ferbotz.aurapix.core.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import co.touchlab.kermit.Logger
import com.ferbotz.aurapix.core.platform.findActivity

@Composable
actual fun rememberInAppBrowser(): InAppBrowser {
    val context = LocalContext.current
    // The tab's toolbar takes the app's own surface, so the page reads as opened by AuraPix.
    val toolbarColor = MaterialTheme.colorScheme.surface.toArgb()
    return remember(context, toolbarColor) {
        InAppBrowser { url ->
            val tab = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setDefaultColorSchemeParams(
                    CustomTabColorSchemeParams.Builder().setToolbarColor(toolbarColor).build(),
                )
                .build()
            val activity = context.findActivity()
            if (activity == null) tab.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                // With no Custom Tabs browser installed this still opens in whichever browser is.
                tab.launchUrl(activity ?: context, Uri.parse(url))
            } catch (e: ActivityNotFoundException) {
                Logger.withTag("AuraPix").w(e) { "No browser installed to open $url" }
            }
        }
    }
}

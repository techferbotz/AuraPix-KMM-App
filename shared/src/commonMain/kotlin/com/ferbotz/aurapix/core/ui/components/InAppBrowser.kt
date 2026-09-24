package com.ferbotz.aurapix.core.ui.components

import androidx.compose.runtime.Composable

/**
 * Opens a web page over the app: Custom Tabs on Android, `SFSafariViewController` on iOS — how
 * API.md §4.17b says the legal pages are opened.
 *
 * Not an embedded WebView: the legal pages carry `mailto:` links (the email route for deleting an
 * account, for one), which a bare WebView can't follow, and the reader gets a real address bar.
 * Not the system browser either: the user never leaves the app, so whatever opened the page — the
 * paywall sheet, say — is still there, in the same state, when they close it.
 */
fun interface InAppBrowser {
    fun open(url: String)
}

@Composable
expect fun rememberInAppBrowser(): InAppBrowser

package com.ferbotz.aurapix.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import platform.Foundation.NSURL
import platform.SafariServices.SFSafariViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

@Composable
actual fun rememberInAppBrowser(): InAppBrowser {
    val host = LocalUIViewController.current
    return remember(host) {
        InAppBrowser { url ->
            val nsUrl = NSURL.URLWithString(url) ?: return@InAppBrowser
            // SFSafariViewController throws on anything but http(s); hand the rest to the system.
            if (nsUrl.scheme?.lowercase() !in setOf("http", "https")) {
                UIApplication.sharedApplication.openURL(nsUrl, options = emptyMap<Any?, Any>(), completionHandler = null)
                return@InAppBrowser
            }
            host.topmostPresented()
                .presentViewController(SFSafariViewController(uRL = nsUrl), animated = true, completion = null)
        }
    }
}

/** A controller presents one thing at a time, so present over whatever is already showing. */
private fun UIViewController.topmostPresented(): UIViewController {
    var top = this
    while (true) top = top.presentedViewController ?: return top
}

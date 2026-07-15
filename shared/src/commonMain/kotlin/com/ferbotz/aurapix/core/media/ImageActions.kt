package com.ferbotz.aurapix.core.media

import androidx.compose.runtime.Composable

/** Save the result image to the gallery, or hand it to the OS share sheet. */
interface ImageActions {
    /** Download the image at [url] into the device's photo gallery. */
    fun download(url: String)

    /** Share the image at [url] via the platform share sheet. */
    fun share(url: String)

    /** Share a plain-text link (e.g. a template URL) via the platform share sheet. */
    fun shareLink(url: String)
}

/** Remembers the platform [ImageActions] (needs a [Context]/root view controller from composition). */
@Composable
expect fun rememberImageActions(): ImageActions

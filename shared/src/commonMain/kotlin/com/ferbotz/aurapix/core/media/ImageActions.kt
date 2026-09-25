package com.ferbotz.aurapix.core.media

import androidx.compose.runtime.Composable

/** Save the result image to the gallery, or hand it to the OS share sheet. */
interface ImageActions {
    /** Download the image at [url] into the device's photo gallery. */
    fun download(url: String)

    /** Share the image at [url] via the platform share sheet. */
    fun share(url: String)

    /**
     * Share [text] with the image at [imageUrl] attached, so chat apps show the picture with the
     * text as its caption. Falls back to the text alone when there's no image or it can't be
     * loaded, so the text — and any link in it — always goes out. Returns once the sheet is up.
     */
    suspend fun shareWithImage(text: String, imageUrl: String?)
}

/** Remembers the platform [ImageActions] (needs a [Context]/root view controller from composition). */
@Composable
expect fun rememberImageActions(): ImageActions

package com.ferbotz.aurapix.core.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageWriteToSavedPhotosAlbum

@Composable
actual fun rememberImageActions(): ImageActions {
    val scope = rememberCoroutineScope()
    return remember { IosImageActions(scope) }
}

@OptIn(ExperimentalForeignApi::class)
private class IosImageActions(private val scope: CoroutineScope) : ImageActions {

    override fun download(url: String) = withImage(url) { image ->
        // Requires NSPhotoLibraryAddUsageDescription in Info.plist.
        UIImageWriteToSavedPhotosAlbum(image, null, null, null)
    }

    override fun share(url: String) = withImage(url) { image -> present(listOf(image)) }

    override suspend fun shareWithImage(text: String, imageUrl: String?) {
        val image = imageUrl?.let { loadImage(it) }
        // Each target app takes the items it understands: chats keep both, some keep only one.
        present(listOfNotNull(image, text))
    }

    private fun present(items: List<Any>) {
        val activityVc = UIActivityViewController(activityItems = items, applicationActivities = null)
        // Presented from the app's root controller (a sheet on iPhone). iPad would also need a
        // popover anchor on the activity controller before presenting.
        UIApplication.sharedApplication.keyWindow?.rootViewController
            ?.presentViewController(activityVc, animated = true, completion = null)
    }

    /** Fetches [url] off the main thread, then delivers a [UIImage] back on the main thread. */
    private fun withImage(url: String, onImage: (UIImage) -> Unit) {
        scope.launch { loadImage(url)?.let(onImage) }
    }

    /** Fetches [url] off the main thread; null when it can't be read as an image. */
    private suspend fun loadImage(url: String): UIImage? {
        val nsUrl = NSURL.URLWithString(url) ?: return null
        return withContext(Dispatchers.Default) {
            NSData.dataWithContentsOfURL(nsUrl)?.let { UIImage.imageWithData(it) }
        }
    }
}

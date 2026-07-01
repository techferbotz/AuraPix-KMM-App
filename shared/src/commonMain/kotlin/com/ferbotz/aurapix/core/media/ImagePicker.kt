package com.ferbotz.aurapix.core.media

import androidx.compose.runtime.Composable

/** Launches the platform photo picker; the chosen image's bytes are delivered to the callback. */
interface ImagePicker {
    fun pick()
}

/** Remembers a platform [ImagePicker] wired to [onImagePicked] (called with the selected image bytes). */
@Composable
expect fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): ImagePicker

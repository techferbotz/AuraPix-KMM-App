package com.ferbotz.aurapix.core.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/** iOS stub — the native photo picker (PHPickerViewController) isn't wired yet. */
@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): ImagePicker =
    remember {
        object : ImagePicker {
            override fun pick() { /* no-op until the iOS picker is implemented */ }
        }
    }

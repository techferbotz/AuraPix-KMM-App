package com.ferbotz.aurapix.core.ui.components

import androidx.compose.ui.platform.ClipEntry

/**
 * A plain-text [ClipEntry] to hand to `LocalClipboard.current.setClipEntry(...)`. Compose has no
 * common way to build one: Android wraps a `ClipData`, iOS a pasteboard string. [label] is the
 * clip's user-visible name on Android and is ignored on iOS.
 */
expect fun plainTextClipEntry(label: String, text: String): ClipEntry

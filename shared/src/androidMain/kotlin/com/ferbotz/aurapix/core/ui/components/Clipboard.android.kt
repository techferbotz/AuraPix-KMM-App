package com.ferbotz.aurapix.core.ui.components

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry

actual fun plainTextClipEntry(label: String, text: String): ClipEntry =
    ClipEntry(ClipData.newPlainText(label, text))

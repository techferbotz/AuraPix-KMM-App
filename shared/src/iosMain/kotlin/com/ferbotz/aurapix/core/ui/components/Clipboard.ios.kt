package com.ferbotz.aurapix.core.ui.components

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry

@OptIn(ExperimentalComposeUiApi::class)
actual fun plainTextClipEntry(label: String, text: String): ClipEntry = ClipEntry.withPlainText(text)

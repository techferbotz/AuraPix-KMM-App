package com.ferbotz.aurapix.core.platform

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/** The activity behind a Compose [Context], which may be wrapped (a dialog's, a theme's). */
internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

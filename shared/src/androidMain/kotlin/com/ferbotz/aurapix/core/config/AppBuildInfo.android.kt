package com.ferbotz.aurapix.core.config

import android.os.Build
import com.ferbotz.aurapix.core.data.local.AndroidAppContext

/**
 * Android's [AppBuildInfo], read from the installed package. Anything unreadable falls back to
 * `"0"` / `0` — an unknown build must never hard-block the app, so the failure is silent by design.
 */
actual fun appBuildInfo(): AppBuildInfo {
    val context = runCatching { AndroidAppContext.require() }.getOrNull()
    val info = context?.let {
        runCatching { it.packageManager.getPackageInfo(it.packageName, 0) }.getOrNull()
    }
    val buildNumber = when {
        info == null -> 0L
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> info.longVersionCode
        else -> @Suppress("DEPRECATION") info.versionCode.toLong()
    }
    return AppBuildInfo(
        platform = AppPlatform.ANDROID,
        versionName = info?.versionName ?: "0",
        buildNumber = buildNumber,
    )
}

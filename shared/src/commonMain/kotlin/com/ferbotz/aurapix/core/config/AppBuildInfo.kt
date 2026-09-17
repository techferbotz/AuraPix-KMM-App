package com.ferbotz.aurapix.core.config

/** Which platform build is calling — the wire value of the `X-App-Platform` header. */
enum class AppPlatform(val wire: String) {
    ANDROID("android"),
    IOS("ios"),
}

/**
 * Identifies this build to the backend. Sent on **every** request as `X-App-Platform`,
 * `X-App-Version` and `X-App-Build` (BE-005): the backend targets remote config on them, and a
 * build that never sent them can never be placed in a rollout or experiment later. [buildNumber]
 * is also what `update.minSupportedBuild` / `recommendedBuild` are compared against.
 *
 * @param versionName the display version, e.g. `1.4.2`. Logs only — never compared.
 * @param buildNumber the integer build (Android `versionCode`, iOS `CFBundleVersion`); `0` when
 *   it can't be read, which the update policy treats as "unknown build, never block".
 */
data class AppBuildInfo(
    val platform: AppPlatform,
    val versionName: String,
    val buildNumber: Long,
)

/** This build, read from the platform's own package metadata. */
expect fun appBuildInfo(): AppBuildInfo

package com.ferbotz.aurapix.core.config

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * The live [RemoteConfig] for everything under the app root's gate. Screens read
 * `LocalRemoteConfig.current.features.<key>` to hide an entry point the backend has switched off,
 * so adding a switch never means threading a parameter through a dozen call sites.
 *
 * Outside the gate — `@Preview`s and tests — it is the compiled-in [RemoteConfig.Default], so
 * previews keep rendering with every feature on and nothing to stub.
 */
val LocalRemoteConfig: ProvidableCompositionLocal<RemoteConfig> =
    compositionLocalOf { RemoteConfig.Default }

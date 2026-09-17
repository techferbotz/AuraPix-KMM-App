package com.ferbotz.aurapix.shell.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.LifecycleStartEffect
import com.ferbotz.aurapix.core.config.LocalRemoteConfig
import com.ferbotz.aurapix.core.di.DataModule

/**
 * Remote-config gate around the **whole** UI, so there is exactly one place that can stop the app
 * and exactly one place that publishes the document to every screen.
 *
 * The config is fetched on every cold start and re-fetched on foreground once its TTL lapses;
 * until it lands the cached copy (or the compiled-in defaults) applies, so launch is never
 * blocked on it and a failed fetch is invisible. Order is fixed: maintenance, then a required
 * update — both of which *replace* the app — then the app itself, with a dismissible nudge over
 * it for a merely recommended update.
 */
@Composable
fun RemoteConfigGate(content: @Composable () -> Unit) {
    val viewModel = remember {
        RemoteConfigViewModel(DataModule.remoteConfigRepository, DataModule.buildInfo)
    }
    DisposableEffect(Unit) { onDispose { viewModel.onCleared() } }
    val state by viewModel.uiState.collectAsState()

    // Each return to the foreground re-checks the TTL; the cold-start fetch runs in the VM's init.
    LifecycleStartEffect(viewModel) {
        viewModel.onAppForegrounded()
        onStopOrDispose { }
    }

    CompositionLocalProvider(LocalRemoteConfig provides state.config) {
        when {
            state.isUnderMaintenance -> MaintenanceScreen(
                message = state.maintenanceMessage,
                onRetry = viewModel::retry,
            )

            state.isUpdateRequired -> UpdateRequiredScreen(
                message = state.updateMessage,
                storeUrl = state.storeUrl,
            )

            else -> {
                content()
                if (state.showUpdateNudge) {
                    UpdateNudgeDialog(
                        message = state.updateMessage,
                        storeUrl = state.storeUrl,
                        onDismiss = viewModel::dismissUpdateNudge,
                    )
                }
            }
        }
    }
}

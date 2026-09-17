package com.ferbotz.aurapix.shell.ui

import com.ferbotz.aurapix.core.config.AppBuildInfo
import com.ferbotz.aurapix.core.config.RemoteConfig
import com.ferbotz.aurapix.core.config.RemoteConfigRepository
import com.ferbotz.aurapix.core.config.RemoteConfigSnapshot
import com.ferbotz.aurapix.core.config.UpdateRequirement
import com.ferbotz.aurapix.core.ui.base.AuraViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * What [RemoteConfigGate] renders from the remote config. Deliberately universal — it exposes
 * only maintenance and update, never a per-app feature switch: those are read at the entry point
 * that draws them, through `LocalRemoteConfig`.
 */
data class RemoteConfigUiState(
    val config: RemoteConfig = RemoteConfig.Default,
    val isUnderMaintenance: Boolean = false,
    val maintenanceMessage: String = DEFAULT_MAINTENANCE_MESSAGE,
    val updateRequirement: UpdateRequirement = UpdateRequirement.NONE,
    val updateMessage: String? = null,
    val storeUrl: String? = null,
    val updateNudgeDismissed: Boolean = false,
) {
    val isUpdateRequired: Boolean
        get() = updateRequirement == UpdateRequirement.REQUIRED

    val showUpdateNudge: Boolean
        get() = updateRequirement == UpdateRequirement.RECOMMENDED && !updateNudgeDismissed

    companion object {
        /** Used when maintenance is on but the backend sent no copy. */
        const val DEFAULT_MAINTENANCE_MESSAGE: String =
            "AuraPix is down for a short spot of maintenance. Please check back in a few minutes."
    }
}

/**
 * Owns the remote-config lifecycle at the app root (BE-005):
 *
 * - **every cold start** fetches `GET /config` from [init] — the cached or compiled-in document
 *   is already live meanwhile, so launch never waits on the network;
 * - **returning to the foreground** re-fetches only once the document is older than its
 *   `ttlSeconds` ([onAppForegrounded]);
 * - the resulting [uiState] tells the gate whether to show maintenance, a forced update or a
 *   dismissible nudge, and carries the document every screen reads via `LocalRemoteConfig`.
 */
class RemoteConfigViewModel(
    private val repository: RemoteConfigRepository,
    private val buildInfo: AppBuildInfo,
) : AuraViewModel() {

    private val nudgeDismissed = MutableStateFlow(false)

    val uiState: StateFlow<RemoteConfigUiState> =
        combine(repository.snapshot, nudgeDismissed) { snapshot, dismissed ->
            snapshot.toUiState(dismissed)
        }.stateIn(
            scope,
            SharingStarted.Eagerly,
            repository.snapshot.value.toUiState(nudgeDismissed = false),
        )

    init {
        scope.launch { repository.refresh() }
    }

    /** App came to the foreground: re-fetch only if the TTL has lapsed. */
    fun onAppForegrounded() {
        scope.launch { repository.refreshIfStale() }
    }

    /** "Try again" on the maintenance screen — the only way out of it, so unconditional. */
    fun retry() {
        scope.launch { repository.refresh() }
    }

    /**
     * Dismiss the recommended-update nudge. Deliberately **not** persisted: once per process is
     * enough, and persisting it would mean a later nudge never reappears.
     */
    fun dismissUpdateNudge() {
        nudgeDismissed.value = true
    }

    private fun RemoteConfigSnapshot.toUiState(nudgeDismissed: Boolean): RemoteConfigUiState =
        RemoteConfigUiState(
            config = config,
            isUnderMaintenance = config.maintenance.enabled,
            maintenanceMessage = config.maintenance.message?.takeIf { it.isNotBlank() }
                ?: RemoteConfigUiState.DEFAULT_MAINTENANCE_MESSAGE,
            updateRequirement = config.update.requirementFor(buildInfo.buildNumber),
            updateMessage = config.update.message?.takeIf { it.isNotBlank() },
            storeUrl = config.update.storeUrlFor(buildInfo.platform)?.takeIf { it.isNotBlank() },
            updateNudgeDismissed = nudgeDismissed,
        )
}

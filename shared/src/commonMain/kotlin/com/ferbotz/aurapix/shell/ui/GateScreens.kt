package com.ferbotz.aurapix.shell.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.components.AmbientGlow
import com.ferbotz.aurapix.core.ui.components.BrandLogo
import com.ferbotz.aurapix.core.ui.components.PrimaryButton
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme

/**
 * The three screens [RemoteConfigGate] can put in front of the app. They share the Splash
 * treatment — darkest ground, ambient glow, brand mark — because they are the only other
 * full-bleed states where the app has nothing of its own to show yet.
 */

/** `maintenance.enabled` — the app shows this and loads nothing else. Retry re-fetches `/config`. */
@Composable
fun MaintenanceScreen(message: String, onRetry: () -> Unit) {
    GateScaffold(
        title = "Back shortly",
        message = message,
    ) {
        PrimaryButton("Try again", onRetry, Modifier.fillMaxWidth())
    }
}

/**
 * Below `update.minSupportedBuild` — a hard block with no dismiss.
 *
 * When [storeUrl] is null (no store listing yet) the screen shows the message **without** a
 * button rather than a dead link: a user who can't act on it is better served by an explanation
 * than by a button that does nothing.
 */
@Composable
fun UpdateRequiredScreen(message: String?, storeUrl: String?) {
    val uriHandler = LocalUriHandler.current
    GateScaffold(
        title = "Update required",
        message = message ?: "This version of AuraPix is no longer supported. Update to keep creating.",
    ) {
        if (storeUrl != null) {
            PrimaryButton("Update now", { uriHandler.openUri(storeUrl) }, Modifier.fillMaxWidth())
        }
    }
}

/** Below `update.recommendedBuild` — a nudge over the running app, dismissible for this process. */
@Composable
fun UpdateNudgeDialog(message: String?, storeUrl: String?, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update available") },
        text = {
            Text(message ?: "A newer version of AuraPix is ready, with the latest templates and fixes.")
        },
        confirmButton = {
            if (storeUrl != null) {
                TextButton(onClick = { uriHandler.openUri(storeUrl); onDismiss() }) { Text("Update") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Not now") } },
    )
}

/** Shared layout for the two full-screen gates. */
@Composable
private fun GateScaffold(
    title: String,
    message: String,
    action: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        contentAlignment = Alignment.Center,
    ) {
        AmbientGlow()
        Column(
            modifier = Modifier.safeContentPadding().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BrandLogo(size = 72.dp)
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            action()
        }
    }
}

@Preview
@Composable
private fun MaintenancePreview() {
    AuraPixTheme {
        MaintenanceScreen(RemoteConfigUiState.DEFAULT_MAINTENANCE_MESSAGE, onRetry = {})
    }
}

@Preview
@Composable
private fun UpdateRequiredPreview() {
    AuraPixTheme {
        UpdateRequiredScreen(message = null, storeUrl = "https://play.google.com/store")
    }
}

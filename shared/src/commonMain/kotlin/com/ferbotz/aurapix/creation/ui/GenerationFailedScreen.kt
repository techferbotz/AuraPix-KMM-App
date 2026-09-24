package com.ferbotz.aurapix.creation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.data.remote.ApiError
import com.ferbotz.aurapix.core.ui.components.AmbientGlow
import com.ferbotz.aurapix.core.ui.components.GlassCard
import com.ferbotz.aurapix.core.ui.components.PrimaryButton
import com.ferbotz.aurapix.core.ui.components.SecondaryButton
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme

/**
 * Where a generation lands when it doesn't produce an image. It says what actually went wrong —
 * the API's own `failureReason` when there is one — and offers only a way out that can work:
 *
 *  - **Try again** after a run that `FAILED`, labelled with its price, since that's a new
 *    generation. After a request that never started, it just resends it.
 *  - **Check again** when the generation started but checking on it failed. Free: it polls the
 *    same creation.
 *  - **Back to template** when retrying the same request can't succeed — the photos don't fit the
 *    template, or the user was signed out.
 *
 * It never claims gems weren't used unless the request was refused outright: a gem is taken
 * when a generation starts (§4.10), and nothing in the contract says a failed one is refunded.
 *
 * [failure] is null when the app no longer knows what happened — the process was restarted on
 * this screen — and only Go Home is offered. Without [onRetry] (a failed creation opened from My
 * Creations, whose photos are gone) the way back is through the template.
 */
@Composable
fun GenerationFailedScreen(
    failure: GenerationFailure?,
    modifier: Modifier = Modifier,
    generationCost: Int = 0,
    onRetry: (() -> Unit)? = null,
    onOpenTemplate: () -> Unit = {},
    onGoHome: () -> Unit = {},
) {
    val copy = failure.copy(generationCost)
    Box(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        AmbientGlow(color = MaterialTheme.colorScheme.error)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(copy.icon, null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(48.dp))
            }
            Text(
                copy.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                copy.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            copy.detail?.let { detail ->
                GlassCard(Modifier.fillMaxWidth()) {
                    Text("What happened", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            val retry = copy.retryLabel?.let { label -> onRetry?.let { label to it } }
            when {
                retry != null -> {
                    PrimaryButton(retry.first, retry.second, Modifier.fillMaxWidth(), leadingIcon = Icons.Rounded.Refresh)
                    SecondaryButton("Go Home", onGoHome, Modifier.fillMaxWidth())
                }
                // A retry that can't happen here still has a way through the template.
                copy.retryLabel != null || copy.backToTemplate -> {
                    val label = if (onRetry == null) "Try this template again" else "Back to template"
                    PrimaryButton(label, onOpenTemplate, Modifier.fillMaxWidth())
                    SecondaryButton("Go Home", onGoHome, Modifier.fillMaxWidth())
                }
                else -> PrimaryButton("Go Home", onGoHome, Modifier.fillMaxWidth())
            }

            copy.reference?.let { id ->
                // Selectable, so it can be pasted into a support email.
                SelectionContainer {
                    Text(
                        "Reference: $id",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

/** What the screen says about one [GenerationFailure], and which way out it offers. */
private class FailureCopy(
    val icon: ImageVector,
    val title: String,
    val message: String,
    val detail: String? = null,
    /** The in-place retry's label, or null when sending the same request again can't help. */
    val retryLabel: String? = null,
    /** Send the user back to the template, to change what they submit or sign in again. */
    val backToTemplate: Boolean = false,
    /** The creation id, for support. */
    val reference: String? = null,
)

private fun GenerationFailure?.copy(cost: Int): FailureCopy = when (this) {
    is GenerationFailure.Failed -> FailureCopy(
        icon = Icons.Rounded.ErrorOutline,
        title = "Generation failed",
        message = "Something went wrong while creating your portrait. The attempt stays in My Creations.",
        detail = reason,
        retryLabel = if (cost > 0) "Try again · $cost gems" else "Try again",
        reference = creationId,
    )
    is GenerationFailure.Unconfirmed -> FailureCopy(
        icon = if (error is ApiError.NetworkError) Icons.Rounded.WifiOff else Icons.Rounded.HourglassEmpty,
        title = if (error is ApiError.NetworkError) "Connection lost" else "Couldn't get your result",
        message = "Your portrait may still be on its way. Checking again is free, and it will " +
            "appear in My Creations once it's done.",
        retryLabel = "Check again",
        reference = creationId,
    )
    is GenerationFailure.NotStarted -> error.notStartedCopy()
    null -> FailureCopy(
        icon = Icons.Rounded.ErrorOutline,
        title = "Generation failed",
        message = "Something went wrong. Pick the template again to start over.",
    )
}

/** The request never started a generation. A refusal (4xx) says so: no gem was taken. */
private fun ApiError.notStartedCopy(): FailureCopy = when (this) {
    is ApiError.NetworkError -> FailureCopy(
        icon = Icons.Rounded.WifiOff,
        title = "Couldn't reach AuraPix",
        message = "Check your connection and try again.",
        retryLabel = "Try again",
    )
    ApiError.TemplateDisabled -> FailureCopy(
        icon = Icons.Rounded.PauseCircle,
        title = "Temporarily unavailable",
        message = "This template can't be used right now. Try again later — no gems were used.",
    )
    ApiError.TemplateNotFound -> FailureCopy(
        icon = Icons.Rounded.SearchOff,
        title = "Template unavailable",
        message = "This template isn't available anymore. No gems were used.",
    )
    ApiError.InvalidImageCount -> FailureCopy(
        icon = Icons.Rounded.AddPhotoAlternate,
        title = "Check your photos",
        message = "This template needs one photo for each slot. No gems were used.",
        backToTemplate = true,
    )
    is ApiError.InvalidRequest -> FailureCopy(
        icon = Icons.Rounded.ErrorOutline,
        title = "Couldn't start",
        message = "The request was turned down, so no gems were used.",
        detail = message,
        backToTemplate = true,
    )
    ApiError.Unauthorized, ApiError.InvalidToken -> FailureCopy(
        icon = Icons.Rounded.Lock,
        title = "You've been signed out",
        message = "Sign in again to generate. No gems were used.",
        backToTemplate = true,
    )
    // Normally the paywall catches this before the screen is reached.
    ApiError.InsufficientCredits -> FailureCopy(
        icon = Icons.Rounded.Diamond,
        title = "Out of gems",
        message = "Top up to keep creating.",
        backToTemplate = true,
    )
    else -> FailureCopy(
        icon = Icons.Rounded.ErrorOutline,
        title = "Generation failed",
        message = "Something went wrong on our side. Please try again.",
        retryLabel = "Try again",
    )
}

@Preview
@Composable
private fun GenerationFailedScreenPreview() {
    AuraPixTheme {
        GenerationFailedScreen(
            failure = GenerationFailure.Failed(
                creationId = "0b1f2a64-6c1e-4f7e-9d0e-3c55a8d4e9b2",
                reason = "The model couldn't find a face in the photo.",
            ),
            generationCost = 10,
            onRetry = {},
        )
    }
}

@Preview
@Composable
private fun GenerationUnconfirmedPreview() {
    AuraPixTheme(darkTheme = false) {
        GenerationFailedScreen(
            failure = GenerationFailure.Unconfirmed("0b1f2a64-6c1e-4f7e-9d0e-3c55a8d4e9b2", ApiError.NetworkError(Exception())),
            onRetry = {},
        )
    }
}

@Preview
@Composable
private fun TemplateDisabledPreview() {
    AuraPixTheme { GenerationFailedScreen(failure = GenerationFailure.NotStarted(ApiError.TemplateDisabled), onRetry = {}) }
}

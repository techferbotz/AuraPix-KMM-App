package com.ferbotz.aurapix.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.components.AmbientGlow
import com.ferbotz.aurapix.core.ui.components.AuraIconButton
import com.ferbotz.aurapix.core.ui.components.AuraListRow
import com.ferbotz.aurapix.core.ui.components.AuraTopBar
import com.ferbotz.aurapix.core.ui.components.GlassCard
import com.ferbotz.aurapix.core.ui.components.PrimaryButton
import com.ferbotz.aurapix.core.ui.components.SecondaryButton
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme
import com.ferbotz.aurapix.core.ui.theme.AuraShapes
import com.ferbotz.aurapix.core.ui.theme.AuraTheme

/**
 * Settings → Delete Account: the confirmation the public deletion page walks Play reviewers
 * through — "Read the confirmation message and tap Delete to confirm" — so the button says
 * exactly **Delete** (API.md §4.2a).
 *
 * Says what the contract requires it to say: that deletion is immediate and permanent, that
 * creations and gems go with it and nothing is refunded, where the full list lives, and — while
 * [DeleteAccountUiState.manageSubscriptionUrl] is set — that the store subscription keeps billing.
 *
 * [signedIn] is observed rather than assumed: a rejected token drops the session mid-screen
 * (BE-008), and then there is nothing to delete until the user signs back in.
 */
@Composable
fun DeleteAccountScreen(
    modifier: Modifier = Modifier,
    email: String? = null,
    gems: Int = 0,
    signedIn: Boolean = true,
    state: DeleteAccountUiState = DeleteAccountUiState(),
    onBack: () -> Unit = {},
    onDelete: () -> Unit = {},
    onSignIn: () -> Unit = {},
    onWhatGetsDeleted: () -> Unit = {},
    onManageSubscription: () -> Unit = {},
) {
    val busy = state.step == DeleteAccountStep.Deleting || state.step == DeleteAccountStep.Deleted
    // A successful deletion signs out too; only a sign-out that didn't come from it counts here.
    val signedOut = !signedIn && !busy

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AuraTopBar(
                title = "Delete Account",
                navigationIcon = {
                    AuraIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", { if (!busy) onBack() })
                },
            )
        },
        bottomBar = {
            // Stacked at equal width, never an unequal pair side by side.
            Column(
                Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (signedOut) {
                    PrimaryButton("Sign in", onSignIn, Modifier.fillMaxWidth())
                } else {
                    PrimaryButton(
                        text = "Delete",
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                        loading = busy,
                        containerBrush = null,
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                        glowColor = MaterialTheme.colorScheme.error,
                    )
                }
                SecondaryButton("Cancel", onBack, Modifier.fillMaxWidth(), enabled = !busy)
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.DeleteForever,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(40.dp),
                    )
                }
                Text(
                    "Delete your account?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    if (email.isNullOrBlank()) "This permanently deletes your AuraPix account."
                    else "This permanently deletes the AuraPix account for $email.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            GlassCard(Modifier.fillMaxWidth()) {
                ConsequenceRow(Icons.Rounded.ErrorOutline, "It happens immediately and permanently, and it can't be undone.")
                ConsequenceRow(Icons.Rounded.PhotoLibrary, "All your creations are deleted.")
                ConsequenceRow(Icons.Rounded.Diamond, gemsLine(gems))
            }

            GlassCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp)) {
                AuraListRow(
                    "What gets deleted",
                    subtitle = "What's removed, what's kept, and for how long",
                    leadingIcon = Icons.Rounded.Info,
                    trailing = {
                        Icon(Icons.AutoMirrored.Rounded.OpenInNew, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    onClick = onWhatGetsDeleted,
                )
            }

            if (state.manageSubscriptionUrl != null) {
                SubscriptionBillingWarning(
                    "Deleting your account doesn't cancel AuraPix Premium. Google Play keeps " +
                        "charging you until you cancel it there, and renewals after deletion grant nothing.",
                    onManageSubscription,
                )
            }

            when {
                signedOut -> InlineError("You've been signed out, so nothing was deleted. Sign in again to delete your account.")
                state.step is DeleteAccountStep.Failed -> InlineError(state.step.message)
            }
        }
    }
}

/**
 * Shown once the account is gone. The app carries on as a guest from here, and the same Google
 * account can sign in again later to a new, empty account (§4.2a).
 */
@Composable
fun AccountDeletedScreen(
    modifier: Modifier = Modifier,
    subscriptionStillBilling: Boolean = false,
    onManageSubscription: () -> Unit = {},
    onDone: () -> Unit = {},
) {
    Box(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        AmbientGlow()
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
                Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
            }
            Text("Account deleted", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "Your AuraPix account, creations and gems have been permanently deleted. You can keep " +
                    "browsing as a guest — signing in with Google again starts a new, empty account.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (subscriptionStillBilling) {
                SubscriptionBillingWarning(
                    "Deleting your account didn't cancel AuraPix Premium. Google Play keeps charging " +
                        "you until you cancel it there.",
                    onManageSubscription,
                )
            }
            PrimaryButton("Done", onDone, Modifier.fillMaxWidth().padding(top = 8.dp))
        }
    }
}

@Composable
private fun ConsequenceRow(icon: ImageVector, text: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

/** Amber, not red: nothing has failed — it's a bill the user may not know is still coming. */
@Composable
private fun SubscriptionBillingWarning(text: String, onManageSubscription: () -> Unit) {
    val warning = AuraTheme.colors.warning
    val shape = AuraShapes.large
    Column(
        Modifier.fillMaxWidth()
            .clip(shape)
            .background(warning.copy(alpha = 0.10f))
            .border(1.dp, warning.copy(alpha = 0.45f), shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Rounded.WarningAmber, null, tint = warning, modifier = Modifier.size(20.dp))
            Text("Your subscription keeps billing", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SecondaryButton("Manage in Google Play", onManageSubscription, Modifier.fillMaxWidth())
    }
}

@Composable
private fun InlineError(message: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Rounded.WarningAmber, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
    }
}

private fun gemsLine(gems: Int): String = when {
    gems > 1 -> "Your $gems gems are lost, with no refund."
    gems == 1 -> "Your 1 gem is lost, with no refund."
    else -> "Any gems on the account are lost, with no refund."
}

@Preview
@Composable
private fun DeleteAccountScreenDarkPreview() {
    AuraPixTheme(darkTheme = true) {
        DeleteAccountScreen(
            email = "julian.vane@gmail.com",
            gems = 120,
            state = DeleteAccountUiState(manageSubscriptionUrl = playSubscriptionsUrl("aurapix_premium")),
        )
    }
}

@Preview
@Composable
private fun DeleteAccountScreenLightErrorPreview() {
    AuraPixTheme(darkTheme = false) {
        DeleteAccountScreen(
            email = "julian.vane@gmail.com",
            gems = 1,
            state = DeleteAccountUiState(step = DeleteAccountStep.Failed("Something went wrong, and nothing was deleted. Please try again.")),
        )
    }
}

@Preview
@Composable
private fun AccountDeletedScreenPreview() {
    AuraPixTheme(darkTheme = true) { AccountDeletedScreen(subscriptionStillBilling = true) }
}

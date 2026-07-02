package com.ferbotz.aurapix.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.base.UiState
import com.ferbotz.aurapix.core.ui.base.userMessage
import com.ferbotz.aurapix.core.ui.components.AuraBottomBar
import com.ferbotz.aurapix.core.ui.components.AuraIconButton
import com.ferbotz.aurapix.core.ui.components.AuraListRow
import com.ferbotz.aurapix.core.ui.components.AuraTab
import com.ferbotz.aurapix.core.ui.components.AuraTopBar
import com.ferbotz.aurapix.core.ui.components.Avatar
import com.ferbotz.aurapix.core.ui.components.GlassCard
import com.ferbotz.aurapix.core.ui.components.PrimaryButton
import com.ferbotz.aurapix.core.ui.components.StatusBadge
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme

/** User profile: header, plan/credit overview, upgrade CTA and account menu — driven by the real profile. */
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    state: UiState<ProfileUi> = UiState.Success(sampleProfile),
    onUpgrade: () -> Unit = {},
    onPurchaseCredits: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onHelp: () -> Unit = {},
    onPrivacyPolicy: () -> Unit = {},
    onLogout: () -> Unit = {},
    onRetry: () -> Unit = {},
    selectedTab: AuraTab = AuraTab.Profile,
    onSelectTab: (AuraTab) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AuraTopBar(
                title = "Profile",
                actions = { AuraIconButton(Icons.Rounded.Settings, "Settings", onOpenSettings) },
            )
        },
        bottomBar = { AuraBottomBar(selected = selectedTab, onSelect = onSelectTab) },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (state) {
                is UiState.Loading, UiState.Idle ->
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary,
                    )

                is UiState.Error ->
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            state.error.userMessage(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        PrimaryButton("Retry", onClick = onRetry, modifier = Modifier.width(160.dp))
                    }

                is UiState.Success -> ProfileContent(
                    profile = state.data,
                    onUpgrade = onUpgrade,
                    onPurchaseCredits = onPurchaseCredits,
                    onOpenSettings = onOpenSettings,
                    onHelp = onHelp,
                    onPrivacyPolicy = onPrivacyPolicy,
                    onLogout = onLogout,
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    profile: ProfileUi,
    onUpgrade: () -> Unit,
    onPurchaseCredits: () -> Unit,
    onOpenSettings: () -> Unit,
    onHelp: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.size(8.dp))
        Box(contentAlignment = Alignment.BottomCenter) {
            Avatar(
                size = 96.dp,
                imageUrl = profile.avatarUrl,
                initials = profile.name.firstOrNull()?.uppercase(),
                border = true,
            )
            if (profile.isPremium) StatusBadge("Pro")
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(profile.name, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
            if (profile.isPremium) {
                Icon(Icons.Rounded.Verified, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }
        Text(profile.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        // Plan + credits overview
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassCard(Modifier.weight(1f)) {
                Text("Current Plan", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    profile.subscriptionLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            GlassCard(Modifier.weight(1f)) {
                Text("Gems", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Icon(Icons.Rounded.Diamond, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text("${profile.totalCredits}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // Upgrade CTA — only when not already premium
        if (!profile.isPremium) {
            GlassCard(glow = true, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Rounded.WorkspacePremium, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Upgrade to Premium", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text("Unlock all styles & priority speed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                PrimaryButton("Upgrade", onUpgrade, Modifier.fillMaxWidth().padding(top = 12.dp))
            }
        }

        // Menu
        GlassCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp)) {
            AuraListRow("Purchase Credits", leadingIcon = Icons.Rounded.ShoppingCart, onClick = onPurchaseCredits)
            AuraListRow("Settings", leadingIcon = Icons.Rounded.Settings, onClick = onOpenSettings)
            AuraListRow("Help Center", leadingIcon = Icons.AutoMirrored.Rounded.HelpOutline, onClick = onHelp)
            AuraListRow("Privacy Policy", leadingIcon = Icons.Rounded.PrivacyTip, onClick = onPrivacyPolicy)
            AuraListRow(
                "Logout",
                leadingIcon = Icons.AutoMirrored.Rounded.Logout,
                tint = MaterialTheme.colorScheme.error,
                onClick = onLogout,
            )
        }

        Text(
            "AuraPix v1.0",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        )
    }
}

private val sampleProfile = ProfileUi(
    name = "Julian Vane",
    email = "julian@example.com",
    avatarUrl = null,
    totalCredits = 50,
    subscriptionLabel = "Premium",
    isPremium = true,
)

@Preview
@Composable
private fun ProfileScreenPreview() {
    AuraPixTheme { ProfileScreen() }
}

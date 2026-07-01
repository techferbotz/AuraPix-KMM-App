package com.ferbotz.aurapix.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.ferbotz.aurapix.core.ui.theme.AuraShapes

/** User profile: header, plan/credit overview, upgrade CTA and account menu. */
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    name: String = "Julian Vane",
    credits: Int = 50,
    creditsCap: Int = 500,
    onUpgrade: () -> Unit = {},
    onPurchaseCredits: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onHelp: () -> Unit = {},
    onPrivacyPolicy: () -> Unit = {},
    onLogout: () -> Unit = {},
    selectedTab: AuraTab = AuraTab.Profile,
    onSelectTab: (AuraTab) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AuraTopBar(
                title = "Profile",
                actions = {
                    AuraIconButton(Icons.Rounded.Settings, "Settings", onOpenSettings)
                },
            )
        },
        bottomBar = { AuraBottomBar(selected = selectedTab, onSelect = onSelectTab) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer8()
            Box(contentAlignment = Alignment.BottomCenter) {
                Avatar(size = 96.dp, border = true)
                StatusBadge("Pro", Modifier.padding(bottom = 0.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(name, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                Icon(Icons.Rounded.Verified, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Text("Premium Member", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Plan + credits overview
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassCard(Modifier.weight(1f)) {
                    Text("Current Plan", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Pro Plan", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 4.dp))
                    Text("Renews in 12 days", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                GlassCard(Modifier.weight(1f)) {
                    Text("Credits", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$credits / $creditsCap", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 4.dp))
                    LinearProgressIndicator(
                        progress = { credits.toFloat() / creditsCap },
                        modifier = Modifier.fillMaxWidth().height(6.dp).padding(top = 8.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    )
                }
            }

            // Upgrade CTA
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

            // Menu
            GlassCard(Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)) {
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
                "AuraPix v1.0.4",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun Spacer8() = Box(Modifier.size(8.dp))

@Preview
@Composable
private fun ProfileScreenPreview() {
    AuraPixTheme { ProfileScreen() }
}

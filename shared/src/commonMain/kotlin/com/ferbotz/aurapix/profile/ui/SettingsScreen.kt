package com.ferbotz.aurapix.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.components.AuraIconButton
import com.ferbotz.aurapix.core.ui.components.AuraListRow
import com.ferbotz.aurapix.core.ui.components.AuraToggle
import com.ferbotz.aurapix.core.ui.components.AuraTopBar
import com.ferbotz.aurapix.core.ui.components.Avatar
import com.ferbotz.aurapix.core.ui.components.GlassCard
import com.ferbotz.aurapix.core.ui.components.OverlineLabel
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme

/**
 * App & account settings, grouped into glass sections. Account is driven by the real user, and
 * [darkTheme] by `ThemeManager` — the switch here is the only way to change the app's appearance,
 * since the theme deliberately ignores the device's night setting.
 *
 * Carries the house set of legal entries (API.md §4.17b): Privacy Policy, Terms & Conditions,
 * Contact support, and — signed in, and only when [onDeleteAccount] is given — Delete Account.
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    name: String = "",
    email: String = "",
    avatarUrl: String? = null,
    signedIn: Boolean = true,
    supportEmail: String = "",
    darkTheme: Boolean = true,
    onDarkThemeChange: (Boolean) -> Unit = {},
    /** False where push isn't set up (iOS today): the Notifications section is left out. */
    pushSupported: Boolean = true,
    /** The system's own answer — permission granted and not switched off in Settings. */
    pushEnabled: Boolean = false,
    onPushNotifications: () -> Unit = {},
    onBack: () -> Unit = {},
    onPrivacyPolicy: () -> Unit = {},
    onTerms: () -> Unit = {},
    onContactSupport: () -> Unit = {},
    onLogout: () -> Unit = {},
    /** Null hides the Delete Account row. */
    onDeleteAccount: (() -> Unit)? = null,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AuraTopBar(
                title = "Settings",
                navigationIcon = { AuraIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            SettingsSection("Account") {
                AuraListRow(
                    title = name.ifBlank { "Account" },
                    subtitle = email.ifBlank { null },
                    leadingContent = { Avatar(imageUrl = avatarUrl, size = 40.dp) },
                    trailing = {
                        Text("Connected", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    },
                )
                AuraListRow(
                    "Logout",
                    leadingIcon = Icons.AutoMirrored.Rounded.Logout,
                    tint = MaterialTheme.colorScheme.error,
                    onClick = onLogout,
                )
                // Shown only when the caller passes [onDeleteAccount]; today it doesn't, so the row
                // is hidden. Play expects an in-app deletion path for apps with accounts, and the
                // public deletion page names this row — Profile tab → Settings → Delete Account →
                // Delete — so keep the label as is when it comes back (§4.2a).
                if (signedIn && onDeleteAccount != null) {
                    AuraListRow(
                        "Delete Account",
                        leadingIcon = Icons.Rounded.DeleteForever,
                        tint = MaterialTheme.colorScheme.error,
                        onClick = onDeleteAccount,
                    )
                }
            }

            SettingsSection("Appearance") {
                AuraListRow(
                    "Dark Theme",
                    subtitle = if (darkTheme) "Violet on near-black" else "Violet on the logo's lavender",
                    leadingIcon = if (darkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                    trailing = { AuraToggle(darkTheme, onDarkThemeChange) },
                )
            }

            // The switch mirrors the system's; the app can't flip that itself, so a tap asks for
            // the permission, or opens the system settings where notifications are turned on or off.
            if (pushSupported) {
                SettingsSection("Notifications") {
                    AuraListRow(
                        "Push Notifications",
                        leadingIcon = Icons.Rounded.NotificationsActive,
                        trailing = { AuraToggle(pushEnabled, { onPushNotifications() }) },
                        onClick = onPushNotifications,
                    )
                }
            }

            SettingsSection("Preferences") {
                AuraListRow(
                    "Language",
                    trailing = {
                        Text("English (US)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    onClick = {},
                )
            }

            SettingsSection("Support") {
                AuraListRow(
                    "Contact support",
                    subtitle = supportEmail.ifBlank { null },
                    leadingIcon = Icons.Rounded.SupportAgent,
                    trailing = { OpensOutside() },
                    onClick = onContactSupport,
                )
            }

            SettingsSection("Legal") {
                AuraListRow("Privacy Policy", trailing = { OpensOutside() }, onClick = onPrivacyPolicy)
                AuraListRow("Terms & Conditions", trailing = { OpensOutside() }, onClick = onTerms)
            }

            SettingsSection("About") {
                AuraListRow(
                    "Version",
                    trailing = { Text("v1.0.4", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                )
            }
        }
    }
}

/** Trailing mark for a row that leaves the screen for a browser tab or the mail app. */
@Composable
private fun OpensOutside() {
    Icon(Icons.AutoMirrored.Rounded.OpenInNew, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OverlineLabel(title, Modifier.padding(start = 4.dp))
        GlassCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp)) {
            content()
        }
    }
}

@Preview
@Composable
private fun SettingsScreenLightPreview() {
    AuraPixTheme(darkTheme = false) {
        SettingsScreen(name = "Julian Vane", email = "julian.vane@gmail.com", supportEmail = "support@ferbotz.com", darkTheme = false)
    }
}

@Preview
@Composable
private fun SettingsScreenDarkPreview() {
    AuraPixTheme(darkTheme = true) {
        SettingsScreen(name = "Julian Vane", email = "julian.vane@gmail.com", supportEmail = "support@ferbotz.com", darkTheme = true)
    }
}

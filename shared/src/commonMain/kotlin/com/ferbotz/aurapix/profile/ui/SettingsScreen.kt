package com.ferbotz.aurapix.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.components.AuraIconButton
import com.ferbotz.aurapix.core.ui.components.AuraListRow
import com.ferbotz.aurapix.core.ui.components.AuraToggle
import com.ferbotz.aurapix.core.ui.components.AuraTopBar
import com.ferbotz.aurapix.core.ui.components.Avatar
import com.ferbotz.aurapix.core.ui.components.CategoryChip
import com.ferbotz.aurapix.core.ui.components.GlassCard
import com.ferbotz.aurapix.core.ui.components.OverlineLabel
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme

/** App & account settings, grouped into glass sections with toggles. */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onPrivacyPolicy: () -> Unit = {},
    onTerms: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    var pushNotifications by remember { mutableStateOf(true) }
    var newTemplates by remember { mutableStateOf(false) }
    var darkTheme by remember { mutableStateOf(true) }

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
                    title = "Julian Vane",
                    subtitle = "julian.vane@gmail.com",
                    leadingContent = { Avatar(size = 40.dp) },
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
            }

            SettingsSection("Notifications") {
                AuraListRow(
                    "Push Notifications",
                    leadingIcon = Icons.Rounded.NotificationsActive,
                    trailing = { AuraToggle(pushNotifications, { pushNotifications = it }) },
                )
                AuraListRow(
                    "New Templates",
                    leadingIcon = Icons.Rounded.Style,
                    trailing = { AuraToggle(newTemplates, { newTemplates = it }) },
                )
            }

            SettingsSection("Appearance") {
                AuraListRow(
                    "Theme",
                    subtitle = "Choose your preferred look",
                    trailing = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CategoryChip("Dark", selected = darkTheme, onClick = { darkTheme = true })
                            CategoryChip("Light", selected = !darkTheme, onClick = { darkTheme = false })
                        }
                    },
                )
                AuraListRow(
                    "Language",
                    trailing = {
                        Text("English (US)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    onClick = {},
                )
            }

            SettingsSection("Legal") {
                AuraListRow("Privacy Policy", onClick = onPrivacyPolicy)
                AuraListRow(
                    "Terms of Service",
                    trailing = { Icon(Icons.AutoMirrored.Rounded.OpenInNew, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    onClick = onTerms,
                )
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
private fun SettingsScreenPreview() {
    AuraPixTheme { SettingsScreen() }
}

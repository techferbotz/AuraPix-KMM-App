package com.ferbotz.aurapix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.ui.components.AmbientGlow
import com.ferbotz.aurapix.ui.components.ConfettiOverlay
import com.ferbotz.aurapix.ui.components.GlassCard
import com.ferbotz.aurapix.ui.components.PrimaryButton
import com.ferbotz.aurapix.ui.theme.AuraPixTheme
import com.ferbotz.aurapix.ui.theme.AuraTheme

/** Confirmation shown after upgrading to Premium. */
@Composable
fun SubscriptionSuccessScreen(
    modifier: Modifier = Modifier,
    onContinue: () -> Unit = {},
) {
    val accent = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        AmbientGlow()
        ConfettiOverlay()
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                Modifier.size(112.dp).clip(CircleShape)
                    .background(AuraTheme.colors.glassSurface)
                    .border(1.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.WorkspacePremium, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp))
            }
            Text(
                buildAnnotatedString {
                    append("Welcome to AuraPix ")
                    withStyle(SpanStyle(color = accent)) { append("Premium") }
                },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                "You now have unlimited access to every style, priority rendering and commercial rights.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BenefitCard(Icons.Rounded.Bolt, "Priority Access", "10× faster renders", Modifier.weight(1f))
                BenefitCard(Icons.Rounded.Palette, "Exclusive Styles", "50+ premium models", Modifier.weight(1f))
            }

            PrimaryButton(
                "Continue to Studio",
                onContinue,
                Modifier.fillMaxWidth().padding(top = 8.dp),
                trailingIcon = Icons.AutoMirrored.Rounded.ArrowForward,
            )
            Text(
                "Subscription active · v1.0.4",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun BenefitCard(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
    GlassCard(modifier) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 8.dp))
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview
@Composable
private fun SubscriptionSuccessScreenPreview() {
    AuraPixTheme { SubscriptionSuccessScreen() }
}

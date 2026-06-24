package com.ferbotz.aurapix.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.ui.components.AuraIconButton
import com.ferbotz.aurapix.ui.components.AuraTopBar
import com.ferbotz.aurapix.ui.components.GlassCard
import com.ferbotz.aurapix.ui.components.PricingCard
import com.ferbotz.aurapix.ui.theme.AuraPixTheme

/** Subscription tiers. Reuses [PricingCard]; the yearly tier is the highlighted plan. */
@Composable
fun PremiumPlansScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onChoosePlan: (String) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AuraTopBar(
                title = "Premium Plans",
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
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Unlock your full potential", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Go Premium for unlimited styles, faster rendering and commercial rights.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            PricingCard(
                title = "Monthly",
                price = "$29",
                period = "month",
                features = listOf("Unlimited styles", "HD downloads", "No watermark"),
                ctaText = "Choose Monthly",
                onClick = { onChoosePlan("monthly") },
                modifier = Modifier.fillMaxWidth(),
            )
            PricingCard(
                title = "Yearly",
                price = "$240",
                period = "year",
                features = listOf("Everything in Monthly", "Commercial usage license", "Early access to new styles", "Priority rendering"),
                ctaText = "Choose Yearly",
                onClick = { onChoosePlan("yearly") },
                modifier = Modifier.fillMaxWidth(),
                highlighted = true,
                badgeText = "Best Value",
            )

            Text("Every plan includes", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureBento(Icons.Rounded.Bolt, "Instant Render", Modifier.weight(1f))
                FeatureBento(Icons.Rounded.Lock, "Private Mode", Modifier.weight(1f))
                FeatureBento(Icons.Rounded.Tune, "Advanced Tools", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FeatureBento(icon: ImageVector, title: String, modifier: Modifier = Modifier) {
    GlassCard(modifier) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Preview
@Composable
private fun PremiumPlansScreenPreview() {
    AuraPixTheme { PremiumPlansScreen() }
}

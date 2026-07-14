package com.ferbotz.aurapix.billing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.ferbotz.aurapix.core.ui.components.AmbientGlow
import com.ferbotz.aurapix.core.ui.components.AuraIconButton
import com.ferbotz.aurapix.core.ui.components.AuraTopBar
import com.ferbotz.aurapix.core.ui.components.PricingCard
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme
import com.ferbotz.aurapix.core.ui.theme.AuraTheme
import com.ferbotz.aurapix.core.ui.theme.redGlow

/** The single Premium subscription: removes ads and grants 100 gems every month. Gold-accented. */
@Composable
fun PremiumPlansScreen(
    modifier: Modifier = Modifier,
    plan: BillingPlan? = null,
    loading: Boolean = false,
    error: String? = null,
    purchasing: Boolean = false,
    onBack: () -> Unit = {},
    onSubscribe: () -> Unit = {},
    onRetry: () -> Unit = {},
) {
    val gold = AuraTheme.colors.premium
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AuraTopBar(
                title = "Premium",
                navigationIcon = { AuraIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack) },
            )
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
            Box(Modifier.fillMaxWidth().height(232.dp)) {
                AmbientGlow(color = gold, alpha = 0.22f)
                Column(
                    Modifier.align(Alignment.Center).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        Modifier.size(96.dp)
                            .redGlow(CircleShape, elevation = 26.dp, color = gold)
                            .clip(CircleShape)
                            .background(AuraTheme.colors.glassSurface)
                            .border(1.dp, gold, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.WorkspacePremium, null, tint = gold, modifier = Modifier.size(48.dp))
                    }
                    Text(
                        "Unlock AuraPix Premium",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        "Go unlimited. Cancel anytime.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            when {
                loading -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                plan != null -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PricingCard(
                        title = "Premium",
                        price = plan.priceLabel,
                        period = "month",
                        features = listOf(
                            "No ads",
                            "${plan.gems} gems every month",
                            "Priority rendering (10× faster)",
                            "Exclusive premium styles",
                            "Commercial usage rights",
                        ),
                        ctaText = "Go Premium",
                        onClick = onSubscribe,
                        modifier = Modifier.fillMaxWidth(),
                        highlighted = true,
                        ctaEnabled = !purchasing,
                        ctaLoading = purchasing,
                        accentColor = gold,
                        onAccentColor = AuraTheme.colors.onPremium,
                    )
                    Text(
                        "Cancel anytime · billed monthly",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                else -> BillingMessage(error ?: "No plans available right now.", onRetry)
            }
        }
    }
}

@Preview
@Composable
private fun PremiumPlansScreenPreview() {
    AuraPixTheme {
        PremiumPlansScreen(
            plan = BillingPlan("monthly", "monthly", "₹99", gems = 100, isSubscription = true, highlighted = true),
        )
    }
}

package com.ferbotz.aurapix.billing.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.components.AuraIconButton
import com.ferbotz.aurapix.core.ui.components.AuraTopBar
import com.ferbotz.aurapix.core.ui.components.PricingCard
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme

/** The single Premium subscription: removes ads and grants 100 gems every month. */
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Unlock your full potential", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Go Premium to remove ads and get 100 gems every month.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            when {
                loading -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                plan != null -> PricingCard(
                    title = "Premium",
                    price = plan.priceLabel,
                    period = "month",
                    features = listOf("No ads", "${plan.gems} gems every month"),
                    ctaText = "Go Premium",
                    onClick = onSubscribe,
                    modifier = Modifier.fillMaxWidth(),
                    highlighted = true,
                    ctaEnabled = !purchasing,
                    ctaLoading = purchasing,
                )

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

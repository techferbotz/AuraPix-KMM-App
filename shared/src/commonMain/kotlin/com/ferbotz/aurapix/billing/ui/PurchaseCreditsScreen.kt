package com.ferbotz.aurapix.billing.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.ferbotz.aurapix.core.ui.components.CreditsBadge
import com.ferbotz.aurapix.core.ui.components.PricingCard
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme

/** Gem packs from the live RevenueCat offering, laid out in a 2-column grid. */
@Composable
fun PurchaseCreditsScreen(
    modifier: Modifier = Modifier,
    credits: Int = 0,
    packs: List<BillingPlan> = emptyList(),
    loading: Boolean = false,
    error: String? = null,
    purchasingProductId: String? = null,
    onBack: () -> Unit = {},
    onSelectPack: (BillingPlan) -> Unit = {},
    onRetry: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AuraTopBar(
                title = "Purchase Gems",
                navigationIcon = { AuraIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack) },
                actions = { CreditsBadge(credits) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Gems never expire. Buy a pack and start creating.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            when {
                loading -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                packs.isEmpty() -> BillingMessage(error ?: "No gem packs available right now.", onRetry)

                else -> packs.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        row.forEach { pack ->
                            PricingCard(
                                title = "${pack.gems} Gems",
                                price = pack.priceLabel,
                                features = listOf("Never expires"),
                                ctaText = "Select",
                                onClick = { onSelectPack(pack) },
                                modifier = Modifier.weight(1f),
                                highlighted = pack.highlighted,
                                badgeText = if (pack.highlighted) "Best Value" else null,
                                ctaEnabled = purchasingProductId == null,
                                ctaLoading = purchasingProductId == pack.productId,
                            )
                        }
                        if (row.size == 1) Box(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PurchaseCreditsScreenPreview() {
    AuraPixTheme {
        PurchaseCreditsScreen(
            credits = 100,
            packs = listOf(
                BillingPlan("gem_value_pack", "gem_value_pack", "₹100", gems = 100, isSubscription = false, highlighted = true),
                BillingPlan("gem_one_time_purchase", "gem_one_time_purchase", "₹15", gems = 10, isSubscription = false, highlighted = false),
            ),
        )
    }
}

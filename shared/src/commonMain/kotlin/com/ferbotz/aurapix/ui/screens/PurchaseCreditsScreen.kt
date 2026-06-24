package com.ferbotz.aurapix.ui.screens

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.ui.components.AuraIconButton
import com.ferbotz.aurapix.ui.components.AuraTopBar
import com.ferbotz.aurapix.ui.components.CreditsBadge
import com.ferbotz.aurapix.ui.components.PricingCard
import com.ferbotz.aurapix.ui.theme.AuraPixTheme

/** Credit packs laid out in a 2-column grid. Reuses [PricingCard] for each pack. */
@Composable
fun PurchaseCreditsScreen(
    modifier: Modifier = Modifier,
    credits: Int = 50,
    onBack: () -> Unit = {},
    onSelectPack: (CreditPack) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AuraTopBar(
                title = "Purchase Credits",
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
                "Credits never expire. Buy a pack and start creating.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            sampleCreditPacks.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    row.forEach { pack ->
                        PricingCard(
                            title = "${pack.credits} Credits",
                            price = pack.price,
                            features = pack.perks,
                            ctaText = "Select",
                            onClick = { onSelectPack(pack) },
                            modifier = Modifier.weight(1f),
                            highlighted = pack.best,
                            badgeText = if (pack.best) "Best Value" else null,
                        )
                    }
                    if (row.size == 1) Box(Modifier.weight(1f))
                }
            }
        }
    }
}

@Preview
@Composable
private fun PurchaseCreditsScreenPreview() {
    AuraPixTheme { PurchaseCreditsScreen() }
}

package com.ferbotz.aurapix.billing.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.config.MonetizationConfig
import com.ferbotz.aurapix.core.config.OfferType
import com.ferbotz.aurapix.core.config.PurchaseOffer
import com.ferbotz.aurapix.core.ui.components.GlassCard
import com.ferbotz.aurapix.core.ui.components.PrimaryButton
import com.ferbotz.aurapix.core.ui.components.SecondaryButton
import com.ferbotz.aurapix.core.ui.components.StatusBadge

/**
 * Out-of-gems paywall. Shows the config's `proUserOffers` for subscribers (top-ups only) or
 * `freeUserOffers` for everyone else (a subscription + a one-time pack). Selecting an offer calls
 * [onSelectOffer] — the caller drives the actual purchase (RevenueCat).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallBottomSheet(
    isPremium: Boolean,
    config: MonetizationConfig,
    onDismiss: () -> Unit,
    onSelectOffer: (PurchaseOffer) -> Unit,
    modifier: Modifier = Modifier,
) {
    val offers = if (isPremium) config.proUserOffers else config.freeUserOffers
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Rounded.Diamond, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Text("You're out of gems", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Each generation costs ${config.generationCostGems} gems. Top up to keep creating.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            offers.forEach { offer -> OfferCard(offer, onClick = { onSelectOffer(offer) }) }
        }
    }
}

@Composable
private fun OfferCard(offer: PurchaseOffer, onClick: () -> Unit) {
    GlassCard(glow = offer.highlighted, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(offer.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    if (offer.highlighted) StatusBadge("Best value")
                }
                Text(offer.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Rounded.Diamond, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Text("${offer.gems}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        val label = (if (offer.type == OfferType.SUBSCRIPTION) "Subscribe" else "Buy") + " · ${offer.priceLabel}"
        if (offer.highlighted) {
            PrimaryButton(label, onClick, Modifier.fillMaxWidth().padding(top = 12.dp))
        } else {
            SecondaryButton(label, onClick, Modifier.fillMaxWidth().padding(top = 12.dp))
        }
    }
}

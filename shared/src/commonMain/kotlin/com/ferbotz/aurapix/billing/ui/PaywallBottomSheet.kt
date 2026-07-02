package com.ferbotz.aurapix.billing.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material3.CircularProgressIndicator
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
import com.ferbotz.aurapix.billing.data.RcPackage
import com.ferbotz.aurapix.core.ui.components.GlassCard
import com.ferbotz.aurapix.core.ui.components.PrimaryButton
import com.ferbotz.aurapix.core.ui.components.SecondaryButton

/**
 * Out-of-gems paywall. Renders the RevenueCat offering's [packages] (real localized prices),
 * enriched with the gem amount per product from config. Selecting a package drives the purchase +
 * backend verify via [onSelect].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallBottomSheet(
    packages: List<RcPackage>,
    loading: Boolean,
    purchasingProductId: String?,
    errorMessage: String?,
    generationCostGems: Int,
    gemsForProduct: (String) -> Int?,
    onSelect: (RcPackage) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                    "Each generation costs $generationCostGems gems. Top up to keep creating.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            when {
                loading -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                errorMessage != null -> Column(
                    Modifier.fillMaxWidth().padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(errorMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    SecondaryButton("Retry", onRetry, Modifier.fillMaxWidth())
                }

                packages.isEmpty() -> Text(
                    "No plans are available right now.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                )

                else -> packages.forEach { pkg ->
                    PackageCard(
                        pkg = pkg,
                        gems = gemsForProduct(pkg.productId),
                        purchasing = purchasingProductId == pkg.productId,
                        onClick = { onSelect(pkg) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PackageCard(pkg: RcPackage, gems: Int?, purchasing: Boolean, onClick: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(pkg.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                if (gems != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Rounded.Diamond, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Text("$gems gems", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Text(
                        if (pkg.isSubscription) "Subscription" else "One-time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(pkg.priceLabel, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        PrimaryButton(
            text = if (pkg.isSubscription) "Subscribe" else "Buy",
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            enabled = !purchasing,
            loading = purchasing,
        )
    }
}

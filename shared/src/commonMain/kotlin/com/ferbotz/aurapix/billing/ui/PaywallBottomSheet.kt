package com.ferbotz.aurapix.billing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.billing.data.RcPackage
import com.ferbotz.aurapix.core.ui.components.PrimaryButton
import com.ferbotz.aurapix.core.ui.components.SecondaryButton
import com.ferbotz.aurapix.core.ui.components.StatusBadge
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme
import com.ferbotz.aurapix.core.ui.theme.AuraShapes
import com.ferbotz.aurapix.core.ui.theme.AuraTheme
import com.ferbotz.aurapix.core.ui.theme.redGlow

/**
 * Out-of-gems paywall. Renders the RevenueCat offering's [packages] (real localized prices),
 * enriched with the gem amount per product from config. The recommended pack ([highlightForProduct])
 * is pre-selected and emphasized; the user taps a row to change the selection and confirms with the
 * single bottom CTA, which drives the purchase + backend verify via [onSelect].
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
    highlightForProduct: (String) -> Boolean,
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
        PaywallSheetContent(
            packages = packages,
            loading = loading,
            purchasingProductId = purchasingProductId,
            errorMessage = errorMessage,
            generationCostGems = generationCostGems,
            gemsForProduct = gemsForProduct,
            highlightForProduct = highlightForProduct,
            onSelect = onSelect,
            onRetry = onRetry,
        )
    }
}

@Composable
private fun PaywallSheetContent(
    packages: List<RcPackage>,
    loading: Boolean,
    purchasingProductId: String?,
    errorMessage: String?,
    generationCostGems: Int,
    gemsForProduct: (String) -> Int?,
    highlightForProduct: (String) -> Boolean,
    onSelect: (RcPackage) -> Unit,
    onRetry: () -> Unit,
) {
    // Pre-select the recommended pack (else the first); the user can tap another row to change it.
    var selectedId by remember { mutableStateOf<String?>(null) }
    val selected = packages.firstOrNull { it.productId == selectedId }
        ?: packages.firstOrNull { highlightForProduct(it.productId) }
        ?: packages.firstOrNull()

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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier.size(64.dp)
                    .redGlow(CircleShape, elevation = 22.dp, color = MaterialTheme.colorScheme.primary)
                    .clip(CircleShape)
                    .background(AuraTheme.colors.glassSurface)
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Diamond, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
            }
            Text("You're out of gems", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
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

            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    packages.forEach { pkg ->
                        PackageRow(
                            pkg = pkg,
                            gems = gemsForProduct(pkg.productId),
                            recommended = highlightForProduct(pkg.productId),
                            selected = pkg.productId == selected?.productId,
                            enabled = purchasingProductId == null,
                            onClick = { selectedId = pkg.productId },
                        )
                    }
                }

                selected?.let { sel ->
                    val subscription = sel.isSubscription
                    val accent = if (subscription) AuraTheme.colors.premium else MaterialTheme.colorScheme.primaryContainer
                    val onAccent = if (subscription) AuraTheme.colors.onPremium else MaterialTheme.colorScheme.onPrimaryContainer
                    val gems = gemsForProduct(sel.productId)
                    val label = when {
                        subscription -> "Go Premium"
                        gems != null -> "Get $gems gems"
                        else -> "Continue"
                    }
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        PrimaryButton(
                            text = label,
                            onClick = { onSelect(sel) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = purchasingProductId == null,
                            loading = purchasingProductId == sel.productId,
                            containerColor = accent,
                            contentColor = onAccent,
                            glowColor = accent,
                        )
                        Text(
                            "Gems never expire",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/** A tappable, radio-selectable pack row. The selected row shows an accent ring + glow. */
@Composable
private fun PackageRow(
    pkg: RcPackage,
    gems: Int?,
    recommended: Boolean,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val accent = if (pkg.isSubscription) AuraTheme.colors.premium else MaterialTheme.colorScheme.primary
    val onAccent = if (pkg.isSubscription) AuraTheme.colors.onPremium else MaterialTheme.colorScheme.onPrimary
    val shape = AuraShapes.large
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (selected) Modifier.redGlow(shape, elevation = 16.dp, color = accent) else Modifier)
            .clip(shape)
            .background(if (selected) MaterialTheme.colorScheme.surfaceContainer else AuraTheme.colors.glassSurface)
            .border(if (selected) 2.dp else 1.dp, if (selected) accent else AuraTheme.colors.glassBorder, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (pkg.isSubscription || recommended) {
                StatusBadge(
                    if (pkg.isSubscription) "Premium" else "Best value",
                    containerColor = accent,
                    contentColor = onAccent,
                    icon = if (pkg.isSubscription) Icons.Rounded.Diamond else null,
                )
            }
            Text(
                if (!pkg.isSubscription && gems != null) "$gems gems" else pkg.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val subtitle = when {
                pkg.isSubscription && gems != null -> "$gems gems every month"
                recommended -> "Most popular"
                else -> null
            }
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(pkg.priceLabel, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        RadioDot(selected = selected, accent = accent, onAccent = onAccent)
    }
}

@Composable
private fun RadioDot(selected: Boolean, accent: Color, onAccent: Color) {
    Box(
        Modifier.size(22.dp).clip(CircleShape)
            .then(
                if (selected) Modifier.background(accent)
                else Modifier.border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) Icon(Icons.Rounded.Check, null, tint = onAccent, modifier = Modifier.size(14.dp))
    }
}

@Preview
@Composable
private fun PaywallSheetContentPreview() {
    AuraPixTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.surface).padding(top = 12.dp)) {
            val gems = mapOf("gem_value_pack" to 120, "gem_mid" to 60, "gem_small" to 20)
            PaywallSheetContent(
                packages = listOf(
                    RcPackage("pkg_value", "gem_value_pack", "120 Gems", "₹299", false),
                    RcPackage("pkg_mid", "gem_mid", "60 Gems", "₹149", false),
                    RcPackage("pkg_small", "gem_small", "20 Gems", "₹49", false),
                ),
                loading = false,
                purchasingProductId = null,
                errorMessage = null,
                generationCostGems = 10,
                gemsForProduct = { gems[it] },
                highlightForProduct = { it == "gem_value_pack" },
                onSelect = {},
                onRetry = {},
            )
        }
    }
}

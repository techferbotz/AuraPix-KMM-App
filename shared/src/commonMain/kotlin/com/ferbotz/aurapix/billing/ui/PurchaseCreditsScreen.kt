package com.ferbotz.aurapix.billing.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.components.AuraIconButton
import com.ferbotz.aurapix.core.ui.components.AuraTopBar
import com.ferbotz.aurapix.core.ui.components.CreditsBadge
import com.ferbotz.aurapix.core.ui.components.PrimaryButton
import com.ferbotz.aurapix.core.ui.components.SecondaryButton
import com.ferbotz.aurapix.core.ui.components.StatusBadge
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme
import com.ferbotz.aurapix.core.ui.theme.AuraShapes
import com.ferbotz.aurapix.core.ui.theme.AuraTheme
import com.ferbotz.aurapix.core.ui.theme.redGlow

/** Gem packs from the live RevenueCat offering: the best-value pack is featured, the rest in a grid. */
@Composable
fun PurchaseCreditsScreen(
    modifier: Modifier = Modifier,
    credits: Int = 0,
    packs: List<BillingPlan> = emptyList(),
    generationCostGems: Int = 0,
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
                title = "Buy gems",
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
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Gems never expire. Pick a pack and start creating.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            when {
                loading -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                packs.isEmpty() -> BillingMessage(error ?: "No gem packs available right now.", onRetry)

                else -> {
                    val featured = packs.firstOrNull { it.highlighted }
                    val rest = packs.filter { it.productId != featured?.productId }

                    featured?.let {
                        GemPackCard(
                            pack = it,
                            generationCostGems = generationCostGems,
                            featured = true,
                            enabled = purchasingProductId == null,
                            purchasing = purchasingProductId == it.productId,
                            onSelect = { onSelectPack(it) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    rest.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            row.forEach { pack ->
                                GemPackCard(
                                    pack = pack,
                                    generationCostGems = generationCostGems,
                                    featured = false,
                                    enabled = purchasingProductId == null,
                                    purchasing = purchasingProductId == pack.productId,
                                    onSelect = { onSelectPack(pack) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (row.size == 1) Box(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * A gem pack. [featured] renders a full-width row with a "Best value" badge, glow and a value cue;
 * otherwise a compact grid tile. Gems are purple (premium gold is reserved for the subscription).
 */
@Composable
private fun GemPackCard(
    pack: BillingPlan,
    generationCostGems: Int,
    featured: Boolean,
    enabled: Boolean,
    purchasing: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = AuraShapes.large
    val accent = MaterialTheme.colorScheme.primary
    val surface = modifier
        .then(if (featured) Modifier.redGlow(shape, elevation = 20.dp, color = accent) else Modifier)
        .clip(shape)
        .background(if (featured) MaterialTheme.colorScheme.surfaceContainer else AuraTheme.colors.glassSurface)
        .border(if (featured) 2.dp else 1.dp, if (featured) accent else AuraTheme.colors.glassBorder, shape)
        .padding(16.dp)

    if (featured) {
        Row(surface, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusBadge("Best value")
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Rounded.Diamond, null, tint = accent, modifier = Modifier.size(22.dp))
                    Text("${pack.gems}", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text("gems", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                imagesCue(pack.gems, generationCostGems)?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(pack.priceLabel, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                PrimaryButton("Select", onSelect, enabled = enabled, loading = purchasing)
            }
        }
    } else {
        Column(surface, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Icon(Icons.Rounded.Diamond, null, tint = accent, modifier = Modifier.size(20.dp))
                Text("${pack.gems}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                Text("gems", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(pack.priceLabel, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            SecondaryButton("Select", onSelect, Modifier.fillMaxWidth(), enabled = enabled)
        }
    }
}

/** "≈ N images" value cue derived from the numeric gem count and per-generation cost. */
private fun imagesCue(gems: Int, generationCostGems: Int): String? {
    if (generationCostGems <= 0) return null
    val images = gems / generationCostGems
    return if (images >= 1) "≈ $images images" else null
}

@Preview
@Composable
private fun PurchaseCreditsScreenPreview() {
    AuraPixTheme {
        PurchaseCreditsScreen(
            credits = 100,
            generationCostGems = 10,
            packs = listOf(
                BillingPlan("gem_value_pack", "gem_value_pack", "₹499", gems = 300, isSubscription = false, highlighted = true),
                BillingPlan("gem_120", "gem_120", "₹299", gems = 120, isSubscription = false, highlighted = false),
                BillingPlan("gem_60", "gem_60", "₹149", gems = 60, isSubscription = false, highlighted = false),
                BillingPlan("gem_20", "gem_20", "₹49", gems = 20, isSubscription = false, highlighted = false),
                BillingPlan("gem_10", "gem_10", "₹29", gems = 10, isSubscription = false, highlighted = false),
            ),
        )
    }
}

package com.ferbotz.aurapix.billing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.config.Store
import com.ferbotz.aurapix.core.ui.components.AuraIconButton
import com.ferbotz.aurapix.core.ui.components.AuraTopBar
import com.ferbotz.aurapix.core.ui.components.CreditsBadge
import com.ferbotz.aurapix.core.ui.components.PrimaryButton
import com.ferbotz.aurapix.core.ui.components.StatusBadge
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme
import com.ferbotz.aurapix.core.ui.theme.AuraShapes
import com.ferbotz.aurapix.core.ui.theme.AuraTheme
import com.ferbotz.aurapix.core.ui.theme.auraGlow

/**
 * Gem packs from the `/config` catalogue, priced by the live store offering.
 *
 * One selectable row per pack with a single confirm button, rather than a Select on every card:
 * the CTA can then name what is about to be bought, and the pre-selected row (the catalogue's
 * `defaultProductId`) does the recommending that a "Best value" badge alone can't.
 */
@Composable
fun PurchaseCreditsScreen(
    modifier: Modifier = Modifier,
    credits: Int = 0,
    packs: List<BillingPlan> = emptyList(),
    generationCostGems: Int = 0,
    defaultProductId: String? = null,
    loading: Boolean = false,
    error: String? = null,
    purchasingProductId: String? = null,
    onBack: () -> Unit = {},
    onConfirm: (BillingPlan) -> Unit = {},
    onRetry: () -> Unit = {},
) {
    var pickedId by remember { mutableStateOf<String?>(null) }
    val selected = packs.firstOrNull { it.productId == pickedId }
        ?: packs.firstOrNull { it.productId == defaultProductId }
        ?: packs.firstOrNull { it.highlighted }
        ?: packs.firstOrNull()

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
        bottomBar = {
            if (selected != null) {
                Column(
                    Modifier.fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .navigationBarsPadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PrimaryButton(
                        text = selected.product.ctaLabel ?: "Continue · ${selected.priceLabel}",
                        onClick = { onConfirm(selected) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = purchasingProductId == null,
                        loading = purchasingProductId == selected.productId,
                    )
                    selected.product.footnote?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.labelSmall,
                            color = AuraTheme.colors.muted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                    packs.forEach { pack ->
                        GemPackRow(
                            pack = pack,
                            generationCostGems = generationCostGems,
                            selected = pack.productId == selected?.productId,
                            enabled = purchasingProductId == null,
                            onClick = { pickedId = pack.productId },
                        )
                    }

                    if (generationCostGems > 0) {
                        Text(
                            "Each generation costs $generationCostGems gems.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AuraTheme.colors.muted,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

/** One selectable pack: radio, gem count and badge, the catalogue's title plus a value cue, price. */
@Composable
private fun GemPackRow(
    pack: BillingPlan,
    generationCostGems: Int,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val shape = AuraShapes.large
    val accent = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (selected) Modifier.auraGlow(shape, elevation = 16.dp, color = accent) else Modifier)
            .clip(shape)
            .background(if (selected) MaterialTheme.colorScheme.surfaceContainer else AuraTheme.colors.glassSurface)
            .border(if (selected) 2.dp else 1.dp, if (selected) accent else AuraTheme.colors.glassBorder, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(
                selectedColor = accent,
                unselectedColor = AuraTheme.colors.muted,
            ),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            // The badge gets its own line: sharing one with the gem count leaves it too little
            // width beside the price, and it wraps mid-phrase.
            pack.product.badge?.let { StatusBadge(it) }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Rounded.Diamond, null, tint = accent, modifier = Modifier.size(20.dp))
                Text(
                    "${pack.gems}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text("gems", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            secondaryLine(pack, generationCostGems)?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(
            pack.priceLabel,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * "Value Pack · ≈ 12 images". The images cue is the catalogue's own [Store.Product.imagesLabel]
 * when it sets one, otherwise derived from the gem count and the live per-generation cost so the
 * two can never disagree.
 */
private fun secondaryLine(pack: BillingPlan, generationCostGems: Int): String? {
    val cue = pack.product.imagesLabel ?: imagesCue(pack.gems, generationCostGems)
    return listOfNotNull(pack.product.title.takeIf { it.isNotBlank() }, cue)
        .takeIf { it.isNotEmpty() }
        ?.joinToString(" · ")
}

/** "≈ N images" derived from the gem count and per-generation cost. */
private fun imagesCue(gems: Int, generationCostGems: Int): String? {
    if (generationCostGems <= 0 || gems <= 0) return null
    val images = gems / generationCostGems
    return when {
        images >= 2 -> "≈ $images images"
        images == 1 -> "≈ 1 image"
        else -> null
    }
}

@Preview
@Composable
private fun PurchaseCreditsScreenPreview() {
    val catalogue = Store.DEFAULT_PRODUCTS.filter { it.kind == Store.Kind.ONE_TIME }
    AuraPixTheme {
        PurchaseCreditsScreen(
            credits = 100,
            generationCostGems = 10,
            defaultProductId = "gem_value_pack",
            packs = catalogue.map {
                BillingPlan(it, it.productId, it.fallbackPriceLabel.orEmpty())
            },
        )
    }
}

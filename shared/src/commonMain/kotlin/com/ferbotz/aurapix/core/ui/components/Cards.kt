package com.ferbotz.aurapix.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.theme.AuraShapes
import com.ferbotz.aurapix.core.ui.theme.AuraTheme
import com.ferbotz.aurapix.core.ui.theme.redGlow

/**
 * The one glass surface used for panels/cards over busy backgrounds. Translucent fill +
 * hairline border (a portable stand-in for CSS backdrop-blur; see Theme docs).
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = AuraShapes.large,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    glow: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .then(if (glow) Modifier.redGlow(shape, elevation = 20.dp) else Modifier)
            .clip(shape)
            .background(AuraTheme.colors.glassSurface)
            .border(1.dp, AuraTheme.colors.glassBorder, shape)
            .padding(contentPadding),
        content = content,
    )
}

/** Section title with an optional trailing action (e.g. "See all"). */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        action?.invoke()
    }
}

/** Small uppercase group label used by Settings-style sections. */
@Composable
fun OverlineLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** A single "✓ feature" line used in pricing/benefit lists. [tint] recolors the check (e.g. gold on Premium). */
@Composable
fun FeatureRow(text: String, modifier: Modifier = Modifier, tint: Color = AuraTheme.colors.success) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            Icons.Rounded.Check,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * One pricing card used by Premium Plans. [highlighted] = the featured tier
 * (accent border + glow + filled CTA); otherwise a glass card with an outline CTA.
 * [accentColor]/[onAccentColor] recolor the border, glow, badge, feature checks and CTA
 * (purple by default; gold for the Premium subscription).
 */
@Composable
fun PricingCard(
    title: String,
    price: String,
    features: List<String>,
    ctaText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    period: String? = null,
    highlighted: Boolean = false,
    badgeText: String? = null,
    ctaEnabled: Boolean = true,
    ctaLoading: Boolean = false,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onAccentColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    val shape = AuraShapes.large
    val borderColor = if (highlighted) accentColor else AuraTheme.colors.glassBorder
    Column(
        modifier = modifier
            .then(if (highlighted) Modifier.redGlow(shape, elevation = 24.dp, color = accentColor) else Modifier)
            .clip(shape)
            .background(if (highlighted) MaterialTheme.colorScheme.surfaceContainer else AuraTheme.colors.glassSurface)
            .border(if (highlighted) 2.dp else 1.dp, borderColor, shape)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        badgeText?.let { StatusBadge(it, containerColor = accentColor, contentColor = onAccentColor) }
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(price, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            period?.let {
                Text(
                    " /$it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val checkTint = if (highlighted) accentColor else AuraTheme.colors.success
            features.forEach { FeatureRow(it, tint = checkTint) }
        }
        if (highlighted) {
            PrimaryButton(
                ctaText, onClick, Modifier.fillMaxWidth(),
                enabled = ctaEnabled, loading = ctaLoading,
                containerColor = accentColor, contentColor = onAccentColor, glowColor = accentColor,
            )
        } else {
            SecondaryButton(ctaText, onClick, Modifier.fillMaxWidth(), enabled = ctaEnabled)
        }
    }
}

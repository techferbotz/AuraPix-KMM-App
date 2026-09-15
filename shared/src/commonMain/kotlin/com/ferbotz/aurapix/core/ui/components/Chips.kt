package com.ferbotz.aurapix.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.theme.AuraTheme

/** Selectable filter pill (Portrait / Anime / All Creations …). One style, everywhere. */
@Composable
fun CategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
    val fg = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = fg,
        modifier = modifier
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
    )
}

/**
 * Read-only tag pill — the category chips on Template Detail and the recent-search chips.
 * Not selectable; `surfaceContainerHigh` fill with secondary-text labels.
 */
@Composable
fun ReadOnlyChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    leadingIcon: ImageVector? = null,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        leadingIcon?.let {
            Icon(
                it,
                contentDescription = null,
                tint = AuraTheme.colors.muted,
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Small label badge (TRENDING, BEST VALUE, PREMIUM, category tags).
 *
 * Defaults to the deep-violet badge fill `#4426B8` on `#E4D7FD` — a dedicated pair, distinct from
 * both the primary CTA violet and the surface ladder, so badges read as metadata rather than as
 * something tappable.
 */
@Composable
fun StatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = AuraTheme.colors.badge,
    contentColor: Color = AuraTheme.colors.onBadge,
    icon: ImageVector? = null,
) {
    Row(
        modifier = modifier.clip(CircleShape).background(containerColor).padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        icon?.let { Icon(it, contentDescription = null, tint = contentColor, modifier = Modifier.size(12.dp)) }
        Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = contentColor)
    }
}

/** Convenience PREMIUM badge. */
@Composable
fun PremiumBadge(modifier: Modifier = Modifier) =
    StatusBadge("Premium", modifier = modifier, icon = Icons.Rounded.Diamond)

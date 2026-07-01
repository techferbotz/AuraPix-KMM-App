package com.ferbotz.aurapix.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.theme.AuraShapes
import com.ferbotz.aurapix.core.ui.theme.redGlow

/** AuraPix logo lockup (glowing red icon tile + wordmark). Reused on Splash and Login. */
@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    iconSize: Dp = 64.dp,
    showWordmark: Boolean = true,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .redGlow(AuraShapes.medium, elevation = 24.dp)
                .clip(AuraShapes.medium)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.AutoAwesome,
                contentDescription = "AuraPix",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(iconSize * 0.5f),
            )
        }
        if (showWordmark) {
            Text("AuraPix", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

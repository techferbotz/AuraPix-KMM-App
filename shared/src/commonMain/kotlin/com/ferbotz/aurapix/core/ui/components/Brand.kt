package com.ferbotz.aurapix.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import aurapix.shared.generated.resources.Res
import aurapix.shared.generated.resources.app_logo
import org.jetbrains.compose.resources.painterResource

/** AuraPix logo lockup (the glowing logo mark + wordmark). Reused on Splash and Login. */
@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    iconSize: Dp = 96.dp,
    showWordmark: Boolean = true,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(Res.drawable.app_logo),
            contentDescription = "AuraPix",
            modifier = Modifier.size(iconSize),
        )
        if (showWordmark) {
            Text("AuraPix", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

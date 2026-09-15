package com.ferbotz.aurapix.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.theme.AuraTheme
import com.ferbotz.aurapix.core.ui.theme.auraCtaBrush
import com.ferbotz.aurapix.core.ui.theme.auraGlow

/**
 * The single primary call-to-action across the app: full-pill, 56dp tall,
 * [MaterialTheme.typography.labelLarge] text, filled with the **brand gradient** and lit by a
 * 16dp violet glow.
 *
 * The gradient (not a flat violet) is deliberate — the design reuses the logo's own `#5533E0 →
 * #7D52F4` ramp on every hero CTA so large violet fills never go flat and cheap.
 *
 * States, all from the design:
 *  - **default** — gradient + glow
 *  - **disabled** — flat `surfaceContainerHigh`, muted label, no glow
 *  - **loading** — spinner replaces the label (or sits beside [loadingText]); taps ignored
 *
 * Pass [containerColor] with `containerBrush = null` to re-accent it flat — that's how the gold
 * Premium CTA is built, without introducing a second button component.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingText: String? = null,
    leadingIcon: ImageVector? = null,
    /** Arbitrary leading content, for slots an [ImageVector] can't express (e.g. the Google mark).
     *  Takes precedence over [leadingIcon]. */
    leadingContent: @Composable (() -> Unit)? = null,
    trailingIcon: ImageVector? = null,
    containerBrush: Brush? = auraCtaBrush(),
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    glowColor: Color = AuraTheme.colors.glow,
) {
    val shape = CircleShape
    val lit = enabled && !loading
    // A gradient can't go through ButtonDefaults.colors, so the brush is painted on the button's
    // own modifier and the container is left transparent. Order matters: glow (unclipped) first,
    // then the fill, so the light bleeds outward from behind the pill.
    val fill = if (containerBrush != null && lit) {
        Modifier.auraGlow(shape, elevation = 16.dp, color = glowColor).background(containerBrush, shape)
    } else if (containerBrush != null) {
        Modifier // disabled: ButtonDefaults paints the flat disabled container
    } else if (lit) {
        Modifier.auraGlow(shape, elevation = 16.dp, color = glowColor)
    } else {
        Modifier
    }
    Button(
        onClick = onClick,
        enabled = lit,
        shape = shape,
        modifier = modifier.heightIn(min = 56.dp).then(fill),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (containerBrush != null) Color.Transparent else containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContentColor = AuraTheme.colors.muted,
        ),
        contentPadding = PaddingValues(horizontal = 24.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.5.dp,
                color = LocalContentColor.current,
            )
            loadingText?.let {
                Spacer(Modifier.width(10.dp))
                Text(it, style = MaterialTheme.typography.labelLarge)
            }
        } else {
            when {
                leadingContent != null -> {
                    leadingContent()
                    Spacer(Modifier.width(10.dp))
                }
                leadingIcon != null -> {
                    Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                }
            }
            Text(text, style = MaterialTheme.typography.labelLarge)
            trailingIcon?.let {
                Spacer(Modifier.width(8.dp))
                Icon(it, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/** Secondary action: 1dp violet outline on a transparent fill, same metrics as [PrimaryButton]. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val shape = CircleShape
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        modifier = modifier.heightIn(min = 56.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.secondary,
        ),
        contentPadding = PaddingValues(horizontal = 24.dp),
    ) {
        leadingIcon?.let {
            Icon(it, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

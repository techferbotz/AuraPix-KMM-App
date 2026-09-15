package com.ferbotz.aurapix.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aurapix.shared.generated.resources.Res
import aurapix.shared.generated.resources.app_logo_mark
import com.ferbotz.aurapix.core.ui.theme.AuraTheme
import org.jetbrains.compose.resources.painterResource

/**
 * The logo mark on a dark ground.
 *
 * Uses the **knockout** vector (`app_logo_mark`), not `app_logo`: the shipped launcher vector
 * opens with an opaque `#F0ECFD` full-bleed square, which renders as a hard-edged light tile on
 * our near-black surfaces. Brief §2.7 — the plate is removed so the violet shapes sit directly
 * on the background, and the mark gets a glow instead of an edge.
 *
 * The glow is two stacked radial gradients standing in for the design's two CSS drop-shadows
 * (24px @ 55% violet, 64px @ 40% deep violet); Compose can't blur an arbitrary alpha shape, so
 * the light is painted behind the mark rather than derived from it.
 */
@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    glow: Boolean = true,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        if (glow) {
            AmbientGlowSpot(
                modifier = Modifier.size(size * 2.6f),
                color = AuraTheme.colors.glow,
                alpha = 0.32f,
            )
            AmbientGlowSpot(
                modifier = Modifier.size(size * 1.5f),
                color = AuraTheme.colors.gradientStart,
                alpha = 0.40f,
            )
        }
        Image(
            painter = painterResource(Res.drawable.app_logo_mark),
            contentDescription = "AuraPix",
            modifier = Modifier.size(size),
        )
    }
}

/**
 * The "Aura**Pix**" wordmark. The second half is tinted `secondary` (the soft violet
 * `#C8ADFD`) so the name carries the brand colour even where the mark isn't shown.
 */
@Composable
fun AuraWordmark(
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayMedium,
) {
    Text(
        text = buildAnnotatedString {
            append("Aura")
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) { append("Pix") }
        },
        style = style,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

/**
 * Full brand lockup — glowing mark over the wordmark. Splash and Login.
 */
@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    iconSize: Dp = 96.dp,
    showWordmark: Boolean = true,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AuraTheme.spacing.md),
    ) {
        BrandLogo(size = iconSize)
        if (showWordmark) AuraWordmark()
    }
}

/**
 * Compact horizontal lockup for top bars: 30dp mark + the wordmark at headlineSmall weight.
 * Used as the leading slot on Home Feed, My Creations and Processing.
 */
@Composable
fun BrandBarLockup(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AuraTheme.spacing.xs),
    ) {
        Image(
            painter = painterResource(Res.drawable.app_logo_mark),
            contentDescription = "AuraPix",
            modifier = Modifier.size(30.dp),
        )
        AuraWordmark(
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.4).sp,
            ),
        )
    }
}

/** A single soft radial light source. Sized by the caller; paints nothing but light. */
@Composable
private fun AmbientGlowSpot(modifier: Modifier, color: Color, alpha: Float) {
    Box(
        modifier.background(
            Brush.radialGradient(
                colorStops = arrayOf(
                    0f to color.copy(alpha = alpha),
                    0.68f to Color.Transparent,
                ),
            )
        )
    )
}

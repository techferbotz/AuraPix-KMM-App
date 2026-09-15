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
import aurapix.shared.generated.resources.app_logo
import aurapix.shared.generated.resources.app_logo_mark
import com.ferbotz.aurapix.core.ui.theme.AuraTheme
import org.jetbrains.compose.resources.painterResource

/**
 * The logo mark, on either ground.
 *
 * **Dark** uses the knockout vector (`app_logo_mark`). The shipped launcher vector opens with an
 * opaque `#F0ECFD` full-bleed square, which renders as a hard-edged light tile on our near-black
 * surfaces — brief §2.7. The plate is removed so the violet shapes sit directly on the
 * background, and the mark gets a glow instead of an edge.
 *
 * **Light** uses the plated vector (`app_logo`) — the mirror problem. Knocked out, the mark's
 * palest stops (`#ECE5FD`, `#F0EBFD`) sit within a hair of the lavender ground and the top of the
 * mark dissolves. The plate is exactly the ground colour, so on light it reads as no plate at all
 * while still giving those stops something to sit on.
 *
 * The glow is two stacked radial gradients standing in for the design's two CSS drop-shadows
 * (24px @ 55% violet, 64px @ 40% deep violet); Compose can't blur an arbitrary alpha shape, so
 * the light is painted behind the mark rather than derived from it. It's dialled back on light,
 * where a bright violet bloom on a pale ground turns to haze instead of reading as emission.
 */
@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    glow: Boolean = true,
) {
    val dark = AuraTheme.isDark
    Box(modifier, contentAlignment = Alignment.Center) {
        if (glow) {
            AmbientGlowSpot(
                modifier = Modifier.size(size * 2.6f),
                color = AuraTheme.colors.glow,
                alpha = if (dark) 0.32f else 0.16f,
            )
            AmbientGlowSpot(
                modifier = Modifier.size(size * 1.5f),
                color = AuraTheme.colors.gradientStart,
                alpha = if (dark) 0.40f else 0.18f,
            )
        }
        Image(
            painter = painterResource(
                if (dark) Res.drawable.app_logo_mark else Res.drawable.app_logo
            ),
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
        BrandLogo(size = 30.dp, glow = false)
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

package com.ferbotz.aurapix.core.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val LocalAuraColors = staticCompositionLocalOf { AuraExtendedDark }

/** Accessors for the AuraPix-specific design tokens not covered by [MaterialTheme]. */
object AuraTheme {
    val colors: AuraExtendedColors
        @Composable @ReadOnlyComposable get() = LocalAuraColors.current

    val spacing: AuraSpacing get() = AuraSpacing
}

/**
 * Root theme. Wrap the whole app (and every `@Preview`) in this. It installs the
 * single-file color scheme ([AuraDarkColorScheme]), Inter typography and shared shapes,
 * and exposes the extended tokens via [AuraTheme].
 */
@Composable
fun AuraPixTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAuraColors provides AuraExtendedDark) {
        MaterialTheme(
            colorScheme = AuraDarkColorScheme,
            typography = auraTypography(),
            shapes = AuraShapes,
            content = content,
        )
    }
}

/**
 * Brand "red glow" drop shadow used on primary CTAs and highlighted cards.
 * Colored shadows render on Android (API 28+); on other targets the elevation still
 * produces a soft shadow. Apply before clipping/background so the glow bleeds outward.
 */
fun Modifier.redGlow(
    shape: Shape = CircleShape,
    elevation: Dp = 18.dp,
    color: Color = AuraExtendedDark.glow,
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    clip = false,
    ambientColor = color,
    spotColor = color,
)

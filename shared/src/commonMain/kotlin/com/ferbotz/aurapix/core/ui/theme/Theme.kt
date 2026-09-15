package com.ferbotz.aurapix.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val LocalAuraColors = staticCompositionLocalOf { AuraExtendedDark }

private val LocalAuraIsDark = staticCompositionLocalOf { true }

/** Accessors for the AuraPix-specific design tokens not covered by [MaterialTheme]. */
object AuraTheme {
    val colors: AuraExtendedColors
        @Composable @ReadOnlyComposable get() = LocalAuraColors.current

    /** Whether the dark scheme is active. Use only where a token genuinely can't express the
     *  difference — picking between two drawables, say — never to branch colours by hand. */
    val isDark: Boolean
        @Composable @ReadOnlyComposable get() = LocalAuraIsDark.current

    val spacing: AuraSpacing get() = AuraSpacing
}

/**
 * Root theme. Wrap the whole app (and every `@Preview`) in this. It installs the
 * single-file color scheme, Inter typography and shared shapes, and exposes the extended
 * tokens via [AuraTheme].
 *
 * [darkTheme] follows the system setting by default. Pass it explicitly to pin a theme — that's
 * how a user-facing appearance preference would drive this, and how the `@Preview`s show both.
 */
@Composable
fun AuraPixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAuraColors provides if (darkTheme) AuraExtendedDark else AuraExtendedLight,
        LocalAuraIsDark provides darkTheme,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) AuraDarkColorScheme else AuraLightColorScheme,
            typography = auraTypography(),
            shapes = AuraShapes,
            content = content,
        )
    }
}

/**
 * The app's signature violet glow — a colored drop shadow behind primary CTAs, the selected
 * nav icon and highlighted cards. It should read as light emission, not as a drop shadow.
 *
 * Colored shadows render on Android (API 28+); on other targets the elevation still
 * produces a soft shadow. Apply before clipping/background so the glow bleeds outward.
 *
 * Elevations in use: 12dp selected nav icon · 16dp buttons · 20dp cards · 24dp highlighted
 * pricing card.
 *
 * `@Composable` so the default colour tracks the active theme rather than being pinned to dark.
 */
@Composable
fun Modifier.auraGlow(
    shape: Shape = CircleShape,
    elevation: Dp = 18.dp,
    color: Color = AuraTheme.colors.glow,
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    clip = false,
    ambientColor = color,
    spotColor = color,
)

/**
 * The brand CTA gradient (`#5533E0 → #6B42EF`), drawn on the 135° diagonal like the design.
 * Used on primary buttons, the selected nav pill and the "Try this" hero CTA — the logo's own
 * ramp rather than a flat violet fill.
 */
@Composable
@ReadOnlyComposable
fun auraCtaBrush(): Brush = Brush.linearGradient(
    colors = listOf(AuraTheme.colors.gradientStart, AuraTheme.colors.gradientEnd),
)

/**
 * The lighter sweep used for the Processing screen's progress arc — `#5533E0 → #A57EFC` on dark,
 * ending a stop darker on light so the arc stays visible against the lavender ground.
 */
@Composable
@ReadOnlyComposable
fun auraSweepBrush(): Brush = Brush.linearGradient(
    colors = listOf(AuraTheme.colors.gradientStart, AuraTheme.colors.gradientSweepEnd),
)

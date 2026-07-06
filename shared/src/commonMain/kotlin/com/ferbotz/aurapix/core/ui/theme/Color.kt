package com.ferbotz.aurapix.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * AuraPix — SINGLE SOURCE OF TRUTH FOR ALL THEME COLORS.
 *
 * Everything that defines a color lives in this one file:
 *  - [AuraDarkColorScheme]   : the Material 3 [ColorScheme] used by all M3 components.
 *  - [AuraExtendedColors]    : the extra, non-Material tokens (glass, glow, badges, scrim).
 *
 * To re-theme the whole app, edit the values here — nothing else needs to change.
 * Palette: AuraPix Design System v1 (purple brand on a zinc-dark surface ladder).
 */

// ---------------------------------------------------------------------------
// Raw palette (AuraPix design tokens — purple / zinc, Material 3 dark)
// ---------------------------------------------------------------------------
private val Purple          = Color(0xFF8B5CF6) // brand accent / primary button
private val PurpleSecondary = Color(0xFFA855F7) // secondary purple
private val PurpleLight     = Color(0xFFC084FC) // light purple accent
private val PurpleDeep      = Color(0xFF6D28D9) // deep purple
private val Violet          = Color(0xFF4C1D95) // dark violet
private val PurplePressed   = Color(0xFF7C3AED) // primary button pressed

private val Bg              = Color(0xFF09090B) // primary background / navigation bar
private val BgSecondary     = Color(0xFF18181B) // secondary background
private val Surface         = Color(0xFF1F1F23) // surface / inputs
private val Elevated        = Color(0xFF27272A) // elevated surface
private val CardBg          = Color(0xFF2A2A2F) // card background / divider
private val Zinc700         = Color(0xFF3F3F46) // border / bright surface

private val White           = Color(0xFFFFFFFF) // primary text / icon
private val TextSecondary   = Color(0xFFA1A1AA) // secondary text / muted icons

private val ErrorRed        = Color(0xFFEF4444) // error / danger button
private val ErrContainer    = Color(0xFF7F1D1D) // error container
private val OnErrContainer  = Color(0xFFFECACA) // text on error container

private val Success         = Color(0xFF22C55E)
private val Warning         = Color(0xFFF59E0B)

// ---------------------------------------------------------------------------
// Material 3 color scheme  (drives every M3 component in the app)
// ---------------------------------------------------------------------------
val AuraDarkColorScheme: ColorScheme = darkColorScheme(
    primary = Purple,
    onPrimary = White,
    primaryContainer = Purple,
    onPrimaryContainer = White,
    inversePrimary = PurplePressed,
    secondary = PurpleSecondary,
    onSecondary = White,
    secondaryContainer = PurpleDeep,
    onSecondaryContainer = White,
    tertiary = PurpleLight,
    onTertiary = Violet,
    tertiaryContainer = Violet,
    onTertiaryContainer = PurpleLight,
    background = Bg,
    onBackground = White,
    surface = Bg,
    onSurface = White,
    surfaceVariant = Elevated,
    onSurfaceVariant = TextSecondary,
    surfaceDim = Bg,
    surfaceBright = Zinc700,
    surfaceContainerLowest = Bg,
    surfaceContainerLow = BgSecondary,
    surfaceContainer = Surface,
    surfaceContainerHigh = Elevated,
    surfaceContainerHighest = CardBg,
    error = ErrorRed,
    onError = White,
    errorContainer = ErrContainer,
    onErrorContainer = OnErrContainer,
    outline = Zinc700,
    outlineVariant = CardBg,
    scrim = Color(0xFF000000),
    inverseSurface = White,
    inverseOnSurface = Bg,
)

// ---------------------------------------------------------------------------
// Extended (non-Material) tokens — glass, glow, badges, gradients
// ---------------------------------------------------------------------------
@Immutable
data class AuraExtendedColors(
    /** Translucent fill that fakes glassmorphism over busy backgrounds. */
    val glassSurface: Color,
    /** Hairline border drawn on glass surfaces. */
    val glassBorder: Color,
    /** Brand purple used for glow/shadow accents. */
    val glow: Color,
    /** Small category/status badge background (deep purple). */
    val badge: Color,
    /** Dark scrim laid over imagery so text stays legible. */
    val scrim: Color,
    /** Foreground color for content drawn on top of imagery. */
    val onImage: Color,
    /** Positive / "good example" accent. */
    val success: Color,
    /** Caution accent. */
    val warning: Color,
)

val AuraExtendedDark = AuraExtendedColors(
    glassSurface = Color(0x9918181B), // rgba(24, 24, 27, 0.60)
    glassBorder = Color(0x14FFFFFF),  // white @ 8%
    glow = Purple,
    badge = PurpleDeep,
    scrim = Color(0xCC000000),
    onImage = Color(0xFFFFFFFF),
    success = Success,
    warning = Warning,
)

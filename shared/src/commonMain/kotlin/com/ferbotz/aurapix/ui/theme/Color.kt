package com.ferbotz.aurapix.ui.theme

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
 */

// ---------------------------------------------------------------------------
// Raw palette (AuraPix design tokens — Material 3 dark)
// ---------------------------------------------------------------------------
private val Red           = Color(0xFFD72638) // brand accent / primaryContainer
private val RedBright     = Color(0xFFBD0B29)
private val RedSoft       = Color(0xFFFFB3B1) // light red used for text/icons on dark
private val OnRed         = Color(0xFF680010)
private val OnRedLight    = Color(0xFFFFF2F1)

private val Ink           = Color(0xFF131313) // background / surface
private val InkLowest     = Color(0xFF0E0E0E)
private val InkLow        = Color(0xFF1C1B1B)
private val InkContainer  = Color(0xFF201F1F)
private val InkHigh       = Color(0xFF2A2A2A)
private val InkHighest    = Color(0xFF353534)
private val InkBright     = Color(0xFF3A3939)

private val Cloud         = Color(0xFFE5E2E1) // onSurface / onBackground
private val Muted         = Color(0xFFE4BDBB) // onSurfaceVariant
private val SurfaceVar    = Color(0xFF534342)
private val OutlineColor  = Color(0xFFAB8887)
private val OutlineVar    = Color(0xFF5C403F)

private val ErrLight      = Color(0xFFFFB4AB)
private val ErrContainer  = Color(0xFF93000A)
private val OnErr         = Color(0xFF690005)
private val OnErrContainer = Color(0xFFFFDAD6)

private val SecondaryCont = Color(0xFFA80025)
private val TertiaryCont  = Color(0xFFBD494E)

// ---------------------------------------------------------------------------
// Material 3 color scheme  (drives every M3 component in the app)
// ---------------------------------------------------------------------------
val AuraDarkColorScheme: ColorScheme = darkColorScheme(
    primary = RedSoft,
    onPrimary = OnRed,
    primaryContainer = Red,
    onPrimaryContainer = OnRedLight,
    inversePrimary = RedBright,
    secondary = RedSoft,
    onSecondary = OnRed,
    secondaryContainer = SecondaryCont,
    onSecondaryContainer = OnRedLight,
    tertiary = RedSoft,
    onTertiary = OnRed,
    tertiaryContainer = TertiaryCont,
    onTertiaryContainer = OnRedLight,
    background = Ink,
    onBackground = Cloud,
    surface = Ink,
    onSurface = Cloud,
    surfaceVariant = SurfaceVar,
    onSurfaceVariant = Muted,
    surfaceDim = Ink,
    surfaceBright = InkBright,
    surfaceContainerLowest = InkLowest,
    surfaceContainerLow = InkLow,
    surfaceContainer = InkContainer,
    surfaceContainerHigh = InkHigh,
    surfaceContainerHighest = InkHighest,
    error = ErrLight,
    onError = OnErr,
    errorContainer = ErrContainer,
    onErrorContainer = OnErrContainer,
    outline = OutlineColor,
    outlineVariant = OutlineVar,
    scrim = Color(0xFF000000),
    inverseSurface = Cloud,
    inverseOnSurface = Color(0xFF313030),
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
    /** Brand red used for glow/shadow accents. */
    val glow: Color,
    /** Small category/status badge background (burgundy). */
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
    glassSurface = Color(0x99151515), // rgba(21, 21, 21, 0.60)
    glassBorder = Color(0x14FFFFFF),  // white @ 8%
    glow = Red,
    badge = Color(0xFF7A1621),
    scrim = Color(0xCC000000),
    onImage = Color(0xFFFFFFFF),
    success = Color(0xFF7BD88F),
    warning = Color(0xFFFFB951),
)

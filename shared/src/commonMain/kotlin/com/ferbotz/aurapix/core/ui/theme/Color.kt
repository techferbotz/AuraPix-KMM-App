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
 *
 * Palette: AuraPix Design System v2 — **violet, derived from the app logo**, on a
 * violet-cast near-black surface ladder. Dark theme only; there is no light mode.
 */

// ---------------------------------------------------------------------------
// Brand violet — taken from the logo vector, do not re-hue
// ---------------------------------------------------------------------------
private val Violet        = Color(0xFF9267FA) // core brand violet — the logo's sparkle shapes
private val VioletBright  = Color(0xFFA57EFC) // emphasis / progress-ring gradient end
private val VioletPressed = Color(0xFF7D52F4) // pressed state, CTA gradient end
private val VioletDeep    = Color(0xFF5533E0) // deepest logo stop, CTA gradient start
private val VioletSoft    = Color(0xFFC8ADFD) // violet-tinted text & icons on dark
private val OnViolet      = Color(0xFFF0ECFD) // the logo plate color — text on violet fills

private val BadgeViolet   = Color(0xFF4426B8) // "TRENDING" / category badge fill
private val OnBadge       = Color(0xFFE4D7FD) // text on [BadgeViolet]

// ---------------------------------------------------------------------------
// Violet-cast dark surface ladder
// ---------------------------------------------------------------------------
private val Bg            = Color(0xFF0B0912) // app background
private val SurfLowest    = Color(0xFF08060E) // splash / result — the darkest ground
private val SurfLow       = Color(0xFF120F1C) // sheets, inert placeholders
private val SurfContainer = Color(0xFF181423) // inputs, search field, skeleton base
private val SurfHigh      = Color(0xFF211B30) // chips, credits badge, disabled CTA
private val SurfHighest   = Color(0xFF29223C) // progress-ring track
private val SurfBright    = Color(0xFF332B49) // brightest elevated fill

private val OnSurface     = Color(0xFFEDEAF6) // primary text
private val OnSurfaceVar  = Color(0xFFA79EC2) // secondary text (violet-tinted grey)
private val Muted         = Color(0xFF6F6590) // disabled labels, unselected nav, placeholders

private val Outline       = Color(0xFF4A4066) // dashed slot borders, inactive pager dots
private val OutlineVar    = Color(0xFF2C2542) // hairline borders
private val Divider       = Color(0xFF17121F) // list-row separators

private val Shimmer       = Color(0xFF241D36) // skeleton highlight sweeping over [SurfContainer]

// ---------------------------------------------------------------------------
// Semantic accents — each owns exactly one job (see the brief's §2.5 hierarchy)
// ---------------------------------------------------------------------------
private val ErrorRed      = Color(0xFFFF5A5F) // destructive & failure ONLY, never decorative
private val OnError       = Color(0xFF450A0A)
private val ErrContainer  = Color(0xFF7F1D1D)
private val OnErrContainer= Color(0xFFFECACA)

private val Success       = Color(0xFF22C55E) // "Looks great", completed stages
private val Warning       = Color(0xFFF59E0B)

private val PremiumGold   = Color(0xFFF5C563) // Premium SUBSCRIPTION only — never gems
private val OnPremiumGold = Color(0xFF3A2606)

// ---------------------------------------------------------------------------
// Material 3 color scheme  (drives every M3 component in the app)
// ---------------------------------------------------------------------------
val AuraDarkColorScheme: ColorScheme = darkColorScheme(
    primary = Violet,
    onPrimary = OnViolet,
    primaryContainer = VioletDeep,
    onPrimaryContainer = OnViolet,
    inversePrimary = VioletPressed,
    secondary = VioletSoft,
    onSecondary = SurfLow,
    secondaryContainer = SurfHigh,
    onSecondaryContainer = VioletSoft,
    tertiary = VioletBright,
    onTertiary = SurfLow,
    tertiaryContainer = BadgeViolet,
    onTertiaryContainer = OnBadge,
    background = Bg,
    onBackground = OnSurface,
    surface = Bg,
    onSurface = OnSurface,
    surfaceVariant = SurfHigh,
    onSurfaceVariant = OnSurfaceVar,
    surfaceDim = SurfLowest,
    surfaceBright = SurfBright,
    surfaceContainerLowest = SurfLowest,
    surfaceContainerLow = SurfLow,
    surfaceContainer = SurfContainer,
    surfaceContainerHigh = SurfHigh,
    surfaceContainerHighest = SurfHighest,
    error = ErrorRed,
    onError = OnError,
    errorContainer = ErrContainer,
    onErrorContainer = OnErrContainer,
    outline = Outline,
    outlineVariant = OutlineVar,
    scrim = Color(0xFF000000),
    inverseSurface = OnSurface,
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
    /** Fill for the floating bottom nav — more opaque than [glassSurface] so it reads solid while
     *  content still shows through the side margins. */
    val navSurface: Color,
    /** Brand violet used for glow/shadow accents. */
    val glow: Color,
    /** Small category/status badge background (deep violet). */
    val badge: Color,
    /** Foreground for content drawn on [badge]. */
    val onBadge: Color,
    /** Dark scrim laid over imagery so text stays legible. */
    val scrim: Color,
    /** Foreground color for content drawn on top of imagery. */
    val onImage: Color,
    /** Dimmed foreground: disabled CTA labels, unselected nav icons, input placeholders. */
    val muted: Color,
    /** Hairline separator between list rows. */
    val divider: Color,
    /** Highlight that sweeps across [ColorScheme.surfaceContainer] in loading skeletons. */
    val shimmer: Color,
    /** Positive / "good example" accent. */
    val success: Color,
    /** Caution accent. */
    val warning: Color,
    /** Premium (subscription) accent — gold, to set membership apart from violet gem purchases. */
    val premium: Color,
    /** Foreground color for content drawn on top of the gold [premium] fill. */
    val onPremium: Color,
    /** Start stop of the brand CTA gradient (logo G5 family). */
    val gradientStart: Color,
    /** End stop of the brand CTA gradient. */
    val gradientEnd: Color,
    /** End stop of the lighter sweep used on the progress ring. */
    val gradientSweepEnd: Color,
)

val AuraExtendedDark = AuraExtendedColors(
    glassSurface = Color(0x99131020), // rgba(19, 16, 32, 0.60)
    glassBorder = Color(0x14FFFFFF),  // white @ 8%
    navSurface = Color(0xE6131020),   // rgba(19, 16, 32, 0.90) — floating nav, less see-through
    glow = Violet,
    badge = BadgeViolet,
    onBadge = OnBadge,
    scrim = Color(0xCC000000),
    onImage = OnSurface,
    muted = Muted,
    divider = Divider,
    shimmer = Shimmer,
    success = Success,
    warning = Warning,
    premium = PremiumGold,
    onPremium = OnPremiumGold,
    gradientStart = VioletDeep,       // #5533E0
    gradientEnd = VioletPressed,      // #7D52F4
    gradientSweepEnd = VioletBright,  // #A57EFC
)

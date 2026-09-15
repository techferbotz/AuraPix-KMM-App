package com.ferbotz.aurapix.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * AuraPix — SINGLE SOURCE OF TRUTH FOR ALL THEME COLORS.
 *
 * Everything that defines a color lives in this one file:
 *  - [AuraDarkColorScheme] / [AuraLightColorScheme] : the Material 3 schemes.
 *  - [AuraExtendedDark]    / [AuraExtendedLight]    : the extra, non-Material tokens
 *    (glass, glow, badges, scrim, gradients).
 *
 * To re-theme the whole app, edit the values here — nothing else needs to change.
 *
 * **Both themes are the same brand.** The violet is identical in each; only the ground flips.
 * Dark runs violet on a violet-cast near-black ladder; light runs violet on the **logo's own
 * light-lavender plate family** (`#F0ECFD` / `#ECE5FD` / `#E4D7FD`). Neither uses neutral grey —
 * every surface carries a violet cast, which is what keeps the two feeling like one product.
 */

// ---------------------------------------------------------------------------
// Brand violet — taken from the logo vector, shared by both themes, do not re-hue
// ---------------------------------------------------------------------------
private val Violet        = Color(0xFF9267FA) // core brand violet — the logo's sparkle shapes
private val VioletBright  = Color(0xFFA57EFC) // emphasis / progress-ring gradient end
private val VioletMid     = Color(0xFF7D52F4) // pressed state
private val VioletLink    = Color(0xFF6B42EF) // CTA gradient end; link/accent on light grounds
private val VioletDeep    = Color(0xFF5533E0) // deepest logo stop, CTA gradient start
private val VioletSoft    = Color(0xFFC8ADFD) // violet-tinted text & icons on dark
private val Plate         = Color(0xFFF0ECFD) // the logo's own plate — text on violet fills
private val PlateTint     = Color(0xFFECE5FD) // logo G1 stop 0%
private val PlateDeep     = Color(0xFFE4D7FD) // logo G1 stop 100%

private val BadgeViolet   = Color(0xFF4426B8) // "TRENDING" / category badge

// ---------------------------------------------------------------------------
// DARK — violet-cast near-black ladder
// ---------------------------------------------------------------------------
private val DBg            = Color(0xFF0B0912) // app background
private val DLowest        = Color(0xFF08060E) // splash / result — the darkest ground
private val DLow           = Color(0xFF120F1C) // sheets, inert placeholders
private val DContainer     = Color(0xFF181423) // inputs, search field, skeleton base
private val DHigh          = Color(0xFF211B30) // chips, credits badge, disabled CTA
private val DHighest       = Color(0xFF29223C) // progress-ring track
private val DBright        = Color(0xFF332B49)

private val DOnSurface     = Color(0xFFEDEAF6) // 16.7:1 on DBg
private val DOnSurfaceVar  = Color(0xFFA79EC2) //  7.8:1 on DBg
private val DMuted         = Color(0xFF6F6590) //  3.7:1 — disabled, unselected nav, placeholders
private val DOutline       = Color(0xFF4A4066)
private val DOutlineVar    = Color(0xFF2C2542)
private val DDivider       = Color(0xFF17121F)
private val DShimmer       = Color(0xFF241D36)

// ---------------------------------------------------------------------------
// LIGHT — the logo's lavender plate family, same violet on top
// ---------------------------------------------------------------------------
private val LBg            = Color(0xFFF0ECFD) // the logo plate, used as the app ground
private val LLowest        = Color(0xFFFFFFFF)
private val LLow           = Color(0xFFF7F4FE) // between white and the plate
private val LContainer     = Color(0xFFECE5FD) // inputs, search field, skeleton base
private val LHigh          = Color(0xFFE4D7FD) // chips, credits badge, disabled CTA
private val LHighest       = Color(0xFFD8C7FB)
private val LBright        = Color(0xFFFFFFFF)

private val LOnSurface     = Color(0xFF1B1038) // 15.4:1 on LBg — violet-black, not neutral
private val LOnSurfaceVar  = Color(0xFF574B80) //  6.7:1 on LBg
private val LMuted         = Color(0xFF8076A6) //  3.6:1 — disabled, unselected nav, placeholders
private val LOutline       = Color(0xFFB6A9DB)
private val LOutlineVar    = Color(0xFFDED5F7)
private val LDivider       = Color(0xFFE6DEF9)
private val LShimmer       = Color(0xFFFBF9FF) // on light the sweep brightens rather than darkens
private val LOnPrimaryCont = Color(0xFF2A167F) // 10.2:1 on LHigh

// ---------------------------------------------------------------------------
// Semantic accents — each owns exactly one job (see the brief's §2.5 hierarchy).
// Light needs deeper tones than dark to clear 4.5:1 against a near-white ground.
// ---------------------------------------------------------------------------
private val DError         = Color(0xFFFF5A5F) // destructive & failure ONLY, never decorative
private val DOnError       = Color(0xFF450A0A)
private val DErrContainer  = Color(0xFF7F1D1D)
private val DOnErrContainer= Color(0xFFFECACA)
private val DSuccess       = Color(0xFF22C55E) // "Looks great", completed stages
private val DWarning       = Color(0xFFF59E0B)

private val LError         = Color(0xFFC9252D) //  4.8:1 on LBg
private val LOnError       = Color(0xFFFFFFFF)
private val LErrContainer  = Color(0xFFFCDAD9)
private val LOnErrContainer= Color(0xFF7F1D1D)
private val LSuccess       = Color(0xFF116B33) //  5.7:1 on LBg
private val LWarning       = Color(0xFF8A4009) //  6.4:1 on LBg

private val PremiumGold    = Color(0xFFF5C563) // Premium SUBSCRIPTION only — never gems
private val OnPremiumGold  = Color(0xFF3A2606)
private val PremiumGoldInk = Color(0xFF6B4A05) // the gold reads as ink, not fill, on light grounds

// ---------------------------------------------------------------------------
// Material 3 color schemes
// ---------------------------------------------------------------------------
val AuraDarkColorScheme: ColorScheme = darkColorScheme(
    primary = Violet,
    onPrimary = Plate,
    primaryContainer = VioletDeep,
    onPrimaryContainer = Plate,
    inversePrimary = VioletMid,
    secondary = VioletSoft,
    onSecondary = DLow,
    secondaryContainer = DHigh,
    onSecondaryContainer = VioletSoft,
    tertiary = VioletBright,
    onTertiary = DLow,
    tertiaryContainer = BadgeViolet,
    onTertiaryContainer = PlateDeep,
    background = DBg,
    onBackground = DOnSurface,
    surface = DBg,
    onSurface = DOnSurface,
    surfaceVariant = DHigh,
    onSurfaceVariant = DOnSurfaceVar,
    surfaceDim = DLowest,
    surfaceBright = DBright,
    surfaceContainerLowest = DLowest,
    surfaceContainerLow = DLow,
    surfaceContainer = DContainer,
    surfaceContainerHigh = DHigh,
    surfaceContainerHighest = DHighest,
    error = DError,
    onError = DOnError,
    errorContainer = DErrContainer,
    onErrorContainer = DOnErrContainer,
    outline = DOutline,
    outlineVariant = DOutlineVar,
    scrim = Color(0xFF000000),
    inverseSurface = DOnSurface,
    inverseOnSurface = DBg,
)

/**
 * The light counterpart. `primary` deepens from `#9267FA` to `#5533E0` — the core violet is too
 * pale to carry `#F0ECFD` label text on a light ground (3.3:1), while the deep stop clears AA at
 * 6.2:1. Same hue family, same logo, just the end of the ramp that works against light.
 */
val AuraLightColorScheme: ColorScheme = lightColorScheme(
    primary = VioletDeep,
    onPrimary = Plate,
    primaryContainer = PlateDeep,
    onPrimaryContainer = LOnPrimaryCont,
    inversePrimary = VioletSoft,
    secondary = VioletLink,
    onSecondary = Plate,
    secondaryContainer = PlateTint,
    onSecondaryContainer = LOnPrimaryCont,
    tertiary = VioletMid,
    onTertiary = Plate,
    tertiaryContainer = LHighest,
    onTertiaryContainer = LOnPrimaryCont,
    background = LBg,
    onBackground = LOnSurface,
    surface = LBg,
    onSurface = LOnSurface,
    surfaceVariant = LHigh,
    onSurfaceVariant = LOnSurfaceVar,
    surfaceDim = LHigh,
    surfaceBright = LBright,
    surfaceContainerLowest = LLowest,
    surfaceContainerLow = LLow,
    surfaceContainer = LContainer,
    surfaceContainerHigh = LHigh,
    surfaceContainerHighest = LHighest,
    error = LError,
    onError = LOnError,
    errorContainer = LErrContainer,
    onErrorContainer = LOnErrContainer,
    outline = LOutline,
    outlineVariant = LOutlineVar,
    scrim = Color(0xFF000000),
    inverseSurface = LOnSurface,
    inverseOnSurface = LBg,
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
    /** Small category/status badge background. */
    val badge: Color,
    /** Foreground for content drawn on [badge]. */
    val onBadge: Color,
    /** Dark scrim laid over imagery so text stays legible. Identical in both themes — photos
     *  carry their own darkness and the text on them is always light. */
    val scrim: Color,
    /** Foreground color for content drawn on top of imagery. */
    val onImage: Color,
    /** Dimmed foreground: disabled CTA labels, unselected nav icons, input placeholders. */
    val muted: Color,
    /** Hairline separator between list rows. */
    val divider: Color,
    /** Highlight that sweeps across `surfaceContainer` in loading skeletons. */
    val shimmer: Color,
    /** Positive / "good example" accent. */
    val success: Color,
    /** Caution accent. */
    val warning: Color,
    /** Premium (subscription) accent — gold, to set membership apart from violet gem purchases. */
    val premium: Color,
    /** Foreground color for content drawn on top of the gold [premium] fill. */
    val onPremium: Color,
    /** Gold used as *text or icon* rather than as a fill — needs to be deeper on light grounds. */
    val premiumInk: Color,
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
    onBadge = PlateDeep,
    scrim = Color(0xCC000000),
    onImage = DOnSurface,
    muted = DMuted,
    divider = DDivider,
    shimmer = DShimmer,
    success = DSuccess,
    warning = DWarning,
    premium = PremiumGold,
    onPremium = OnPremiumGold,
    premiumInk = PremiumGold,
    gradientStart = VioletDeep,       // #5533E0
    gradientEnd = VioletLink,         // #6B42EF
    gradientSweepEnd = VioletBright,  // #A57EFC
)

val AuraExtendedLight = AuraExtendedColors(
    // Glass inverts: on a light ground the translucent layer brightens rather than darkens,
    // and the hairline goes to a low-alpha ink instead of low-alpha white.
    glassSurface = Color(0x99FFFFFF), // white @ 60%
    glassBorder = Color(0x141B1038),  // ink @ 8%
    navSurface = Color(0xF2FFFFFF),   // white @ 95% — the floating pill needs to stay legible
    glow = Violet,
    badge = PlateDeep,
    onBadge = BadgeViolet,
    scrim = Color(0xCC000000),
    onImage = Plate,
    muted = LMuted,
    divider = LDivider,
    shimmer = LShimmer,
    success = LSuccess,
    warning = LWarning,
    premium = PremiumGold,
    onPremium = OnPremiumGold,
    premiumInk = PremiumGoldInk,
    gradientStart = VioletDeep,       // #5533E0
    gradientEnd = VioletLink,         // #6B42EF
    gradientSweepEnd = VioletMid,     // #7D52F4 — the light ground needs the darker sweep end
)

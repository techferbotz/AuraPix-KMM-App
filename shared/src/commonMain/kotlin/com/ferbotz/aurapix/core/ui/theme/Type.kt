package com.ferbotz.aurapix.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import aurapix.shared.generated.resources.Res
import aurapix.shared.generated.resources.inter_bold
import aurapix.shared.generated.resources.inter_extrabold
import aurapix.shared.generated.resources.inter_medium
import aurapix.shared.generated.resources.inter_regular
import aurapix.shared.generated.resources.inter_semibold
import org.jetbrains.compose.resources.Font

/** The Inter typeface bundled in composeResources/font (weights 400/500/600/700/800). */
@Composable
fun interFontFamily(): FontFamily = FontFamily(
    Font(Res.font.inter_regular, FontWeight.Normal),
    Font(Res.font.inter_medium, FontWeight.Medium),
    Font(Res.font.inter_semibold, FontWeight.SemiBold),
    Font(Res.font.inter_bold, FontWeight.Bold),
    Font(Res.font.inter_extrabold, FontWeight.ExtraBold),
)

/**
 * Maps the AuraPix design type scale onto the Material 3 [Typography] slots, all on Inter.
 * Display = hero headlines, Headline = section titles, Title = card/list titles,
 * Body = paragraph text, Label = buttons/chips/captions.
 */
@Composable
fun auraTypography(): Typography {
    val inter = interFontFamily()
    return Typography(
        displayLarge = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.ExtraBold,
            fontSize = 40.sp, lineHeight = 48.sp, letterSpacing = (-0.8).sp,
        ),
        displayMedium = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.Bold,
            fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.6).sp,
        ),
        displaySmall = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.Bold,
            fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.4).sp,
        ),
        headlineLarge = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.Bold,
            fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.4).sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.Bold,
            fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.2).sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp, lineHeight = 26.sp,
        ),
        titleLarge = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp, lineHeight = 28.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp, lineHeight = 24.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp, lineHeight = 22.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.Normal,
            fontSize = 16.sp, lineHeight = 24.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.Normal,
            fontSize = 14.sp, lineHeight = 20.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.Normal,
            fontSize = 12.sp, lineHeight = 16.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.Medium,
            fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.Medium,
            fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.6.sp,
        ),
    )
}

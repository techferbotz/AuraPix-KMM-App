package com.ferbotz.aurapix.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.theme.AuraTheme
import kotlin.random.Random

/**
 * Soft radial brand glow placed behind hero content (Splash, Login, Success, Failed).
 *
 * The design draws this as `radial-gradient(circle, rgba(146,103,250,.28), transparent 68%)` on
 * an oversized square that bleeds off the left edge — so it reads as one off-screen light source,
 * not a vignette. [diameter] and the offsets reproduce that placement; the defaults match the
 * Splash/Login artboards (600dp, 105dp off the left edge).
 */
@Composable
fun BoxScope.AmbientGlow(
    color: Color = MaterialTheme.colorScheme.primary,
    // Halved on light: the same 28% violet that reads as emission against near-black turns into
    // flat haze against a lavender ground, where there's no darkness for it to bloom out of.
    alpha: Float = if (AuraTheme.isDark) 0.28f else 0.14f,
    diameter: Dp = 600.dp,
    offsetX: Dp = (-105).dp,
    offsetY: Dp = 120.dp,
) {
    Box(
        modifier = Modifier
            .size(diameter)
            .offset(x = offsetX, y = offsetY)
            .background(
                Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to color.copy(alpha = alpha),
                        0.68f to Color.Transparent,
                    ),
                )
            )
    )
}

/**
 * The error-tinted variant used by Generation Failed — the one screen in the app where the
 * ambient light turns red. Recovery actions on top of it stay violet.
 */
@Composable
fun BoxScope.AmbientErrorGlow(
    diameter: Dp = 600.dp,
    offsetX: Dp = (-105).dp,
    offsetY: Dp = 60.dp,
) = AmbientGlow(
    color = MaterialTheme.colorScheme.error,
    alpha = if (AuraTheme.isDark) 0.14f else 0.08f,
    diameter = diameter,
    offsetX = offsetX,
    offsetY = offsetY,
)

/**
 * Loading skeleton fill: a highlight band sweeping left-to-right across
 * the `surfaceContainer` fill, 1.4s on a loop. The design uses these everywhere instead of
 * spinners for content that has a known shape — feed tiles, search rows, the credits badge.
 *
 * Apply to a sized, empty [Box]. Stagger sibling tiles with [delayMillis] so a row doesn't
 * pulse in lockstep.
 */
@Composable
fun Modifier.shimmer(
    shape: Shape,
    delayMillis: Int = 0,
): Modifier {
    val base = MaterialTheme.colorScheme.surfaceContainer
    val highlight = AuraTheme.colors.shimmer
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, delayMillis = delayMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer-progress",
    )
    return this
        .background(base, shape)
        .drawWithContent {
            drawContent()
            // Sweep a band twice the width of the box across it, matching the design's
            // `background-size: 200%` translation.
            val span = size.width * 2f
            val startX = -span + progress * (span + size.width)
            drawRect(
                brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color.Transparent,
                        0.5f to highlight,
                        1f to Color.Transparent,
                    ),
                    start = Offset(startX, 0f),
                    end = Offset(startX + span, 0f),
                ),
            )
        }
}

/** Convenience: a standalone skeleton block of the given [shape]. */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    delayMillis: Int = 0,
) = Box(modifier.shimmer(shape, delayMillis))

private data class ConfettiParticle(
    val x: Float,
    val widthPx: Float,
    val heightPx: Float,
    val phase: Float,
    val speed: Float,
    val colorIndex: Int,
)

/** Lightweight animated confetti for the success screens. Purely decorative. */
@Composable
fun ConfettiOverlay(
    modifier: Modifier = Modifier,
    particleCount: Int = 40,
) {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        AuraTheme.colors.gradientStart,
    )
    val particles = remember(particleCount) {
        val rnd = Random(42)
        List(particleCount) {
            ConfettiParticle(
                x = rnd.nextFloat(),
                widthPx = 6f + rnd.nextFloat() * 8f,
                heightPx = 10f + rnd.nextFloat() * 12f,
                phase = rnd.nextFloat(),
                speed = 0.6f + rnd.nextFloat() * 0.9f,
                colorIndex = rnd.nextInt(4),
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "confetti")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "confetti-progress",
    )
    Canvas(modifier.fillMaxSize()) {
        particles.forEach { p ->
            val progress = (p.phase + t * p.speed) % 1f
            val y = progress * (size.height + p.heightPx) - p.heightPx
            val x = p.x * size.width
            drawRect(
                color = colors[p.colorIndex].copy(alpha = 0.85f),
                topLeft = Offset(x, y),
                size = Size(p.widthPx, p.heightPx),
            )
        }
    }
}

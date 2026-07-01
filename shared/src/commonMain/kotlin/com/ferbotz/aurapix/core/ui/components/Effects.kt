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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

/**
 * Soft radial brand glow placed behind hero content (Splash, Login, Success, Failed).
 * Call inside a [BoxScope]; it fills the parent.
 */
@Composable
fun BoxScope.AmbientGlow(
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    alpha: Float = 0.28f,
) {
    Box(
        modifier = Modifier
            .matchParentSize()
            .background(Brush.radialGradient(listOf(color.copy(alpha = alpha), Color.Transparent)))
    )
}

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
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.onSurface,
        MaterialTheme.colorScheme.tertiaryContainer,
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

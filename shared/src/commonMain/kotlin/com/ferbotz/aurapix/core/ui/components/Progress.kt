package com.ferbotz.aurapix.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.theme.AuraShapes
import com.ferbotz.aurapix.core.ui.theme.AuraTheme

/**
 * Big circular progress ring (Processing screen). [content] is centered inside, e.g. "62%".
 *
 * The arc is a **gradient sweep** (`#5533E0 → #A57EFC`) rather than a flat violet — it's the one
 * element on that screen carrying motion, so the design gives it the brand ramp and a soft violet
 * bloom. Track is `surfaceContainerHighest`.
 */
@Composable
fun CircularProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 200.dp,
    strokeWidth: Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val start = AuraTheme.colors.gradientStart
    val end = AuraTheme.colors.gradientSweepEnd
    val bloom = AuraTheme.colors.glow
    Box(modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = strokeWidth.toPx()
            val d = size.minDimension - sw
            val topLeft = Offset((size.width - d) / 2f, (size.height - d) / 2f)
            val arcSize = Size(d, d)
            val sweep = 360f * progress.coerceIn(0f, 1f)
            drawArc(track, 0f, 360f, false, topLeft, arcSize, style = Stroke(sw, cap = StrokeCap.Round))
            // Widened, low-alpha pass underneath stands in for the CSS drop-shadow bloom.
            if (sweep > 0f) {
                drawArc(
                    color = bloom.copy(alpha = 0.28f),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(sw * 1.7f, cap = StrokeCap.Round),
                )
            }
            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(start, end),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height),
                ),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(sw, cap = StrokeCap.Round),
            )
        }
        content()
    }
}

/**
 * Indeterminate pill progress bar (Splash).
 *
 * M3's `LinearProgressIndicator` only takes a flat colour, and the design wants the brand ramp:
 * a 35%-wide gradient lozenge sliding across a `surfaceContainerHigh` track. Timing is the
 * design's `slide` keyframe — 1.3s, ease-in-out, from just off the left edge to off the right.
 */
@Composable
fun AuraIndeterminateBar(
    modifier: Modifier = Modifier,
    height: Dp = 4.dp,
) {
    val shape = CircleShape
    val transition = rememberInfiniteTransition(label = "slide")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "slide-progress",
    )
    val start = AuraTheme.colors.gradientStart
    val end = MaterialTheme.colorScheme.primary
    Box(
        modifier
            .height(height)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val fillW = size.width * 0.35f
            // translateX(-110% → 360%) of the lozenge's own width.
            val x = fillW * (-1.10f + 4.70f * t)
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(start, end), startX = x, endX = x + fillW),
                topLeft = Offset(x, 0f),
                size = Size(fillW, size.height),
                cornerRadius = CornerRadius(size.height / 2f),
            )
        }
    }
}

enum class StageState { Done, Active, Pending }

/**
 * A single step row in a multi-stage process (Processing screen).
 *
 * The state lives in the **leading tile**, not a trailing indicator: done is a green check on a
 * success-tinted tile, active is a violet spinner on a violet-tinted tile with a violet label,
 * pending is an empty tile and the whole row dimmed to 45%.
 */
@Composable
fun StageRow(
    title: String,
    state: StageState,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val success = AuraTheme.colors.success
    val primary = MaterialTheme.colorScheme.primary
    val tileColor = when (state) {
        StageState.Done -> success.copy(alpha = 0.14f)
        StageState.Active -> primary.copy(alpha = 0.16f)
        StageState.Pending -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val titleColor = when (state) {
        StageState.Active -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurface
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (state == StageState.Pending) 0.45f else 1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(AuraShapes.small).background(tileColor),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                StageState.Done -> Icon(
                    Icons.Rounded.Check,
                    contentDescription = "Done",
                    tint = success,
                    modifier = Modifier.size(18.dp),
                )
                StageState.Active -> CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.5.dp,
                    color = primary,
                    trackColor = primary.copy(alpha = 0.25f),
                )
                // Pending tiles stay empty in the design — the dimmed row carries the meaning.
                StageState.Pending -> icon?.let {
                    Icon(
                        it,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        Text(
            title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall,
            color = titleColor,
        )
    }
}

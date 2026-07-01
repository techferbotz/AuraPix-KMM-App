package com.ferbotz.aurapix.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Big circular progress ring (Processing screen). [content] is centered inside, e.g. "45%". */
@Composable
fun CircularProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 200.dp,
    strokeWidth: Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val track = MaterialTheme.colorScheme.surfaceContainerHigh
    val accent = MaterialTheme.colorScheme.primaryContainer
    Box(modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = strokeWidth.toPx()
            val d = size.minDimension - sw
            val topLeft = Offset((size.width - d) / 2f, (size.height - d) / 2f)
            val arcSize = Size(d, d)
            drawArc(track, 0f, 360f, false, topLeft, arcSize, style = Stroke(sw, cap = StrokeCap.Round))
            drawArc(
                accent,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(sw, cap = StrokeCap.Round),
            )
        }
        content()
    }
}

enum class StageState { Done, Active, Pending }

/** A single step row in a multi-stage process (Processing screen). */
@Composable
fun StageRow(
    icon: ImageVector,
    title: String,
    state: StageState,
    modifier: Modifier = Modifier,
) {
    val active = state != StageState.Pending
    val iconBg = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
    val iconFg = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val titleColor = if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconFg, modifier = Modifier.size(20.dp))
        }
        Text(
            title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall,
            color = titleColor,
        )
        when (state) {
            StageState.Done -> Icon(
                Icons.Rounded.Check,
                contentDescription = "Done",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            StageState.Active -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
            )
            StageState.Pending -> {}
        }
    }
}

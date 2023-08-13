package com.sarahang.playback.ui.playback.timer.widget

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp

@Composable
internal fun AnimatedClock(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    contentColor: Color = Color.White
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Creates a child animation of float type as a part of the [InfiniteTransition].
    val clockAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(AnimationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        drawCircle(
            color = color,
            radius = size.width / 2,
            center = center,
        )

        withTransform(
            transformBlock = { rotate(clockAnimation, center) },
            drawBlock = {
                drawLine(
                    color = contentColor,
                    start = center,
                    end = Offset(center.x, center.y - size.width / 2 + 4.dp.toPx()),
                    strokeWidth = HandStrokeWidth.dp.toPx()
                )
            }
        )
    }
}

private const val AnimationDuration = 8_000
private const val HandStrokeWidth = 1.5

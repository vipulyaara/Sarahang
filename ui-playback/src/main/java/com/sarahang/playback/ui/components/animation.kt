package com.sarahang.playback.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sarahang.playback.core.PLAYBACK_PROGRESS_INTERVAL

@Composable
fun animatePlaybackProgress(
    targetValue: Float,
) = animateFloatAsState(
    targetValue = targetValue,
    animationSpec = tween(
        durationMillis = PLAYBACK_PROGRESS_INTERVAL.toInt(),
        easing = FastOutSlowInEasing
    )
)

@Composable
fun AnimatedVisibilityFade(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: EnterTransition = fadeIn(),
    exit: ExitTransition = fadeOut(),
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enter,
        exit = exit,
        content = content
    )
}

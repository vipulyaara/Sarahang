/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package com.sarahang.playback.ui.sheet

import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.isBuffering
import com.sarahang.playback.core.millisToDuration
import com.sarahang.playback.core.models.LocalPlaybackConnection
import com.sarahang.playback.core.models.PlaybackProgressState
import com.sarahang.playback.ui.components.Slider
import com.sarahang.playback.ui.components.SliderDefaults
import com.sarahang.playback.ui.components.animatePlaybackProgress
import kotlin.math.roundToLong

@Composable
fun PlaybackProgress(
    playbackState: PlaybackStateCompat,
    modifier: Modifier = Modifier,
    thumbRadius: Dp = 4.dp,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    val progressState by playbackConnection.playbackProgress.collectAsStateWithLifecycle()
    val (draggingProgress, setDraggingProgress) = remember { mutableStateOf<Float?>(null) }
    val isDragging by remember { derivedStateOf { draggingProgress != null } }

    Box(modifier) {
        PlaybackProgressSlider(
            playbackState = playbackState,
            progressState = progressState,
            draggingProgress = draggingProgress,
            setDraggingProgress = setDraggingProgress,
            thumbRadius = if (isDragging) thumbRadius * 3 else thumbRadius,
            contentColor = contentColorFor(LocalContentColor.current)
        )
        PlaybackProgressDuration(progressState, draggingProgress, thumbRadius)
    }
}

@Composable
internal fun PlaybackProgressSlider(
    playbackState: PlaybackStateCompat,
    progressState: PlaybackProgressState,
    draggingProgress: Float?,
    setDraggingProgress: (Float?) -> Unit,
    thumbRadius: Dp,
    contentColor: Color,
    bufferedProgressColor: Color = contentColor.copy(alpha = 0.25f),
    height: Dp = 44.dp,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current
) {
    val updatedProgressState by rememberUpdatedState(progressState)
    val updatedDraggingProgress by rememberUpdatedState(draggingProgress)

    val inactiveTrackColor = contentColor.copy(alpha = 0.38f)
    val sliderColors = SliderDefaults.colors(
        thumbColor = contentColor,
        activeTrackColor = contentColor,
        inactiveTrackColor = inactiveTrackColor,
    )
    val linearProgressMod = Modifier
        .fillMaxWidth(fraction = .99f) // reduce linearProgressIndicators width to match Slider's
        .clip(CircleShape) // because Slider is rounded

    val bufferedProgress by animatePlaybackProgress(progressState.bufferedProgress)
    val isBuffering = playbackState.isBuffering
    val sliderProgress = progressState.progress

    Box(
        modifier = Modifier.height(height),
        contentAlignment = Alignment.Center
    ) {
        if (!isBuffering)
            LinearProgressIndicator(
                progress = { bufferedProgress },
                color = bufferedProgressColor,
                trackColor = Color.Transparent,
                modifier = linearProgressMod
            )

        Slider(
            value = draggingProgress ?: sliderProgress,
            onValueChange = {
                if (!isBuffering) setDraggingProgress(it)
            },
            thumbRadius = thumbRadius,
            colors = sliderColors,
            modifier = Modifier.alpha(isBuffering.not().toFloat()),
            onValueChangeFinished = {
                playbackConnection.transportControls?.seekTo(
                    (updatedProgressState.total.toFloat() * (updatedDraggingProgress
                        ?: 0f)).roundToLong()
                )
                setDraggingProgress(null)
            }
        )

        if (isBuffering) {
            LinearProgressIndicator(
                progress = { 0f },
                color = contentColor,
                trackColor = inactiveTrackColor,
                modifier = linearProgressMod
            )
            LinearProgressIndicator(
                color = contentColor,
                trackColor = inactiveTrackColor,
                modifier = Modifier
                    .align(Alignment.Center)
                    .then(linearProgressMod)
            )
        }
    }
}

@Composable
internal fun BoxScope.PlaybackProgressDuration(
    progressState: PlaybackProgressState,
    draggingProgress: Float?,
    thumbRadius: Dp
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = thumbRadius)
            .align(Alignment.BottomCenter)
    ) {
        val currentDuration = when (draggingProgress != null) {
            true -> (progressState.total.toFloat() * (draggingProgress)).toLong()
                .millisToDuration()

            else -> progressState.currentDuration
        }


        Text(
            text = currentDuration,
            style = MaterialTheme.typography.bodySmall,
            color = LocalContentColor.current.copy(alpha = 0.6f)
        )
        Text(
            text = progressState.totalDuration,
            style = MaterialTheme.typography.bodySmall,
            color = LocalContentColor.current.copy(alpha = 0.6f)
        )
    }
}

fun Boolean.toFloat() = if (this) 1f else 0f

@file:OptIn(ExperimentalMaterial3Api::class)

package com.sarahang.playback.ui.playback.speed

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kafka.ui_playback.generated.resources.Res
import kafka.ui_playback.generated.resources.playback_speed
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun PlaybackSpeed(
    viewModelFactory: () -> PlaybackSpeedViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val viewModel = viewModel { viewModelFactory() }
    val currentSpeed by viewModel.currentSpeed.collectAsStateWithLifecycle()

    val dismissSheet: () -> Unit = {
        coroutineScope
            .launch { sheetState.hide() }
            .invokeOnCompletion { onDismiss() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.large,
        containerColor = colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(Res.string.playback_speed),
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.size(16.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.size(24.dp))

            val sliderState = rememberPodcastSliderState(currentValue = currentSpeed * 10)

            PodcastSlider(state = sliderState)

            Spacer(modifier = Modifier.size(24.dp))

            val hapticFeedback = LocalHapticFeedback.current
            LaunchedEffect(sliderState.currentValue) {
                viewModel.setSpeedRaw(sliderState.currentValue)
            }

            Spacer(modifier = Modifier.size(44.dp))

            QuickSpeedRow(
                intervals = viewModel.quickSpeedIntervals,
                currentSpeed = currentSpeed
            ) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.setSpeed(it)
                dismissSheet()
            }
        }
    }
}

@Composable
private fun QuickSpeedRow(
    intervals: List<Float>,
    currentSpeed: Float,
    modifier: Modifier = Modifier,
    onSpeedChanged: (Float) -> Unit
) {
    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        items(intervals) { speed ->
            val background by animateColorAsState(
                targetValue = if (speed == currentSpeed) colorScheme.primary else colorScheme.surfaceVariant,
                label = "background"
            )
            val contentColor by animateColorAsState(
                targetValue = if (speed == currentSpeed) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                label = "content"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(background)
                    .clickable { onSpeedChanged(speed) }
            ) {
                Text(
                    text = speed.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

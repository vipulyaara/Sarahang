package com.sarahang.playback.ui.playback

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.sarahang.playback.ui.components.icons.Icons
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map

/**
 * State class for the SpeedSelector component that manages playback speed selection
 */
class SpeedSelectorState(
    initialSpeed: Float = 1.0f
) {
    private val minSpeed = 0.5f
    private val maxSpeed = 5.0f
    private val speedStep = 0.1f
    private val majorStepInterval = 5 // Represents 0.5 increments (5 * 0.1 = 0.5)

    // Calculate total ticks needed for our speed range
    val totalTicks = ((maxSpeed - minSpeed) / speedStep).toInt() + 1

    // Calculate initial index for the lazy list
    val initialIndex = ((initialSpeed - minSpeed) / speedStep).toInt()

    // Convert index to speed value
    fun indexToSpeed(index: Int): Float {
        return minSpeed + (index * speedStep)
    }

    // Determine if the tick should be a major one (every 0.5 increment)
    fun isMajorTick(index: Int): Boolean {
        return index % majorStepInterval == 0
    }
}

@Composable
fun SpeedSelector(
    modifier: Modifier = Modifier,
    initialSpeed: Float = 1.0f,
    barColor: Color = MaterialTheme.colorScheme.onSurface,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    onSpeedSelected: (Float) -> Unit = {}
) {
    val speedSelectorState = remember { SpeedSelectorState(initialSpeed) }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = speedSelectorState.initialIndex)

    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current

    // Derive the current speed from list state
    val currentSpeed by remember {
        derivedStateOf {
            speedSelectorState.indexToSpeed(listState.firstVisibleItemIndex)
        }
    }

    // Notify about speed changes
    LaunchedEffect(currentSpeed) {
        onSpeedSelected(currentSpeed)
    }

    // Provide haptic feedback when scrolling
    LaunchedEffect(Unit) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .drop(1)
            .distinctUntilChanged()
            .map { speedSelectorState.isMajorTick(it) }
            .collect { isMajorTick ->
                if (isMajorTick) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                } else {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
    }

    var padding by remember { mutableStateOf(0.dp) }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = currentSpeed.toString() + "x",
            style = MaterialTheme.typography.titleLarge,
            color = primaryColor,
        )
        Icon(
            imageVector = Icons.ChevronDown,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(32.dp)
        )

        Box(
            modifier = modifier
                .onSizeChanged { padding = with(density) { (it.width / 2).toDp() } }
                .fillMaxWidth()
                .height(84.dp)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(horizontal = (padding).coerceAtLeast(0.dp)),
                verticalAlignment = Alignment.CenterVertically,
                flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
            ) {
                items(speedSelectorState.totalTicks) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .fillMaxHeight(
                                when {
                                    speedSelectorState.isMajorTick(index) -> .9f
                                    else -> .5f
                                }
                            )
                            .width(3.dp)
                            .background(
                                barColor.copy(
                                    alpha = when {
                                        speedSelectorState.isMajorTick(index) -> 1f
                                        else -> .4f
                                    }
                                ), shape = CircleShape
                            )
                    )
                }
            }

            // Gradient overlay for fade effect on sides
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                Color.Transparent,
                                Color.Transparent,
                                MaterialTheme.colorScheme.surface,
                            )
                        )
                    )
            )

            // Speed display
//        SpeedDisplay(
//            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
//            speed = currentSpeed
//        )
        }
    }
}

@Composable
private fun SpeedDisplay(
    modifier: Modifier = Modifier,
    speed: Float
) {
    Text(
        text = speed.toString(),
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}

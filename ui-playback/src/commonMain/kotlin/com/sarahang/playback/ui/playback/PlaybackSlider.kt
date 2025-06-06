package com.sarahang.playback.ui.playback

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import kotlinx.coroutines.flow.drop

@Composable
fun PlaybackSlider() {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 32)

    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .drop(1)
            .collect { index ->
                when (index) {
                    0, listState.layoutInfo.totalItemsCount - 1 -> {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }

                    else -> {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }
            }
    }

    var padding by remember { mutableStateOf(0.dp) }

    Box(
        modifier = Modifier
            .onSizeChanged { padding = with(density) { (it.width / 2).toDp() } }
            .fillMaxSize()
    ) {
        LazyRow(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(horizontal = (padding - 5.dp).coerceAtLeast(0.dp)),
            verticalAlignment = Alignment.CenterVertically,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
        ) {
            items(201) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .fillMaxHeight(
                            when (index % 10) {
                                0 -> .3f
                                else -> .1f
                            }
                        )
                        .width(2.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 4.dp)
                .fillMaxHeight(.9f)
                .width(5.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = .4f), shape = CircleShape)
        )

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

        StationDisplay(
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            listState = listState
        )
    }
}

@Composable
private fun StationDisplay(
    modifier: Modifier = Modifier,
    listState: LazyListState,
) {

    val station by remember {
        derivedStateOf {
            val x = 850 + listState.firstVisibleItemIndex
            "${x / 10}.${x % 10}"
        }
    }

    Text(
        text = "$station FM",
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}

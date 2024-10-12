/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package com.sarahang.playback.ui.sheet

import android.support.v4.media.MediaMetadataCompat
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.models.Audio
import com.sarahang.playback.core.models.LocalPlaybackConnection
import com.sarahang.playback.core.models.toAudio
import com.sarahang.playback.ui.theme.Specs
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlin.math.absoluteValue

@Composable
internal fun PlaybackPager(
    nowPlaying: MediaMetadataCompat,
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    currentIndex: Int = 0,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
    content: @Composable (Audio, Int, Modifier) -> Unit,
) {
    val playbackQueue by rememberFlowWithLifecycle(playbackConnection.playbackQueue)
    val playbackCurrentIndex = playbackQueue.currentIndex
    val pagerState = rememberPagerState(currentIndex, pageCount =  { playbackQueue.size })
    var lastRequestedPage by remember(playbackQueue, nowPlaying) {
        mutableStateOf<Int?>(playbackCurrentIndex)
    }

    if (!playbackQueue.isValid) {
        content(nowPlaying.toAudio(), playbackCurrentIndex, modifier)
        return
    }
    LaunchedEffect(Unit) {
        pagerState.scrollToPage(playbackCurrentIndex)
    }
    LaunchedEffect(playbackCurrentIndex, pagerState) {
        if (playbackCurrentIndex != pagerState.currentPage) {
            pagerState.animateScrollToPage(playbackCurrentIndex)
        }
        snapshotFlow { pagerState.isScrollInProgress }
            .filter { !it }
            .map { pagerState.currentPage }
            .collectLatest { page ->
                if (lastRequestedPage != page) {
                    lastRequestedPage = page
                    playbackConnection.skipToQueueItem(page)
                }
            }
    }

    HorizontalPager(
        modifier = modifier,
        state = pagerState,
        contentPadding = PaddingValues(horizontal = Specs.paddingLarge),
        key = { playbackQueue.getOrNull(it)?.id ?: it },
        verticalAlignment = verticalAlignment,
    ) { page ->
        val currentAudio = playbackQueue.getOrNull(page) ?: Audio.unknown

        val pagerMod = Modifier
            .graphicsLayer {
                // Calculate the absolute offset for the current page from the
                // scroll position. We use the absolute value which allows us to mirror
                // any effects for both directions
                val pageOffset = (
                        (pagerState.currentPage - page) + pagerState
                            .currentPageOffsetFraction
                        ).absoluteValue

                // We animate the alpha, between 50% and 100%
                alpha = lerp(
                    start = 0.5f,
                    stop = 1f,
                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                )
            }
        content(currentAudio, page, pagerMod)
    }
}

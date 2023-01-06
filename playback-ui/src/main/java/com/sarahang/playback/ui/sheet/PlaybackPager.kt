/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package com.sarahang.playback.ui.sheet

import android.support.v4.media.MediaMetadataCompat
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.calculateCurrentOffsetForPage
import com.google.accompanist.pager.rememberPagerState
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.models.Audio
import com.sarahang.playback.core.models.LocalPlaybackConnection
import com.sarahang.playback.core.models.PlaybackQueue
import com.sarahang.playback.core.models.toAudio
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlin.math.absoluteValue

@Composable
internal fun PlaybackPager(
    nowPlaying: MediaMetadataCompat,
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    pagerState: PagerState = rememberPagerState(),
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
    content: @Composable (Audio, Int, Modifier) -> Unit,
) {
    val playbackQueue by playbackConnection.playbackQueue.collectAsStateWithLifecycle(PlaybackQueue())
    val playbackCurrentIndex = playbackQueue.currentIndex
    var lastRequestedPage by remember(playbackQueue, nowPlaying) {
        mutableStateOf<Int?>(
            playbackCurrentIndex
        )
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
                    playbackConnection.transportControls?.skipToQueueItem(page.toLong())
                }
            }
    }
    HorizontalPager(
        count = playbackQueue.size,
        modifier = modifier,
        state = pagerState,
        key = { playbackQueue.getOrNull(it) ?: it },
        verticalAlignment = verticalAlignment,
    ) { page ->
        val currentAudio = playbackQueue.getOrNull(page) ?: Audio.unknown

        val pagerMod = Modifier.graphicsLayer {
            val pageOffset = calculateCurrentOffsetForPage(page).absoluteValue
            // TODO: report to upstream if can be reproduced in isolation
            if (pageOffset.isNaN()) {
                return@graphicsLayer
            }

            lerp(
                start = 0.85f,
                stop = 1f,
                fraction = 1f - pageOffset.coerceIn(0f, 1f)
            ).also { scale ->
                scaleX = scale
                scaleY = scale
            }
            alpha = lerp(
                start = 0.5f,
                stop = 1f,
                fraction = 1f - pageOffset.coerceIn(0f, 1f)
            )
        }
        content(currentAudio, page, pagerMod)
    }
}

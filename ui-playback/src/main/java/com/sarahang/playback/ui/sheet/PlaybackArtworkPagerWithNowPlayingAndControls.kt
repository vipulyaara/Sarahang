/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package com.sarahang.playback.ui.sheet

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.sarahang.playback.ui.playback.speed.PlaybackSpeedViewModel
import com.sarahang.playback.ui.playback.timer.SleepTimerViewModel

@Composable
fun PlaybackArtworkPagerWithNowPlayingAndControls(
    nowPlaying: MediaMetadataCompat,
    playbackState: PlaybackStateCompat,
    modifier: Modifier = Modifier,
    artworkVerticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    titleTextStyle: TextStyle = PlaybackNowPlayingDefaults.titleTextStyle,
    artistTextStyle: TextStyle = PlaybackNowPlayingDefaults.artistTextStyle,
    currentIndex: Int = 0,
    isMiniPlayer: Boolean = false,
    onArtworkClick: (() -> Unit)? = null,
    onTitleClick: () -> Unit = {},
    onArtistClick: () -> Unit = {},
    sleepTimerViewModelFactory: () -> SleepTimerViewModel,
    playbackSpeedViewModelFactory: () -> PlaybackSpeedViewModel,
) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        PlaybackPager(
            nowPlaying = nowPlaying,
            currentIndex = currentIndex,
            modifier = Modifier,
            verticalAlignment = artworkVerticalAlignment,
        ) { audio, _, pagerMod ->
            PlaybackArtwork(
                artwork = audio.coverImage,
                nowPlaying = nowPlaying,
                onClick = onArtworkClick,
                modifier = pagerMod,
            )
        }

        PlaybackNowPlayingWithControls(
            nowPlaying = nowPlaying,
            playbackState = playbackState,
            titleTextStyle = titleTextStyle,
            artistTextStyle = artistTextStyle,
            isMiniPlayer = isMiniPlayer,
            onTitleClick = onTitleClick,
            onArtistClick = onArtistClick,
            sleepTimerViewModelFactory = sleepTimerViewModelFactory,
            playbackSpeedViewModelFactory = playbackSpeedViewModelFactory,
        )
    }
}

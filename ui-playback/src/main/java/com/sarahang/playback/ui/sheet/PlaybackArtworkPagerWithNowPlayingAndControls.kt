/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package com.sarahang.playback.ui.sheet

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.sarahang.playback.ui.audio.ADAPTIVE_COLOR_ANIMATION
import com.sarahang.playback.ui.audio.AdaptiveColorResult
import com.sarahang.playback.ui.audio.toAdaptiveColor

@Composable
fun PlaybackArtworkPagerWithNowPlayingAndControls(
    nowPlaying: MediaMetadataCompat,
    playbackState: PlaybackStateCompat,
    modifier: Modifier = Modifier,
    adaptiveColor: AdaptiveColorResult = MaterialTheme.colorScheme.background
        .toAdaptiveColor(isSystemInDarkTheme()),
    artworkVerticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    titleTextStyle: TextStyle = PlaybackNowPlayingDefaults.titleTextStyle,
    artistTextStyle: TextStyle = PlaybackNowPlayingDefaults.artistTextStyle,
    currentIndex: Int = 0,
    onArtworkClick: (() -> Unit)? = null,
    onTitleClick: () -> Unit = {},
    onArtistClick: () -> Unit = {}
) {
    val color by animateColorAsState(adaptiveColor.color, ADAPTIVE_COLOR_ANIMATION)
    ConstraintLayout(modifier = modifier.fillMaxSize()) {
        val (pager, nowPlayingControls) = createRefs()
        PlaybackPager(
            nowPlaying = nowPlaying,
            currentIndex = currentIndex,
            modifier = Modifier
                .constrainAs(pager) {
                    centerHorizontallyTo(parent)
                    top.linkTo(parent.top)
                    bottom.linkTo(nowPlayingControls.top)
                    height = Dimension.fillToConstraints
                },
            verticalAlignment = artworkVerticalAlignment,
        ) { audio, _, pagerMod ->
            PlaybackArtwork(
                artwork = audio.coverImage,
                contentColor = color,
                nowPlaying = nowPlaying,
                onClick = onArtworkClick,
                modifier = pagerMod,
            )
        }

        CompositionLocalProvider(LocalContentColor provides color) {
            PlaybackNowPlayingWithControls(
                nowPlaying = nowPlaying,
                adaptiveColor = adaptiveColor,
                playbackState = playbackState,
                titleTextStyle = titleTextStyle,
                artistTextStyle = artistTextStyle,
                onTitleClick = onTitleClick,
                onArtistClick = onArtistClick,
                modifier = Modifier.constrainAs(nowPlayingControls) {
                    centerHorizontallyTo(parent)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                }
            )
        }
    }
}

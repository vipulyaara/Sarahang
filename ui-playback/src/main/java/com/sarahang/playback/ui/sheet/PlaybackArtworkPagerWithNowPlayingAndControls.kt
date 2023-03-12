/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package com.sarahang.playback.ui.sheet

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState

@Composable
fun PlaybackArtworkPagerWithNowPlayingAndControls(
    nowPlaying: MediaMetadataCompat,
    playbackState: PlaybackStateCompat,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    artworkVerticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    titleTextStyle: TextStyle = PlaybackNowPlayingDefaults.titleTextStyle,
    artistTextStyle: TextStyle = PlaybackNowPlayingDefaults.artistTextStyle,
    pagerState: PagerState = rememberPagerState(),
    onArtworkClick: (() -> Unit)? = null,
    onTitleClick: () -> Unit = {},
    onArtistClick: () -> Unit = {},
) {
    ConstraintLayout(modifier = modifier.fillMaxSize()) {
        val (pager, nowPlayingControls) = createRefs()
        PlaybackPager(
            nowPlaying = nowPlaying,
            pagerState = pagerState,
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
        PlaybackNowPlayingWithControls(
            nowPlaying = nowPlaying,
            playbackState = playbackState,
            color = color,
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

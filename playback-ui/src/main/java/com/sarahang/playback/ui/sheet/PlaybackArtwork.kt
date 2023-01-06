/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package com.sarahang.playback.ui.sheet

import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.artwork
import com.sarahang.playback.core.models.LocalPlaybackConnection
import com.sarahang.playback.core.playPause
import com.sarahang.playback.ui.audio.AdaptiveColorResult
import com.sarahang.playback.ui.audio.adaptiveColor
import com.sarahang.playback.ui.components.CoverImage
import com.sarahang.playback.ui.theme.Specs
import com.sarahang.playback.ui.theme.coloredRippleClickable
import com.sarahang.playback.ui.theme.plainSurfaceColor

@Composable
internal fun PlaybackArtwork(
    artwork: Uri,
    contentColor: Color,
    nowPlaying: MediaMetadataCompat,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    CoverImage(
        data = artwork,
        shape = RectangleShape,
        containerColor = plainSurfaceColor(),
        contentColor = contentColor,
        bitmapPlaceholder = nowPlaying.artwork,
        modifier = Modifier
            .padding(horizontal = Specs.paddingLarge)
            .then(modifier),
        imageModifier = Modifier.coloredRippleClickable(
            onClick = {
                if (onClick != null) onClick.invoke()
                else playbackConnection.mediaController?.playPause()
            },
            color = contentColor,
            rippleRadius = Dp.Unspecified,
        ),
    )
}

@Composable
fun nowPlayingArtworkAdaptiveColor(
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current
): State<AdaptiveColorResult> {
    val nowPlaying by playbackConnection.nowPlaying.collectAsStateWithLifecycle()
    return adaptiveColor(nowPlaying.artwork, initial = MaterialTheme.colorScheme.background)
}

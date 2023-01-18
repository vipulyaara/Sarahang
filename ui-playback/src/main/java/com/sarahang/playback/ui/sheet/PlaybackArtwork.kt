package com.sarahang.playback.ui.sheet

import android.support.v4.media.MediaMetadataCompat
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.core.net.toUri
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.artwork
import com.sarahang.playback.core.models.LocalPlaybackConnection
import com.sarahang.playback.core.playPause
import com.sarahang.playback.ui.components.CoverImage
import com.sarahang.playback.ui.theme.coloredRippleClickable
import com.sarahang.playback.ui.theme.plainSurfaceColor

@Composable
internal fun PlaybackArtwork(
    artwork: String?,
    contentColor: Color,
    nowPlaying: MediaMetadataCompat,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    CoverImage(
        data = artwork?.toUri(),
        shape = RectangleShape,
        containerColor = plainSurfaceColor(),
        contentColor = contentColor,
        bitmapPlaceholder = nowPlaying.artwork,
        modifier = Modifier.then(modifier),
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

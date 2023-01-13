package com.sarahang.playback.ui.sheet

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOn
import androidx.compose.material.icons.filled.RepeatOneOn
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.ShuffleOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.artist
import com.sarahang.playback.core.hasNext
import com.sarahang.playback.core.hasPrevious
import com.sarahang.playback.core.isError
import com.sarahang.playback.core.isPlayEnabled
import com.sarahang.playback.core.isPlaying
import com.sarahang.playback.core.models.LocalPlaybackConnection
import com.sarahang.playback.core.models.PlaybackModeState
import com.sarahang.playback.core.playPause
import com.sarahang.playback.core.title
import com.sarahang.playback.core.toggleRepeatMode
import com.sarahang.playback.core.toggleShuffleMode
import com.sarahang.playback.ui.components.IconButton
import com.sarahang.playback.ui.theme.Specs
import com.sarahang.playback.ui.theme.disabledAlpha
import com.sarahang.playback.ui.theme.orNa
import com.sarahang.playback.ui.theme.simpleClickable
import com.sarahang.playback.ui.components.icons.Icons as PlayerIcons

object PlaybackNowPlayingDefaults {
    val titleTextStyle @Composable get() = MaterialTheme.typography.titleMedium
    val artistTextStyle @Composable get() = MaterialTheme.typography.titleSmall
}

@Composable
internal fun PlaybackNowPlayingWithControls(
    nowPlaying: MediaMetadataCompat,
    playbackState: PlaybackStateCompat,
    contentColor: Color,
    onTitleClick: () -> Unit,
    onArtistClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleTextStyle: TextStyle = PlaybackNowPlayingDefaults.titleTextStyle,
    artistTextStyle: TextStyle = PlaybackNowPlayingDefaults.artistTextStyle,
    onlyControls: Boolean = false,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(32.dp)
    ) {
        if (!onlyControls)
            PlaybackNowPlaying(
                nowPlaying = nowPlaying,
                onTitleClick = onTitleClick,
                onArtistClick = onArtistClick,
                titleTextStyle = titleTextStyle,
                artistTextStyle = artistTextStyle,
                contentColor = contentColor,
            )

        PlaybackProgress(
            playbackState = playbackState,
            contentColor = contentColor
        )

        PlaybackControls(
            playbackState = playbackState,
            contentColor = contentColor,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
internal fun PlaybackNowPlaying(
    nowPlaying: MediaMetadataCompat,
    onTitleClick: () -> Unit,
    onArtistClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleTextStyle: TextStyle = PlaybackNowPlayingDefaults.titleTextStyle,
    artistTextStyle: TextStyle = PlaybackNowPlayingDefaults.artistTextStyle,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
    contentColor: Color = MaterialTheme.colorScheme.surface,
) {
    val playbackMode by playbackConnection.playbackMode.collectAsStateWithLifecycle()
    Column(
        horizontalAlignment = horizontalAlignment,
        modifier = modifier
    ) {
        Text(
            text = nowPlaying.title.orNa(),
            style = titleTextStyle,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier
                .simpleClickable(onClick = onTitleClick)
                .basicMarquee()
        )
        Text(
            text = nowPlaying.artist.orNa(),
            style = artistTextStyle,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .simpleClickable(onClick = onArtistClick)
                .basicMarquee()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ShuffleButton(
                playbackConnection = playbackConnection,
                smallRippleRadius = 30.dp,
                playbackMode = playbackMode,
                contentColor = contentColor
            )

            RepeatButton(
                playbackConnection = playbackConnection,
                smallRippleRadius = 30.dp,
                playbackMode = playbackMode,
                contentColor = contentColor
            )
        }
    }
}

@Composable
internal fun PlaybackControls(
    playbackState: PlaybackStateCompat,
    contentColor: Color,
    modifier: Modifier = Modifier,
    smallRippleRadius: Dp = 30.dp,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { playbackConnection.transportControls?.rewind() },
            rippleRadius = smallRippleRadius,
            modifier = Modifier
                .size(20.dp)
                .weight(2f)
        ) {
            Icon(
                painter = rememberVectorPainter(PlayerIcons.Rewind),
                tint = contentColor,
                modifier = Modifier.fillMaxSize(),
                contentDescription = null
            )
        }

        Spacer(Modifier.width(Specs.paddingLarge))

        IconButton(
            onClick = { playbackConnection.transportControls?.skipToPrevious() },
            rippleRadius = smallRippleRadius,
            modifier = Modifier
                .size(40.dp)
                .weight(4f)
        ) {
            Icon(
                painter = rememberVectorPainter(PlayerIcons.Previous),
                tint = contentColor.disabledAlpha(playbackState.hasPrevious),
                modifier = Modifier.fillMaxSize(),
                contentDescription = null
            )
        }

        Spacer(Modifier.width(Specs.padding))

        IconButton(
            onClick = { playbackConnection.mediaController?.playPause() },
            modifier = Modifier
                .size(80.dp)
                .weight(8f),
            rippleRadius = 35.dp,
        ) {
            Icon(
                painter = rememberVectorPainter(
                    when {
                        playbackState.isError -> Icons.Filled.ErrorOutline
                        playbackState.isPlaying -> PlayerIcons.Pause
                        playbackState.isPlayEnabled -> PlayerIcons.PlayCircle
                        else -> PlayerIcons.PlayCircle
                    }
                ),
                tint = contentColor,
                modifier = Modifier.fillMaxSize(),
                contentDescription = null
            )
        }

        Spacer(Modifier.width(Specs.padding))

        IconButton(
            onClick = { playbackConnection.transportControls?.skipToNext() },
            rippleRadius = smallRippleRadius,
            modifier = Modifier
                .size(40.dp)
                .weight(4f)
        ) {
            Icon(
                painter = rememberVectorPainter(PlayerIcons.Next),
                tint = contentColor.disabledAlpha(playbackState.hasNext),
                modifier = Modifier.fillMaxSize(),
                contentDescription = null
            )
        }

        Spacer(Modifier.width(Specs.paddingLarge))

        IconButton(
            onClick = { playbackConnection.transportControls?.fastForward() },
            rippleRadius = smallRippleRadius,
            modifier = Modifier
                .size(20.dp)
                .weight(2f)
        ) {
            Icon(
                painter = rememberVectorPainter(PlayerIcons.FasForward),
                tint = contentColor,
                modifier = Modifier.fillMaxSize(),
                contentDescription = null
            )
        }
    }
}

@Composable
private fun RepeatButton(
    playbackConnection: PlaybackConnection,
    smallRippleRadius: Dp,
    playbackMode: PlaybackModeState,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = { playbackConnection.mediaController?.toggleRepeatMode() },
        rippleRadius = smallRippleRadius,
        modifier = modifier.size(20.dp)
    ) {
        Icon(
            painter = rememberVectorPainter(
                when (playbackMode.repeatMode) {
                    PlaybackStateCompat.REPEAT_MODE_ONE -> Icons.Default.RepeatOneOn
                    PlaybackStateCompat.REPEAT_MODE_ALL -> Icons.Default.RepeatOn
                    else -> Icons.Default.Repeat
                }
            ),
            tint = contentColor,
            modifier = Modifier.fillMaxSize(),
            contentDescription = null
        )
    }
}

@Composable
private fun ShuffleButton(
    playbackConnection: PlaybackConnection,
    smallRippleRadius: Dp,
    playbackMode: PlaybackModeState,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = { playbackConnection.mediaController?.toggleShuffleMode() },
        modifier = modifier.size(20.dp),
        rippleRadius = smallRippleRadius,
    ) {
        Icon(
            painter = rememberVectorPainter(
                when (playbackMode.shuffleMode) {
                    PlaybackStateCompat.SHUFFLE_MODE_NONE -> Icons.Default.Shuffle
                    PlaybackStateCompat.SHUFFLE_MODE_ALL -> Icons.Default.ShuffleOn
                    else -> Icons.Default.Shuffle
                }
            ),
            tint = contentColor,
            modifier = Modifier.fillMaxSize(),
            contentDescription = null
        )
    }
}

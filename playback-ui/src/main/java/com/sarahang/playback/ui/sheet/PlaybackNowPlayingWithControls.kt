package com.sarahang.playback.ui.sheet

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
    val titleTextStyle @Composable get() = MaterialTheme.typography.headlineSmall
    val artistTextStyle @Composable get() = MaterialTheme.typography.titleMedium
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
        modifier = modifier.padding(Specs.paddingLarge)
    ) {
        if (!onlyControls)
            PlaybackNowPlaying(
                nowPlaying = nowPlaying,
                onTitleClick = onTitleClick,
                onArtistClick = onArtistClick,
                titleTextStyle = titleTextStyle,
                artistTextStyle = artistTextStyle,
            )

        PlaybackProgress(
            playbackState = playbackState,
            contentColor = contentColor
        )

        PlaybackControls(
            playbackState = playbackState,
            contentColor = contentColor,
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
) {
    Column(
        horizontalAlignment = horizontalAlignment,
        modifier = modifier
    ) {
        Text(
            text = nowPlaying.title.orNa(),
            style = titleTextStyle,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier.simpleClickable(onClick = onTitleClick)
        )
        Text(
            text = nowPlaying.artist.orNa(),
            style = artistTextStyle,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.simpleClickable(onClick = onArtistClick)
        )
    }
}

@Composable
internal fun PlaybackControls(
    playbackState: PlaybackStateCompat,
    contentColor: Color,
    modifier: Modifier = Modifier,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current
) {
    val playbackMode by playbackConnection.playbackMode.collectAsStateWithLifecycle()
    Row(
        modifier = modifier.width(288.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { playbackConnection.mediaController?.toggleShuffleMode() },
            modifier = Modifier
                .size(20.dp)
                .weight(2f)
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

        Spacer(Modifier.width(Specs.paddingLarge))

        IconButton(
            onClick = { playbackConnection.transportControls?.skipToPrevious() },
            modifier = Modifier
                .size(40.dp)
                .weight(4f)
        ) {
            Icon(
                painter = rememberVectorPainter(Icons.Default.SkipPrevious),
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
                .weight(8f)
        ) {
            Icon(
                painter = rememberVectorPainter(
                    when {
                        playbackState.isError -> Icons.Filled.ErrorOutline
                        playbackState.isPlaying -> PlayerIcons.Pause
                        playbackState.isPlayEnabled -> PlayerIcons.Play
                        else -> PlayerIcons.Play
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
            modifier = Modifier
                .size(40.dp)
                .weight(4f)
        ) {
            Icon(
                painter = rememberVectorPainter(Icons.Default.SkipNext),
                tint = contentColor.disabledAlpha(playbackState.hasNext),
                modifier = Modifier.fillMaxSize(),
                contentDescription = null
            )
        }

        Spacer(Modifier.width(Specs.paddingLarge))

        IconButton(
            onClick = { playbackConnection.mediaController?.toggleRepeatMode() },
            modifier = Modifier
                .size(20.dp)
                .weight(2f)
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
}

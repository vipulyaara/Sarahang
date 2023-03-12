package com.sarahang.playback.ui.audio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.millisToDuration
import com.sarahang.playback.core.models.Audio
import com.sarahang.playback.core.models.LocalPlaybackConnection
import com.sarahang.playback.core.models.PlaybackQueue.NowPlayingAudio.Companion.isCurrentAudio
import com.sarahang.playback.core.playPause
import com.sarahang.playback.ui.player.mini.PlaybackPlayPause

object AudiosDefaults {
    const val maxLines = 1
}

@Suppress("AnimateAsStateLabel")
@Composable
fun AudioRow(
    audio: Audio,
    modifier: Modifier = Modifier,
    onClick: ((Audio) -> Unit)? = null,
    onPlayAudio: ((Audio) -> Unit)? = null,
    audioIndex: Int = 0,
    adaptiveColor: AdaptiveColorResult = MaterialTheme.colorScheme.background
        .toAdaptiveColor(isSystemInDarkTheme()),
    actionHandler: AudioActionHandler = LocalAudioActionHandler.current,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    val playbackState by playbackConnection.playbackState.collectAsStateWithLifecycle()
    val nowPlayingAudio by playbackConnection.nowPlayingAudio.collectAsStateWithLifecycle()
    val isCurrentAudio = nowPlayingAudio.isCurrentAudio(audio, audioIndex)

    val containerColor by animateColorAsState(if (isCurrentAudio) adaptiveColor.color else Color.Transparent)
    val contentColor =
        if (isCurrentAudio) adaptiveColor.contentColor else MaterialTheme.colorScheme.onBackground

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .combinedClickable(
                onClick = {
                    if (isCurrentAudio) {
                        playbackConnection.mediaController?.playPause()
                    } else {
                        if (onClick != null) onClick(audio)
                        else if (onPlayAudio != null) onPlayAudio(audio)
                        else actionHandler(AudioItemAction.Play(audio))
                    }
                }
            )
            .fillMaxWidth()
            .background(containerColor)
            .padding(PaddingValues(vertical = 8.dp))
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            AudioRowItem(audio = audio, modifier = Modifier.weight(1f)) {
                AnimatedVisibility(isCurrentAudio) {
                    PlaybackPlayPause(
                        modifier = Modifier.padding(start = 8.dp),
                        playbackState = playbackState,
                        onPlayPause = { playbackConnection.mediaController?.playPause() }
                    )
                }
            }
        }
    }
}

@Composable
fun AudioRowItem(
    audio: Audio,
    modifier: Modifier = Modifier,
    maxLines: Int = AudiosDefaults.maxLines,
    contentColor: Color = LocalContentColor.current,
    playPauseButton: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        playPauseButton()

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = audio.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                color = contentColor,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = audio.artist.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.7f),
                    maxLines = maxLines,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .alignByBaseline()
                        .weight(1f, false)
                )
                Text(
                    text = " • " + audio.durationMillis().millisToDuration(),
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.7f),
                    maxLines = maxLines,
                    modifier = Modifier.alignByBaseline()
                )
            }
        }
    }
}

package com.sarahang.playback.ui.audio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.isPlaying
import com.sarahang.playback.core.millisToDuration
import com.sarahang.playback.core.models.Audio
import com.sarahang.playback.core.models.LocalPlaybackConnection
import com.sarahang.playback.core.models.PlaybackQueue.NowPlayingAudio.Companion.isCurrentAudio
import com.sarahang.playback.ui.theme.Specs

object AudiosDefaults {
    val imageSize = 48.dp
    const val maxLines = 1
}

@Composable
fun AudioRow(
    audio: Audio,
    modifier: Modifier = Modifier,
    imageSize: Dp = AudiosDefaults.imageSize,
    onClick: ((Audio) -> Unit)? = null,
    onPlayAudio: ((Audio) -> Unit)? = null,
    audioIndex: Int = 0,
    adaptiveColor: AdaptiveColorResult,
    actionHandler: AudioActionHandler = LocalAudioActionHandler.current,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    var menuVisible by remember { mutableStateOf(false) }

    val playbackState by playbackConnection.playbackState.collectAsStateWithLifecycle()
    val nowPlayingAudio by playbackConnection.nowPlayingAudio.collectAsStateWithLifecycle()
    val isCurrentAudio = nowPlayingAudio.isCurrentAudio(audio, audioIndex)

    val containerColor by animateColorAsState(if (isCurrentAudio) adaptiveColor.backgroundColor else Color.Transparent)
    val borderColor by animateColorAsState(if (isCurrentAudio) adaptiveColor.borderColor else Color.Transparent)
    val contentColor =
        if (isCurrentAudio) adaptiveColor.color else MaterialTheme.colorScheme.onPrimaryContainer

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .drawBehind {
                val strokeWidth = 2.dp.value * density
                val y = size.height - strokeWidth / 2

                drawLine(
                    borderColor,
                    Offset(0f, 0f),
                    Offset(size.width, 0f),
                    strokeWidth
                )

                drawLine(
                    borderColor,
                    Offset(0f, y),
                    Offset(size.width, y),
                    strokeWidth
                )
            }
            .combinedClickable(
                onClick = {
                    if (onClick != null) onClick(audio)
                    else if (onPlayAudio != null) onPlayAudio(audio)
                    else actionHandler(AudioItemAction.Play(audio))
                },
                onLongClick = {
                    menuVisible = true
                }
            )
            .fillMaxWidth()
            .background(containerColor)
            .padding(PaddingValues(vertical = 8.dp))
    ) {
        AudioRowItem(
            audio = audio,
            imageSize = imageSize,
            isCurrentAudio = isCurrentAudio,
            modifier = Modifier.weight(1f),
            adaptiveColor = adaptiveColor.color,
            isPlaying = playbackState.isPlaying
        )

        AudioDropdownMenu(
            expanded = menuVisible,
            onExpandedChange = { menuVisible = it },
            modifier = Modifier.align(Alignment.CenterVertically),
            tint = contentColor,
            onDropdownSelect = {
                val action = AudioItemAction.from(it, audio)
                when {
                    action is AudioItemAction.Play && onPlayAudio != null -> onPlayAudio(audio)
                    else -> actionHandler(action)
                }
            },
        )
    }
}

@Composable
fun AudioRowItem(
    audio: Audio,
    modifier: Modifier = Modifier,
    imageSize: Dp = AudiosDefaults.imageSize,
    maxLines: Int = AudiosDefaults.maxLines,
    adaptiveColor: Color = MaterialTheme.colorScheme.primary,
    isCurrentAudio: Boolean,
    isPlaying: Boolean
) {
    val contentColor by animateColorAsState(
        if (isCurrentAudio) adaptiveColor else MaterialTheme.colorScheme.onBackground
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        AnimatedVisibility(isCurrentAudio) {
            PlayBars(
                size = imageSize,
                containerColor = adaptiveColor,
                isPlaying = isPlaying,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = audio.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                color = contentColor,
                modifier = Modifier.basicMarquee(animationMode = MarqueeAnimationMode.WhileFocused)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(Specs.paddingTiny),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val artistAndDuration = listOf(
                    audio.album,
                    audio.durationMillis().millisToDuration(),
                ).interpunctize()
                Text(
                    text = artistAndDuration,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.7f),
                    maxLines = maxLines,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .alignByBaseline()
                        .basicMarquee(animationMode = MarqueeAnimationMode.WhileFocused)
                )
            }
        }
    }
}

private fun List<String?>.interpunctize(interpunct: String = " ꞏ ") = joinToString(interpunct)

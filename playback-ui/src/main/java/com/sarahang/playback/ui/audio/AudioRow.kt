package com.sarahang.playback.ui.audio

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.placeholder.material.placeholder
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.id
import com.sarahang.playback.core.millisToDuration
import com.sarahang.playback.core.models.Audio
import com.sarahang.playback.core.models.LocalPlaybackConnection
import com.sarahang.playback.ui.components.CoverImage
import com.sarahang.playback.ui.components.icons.Icons
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
    isPlaceholder: Boolean = false,
    onClick: ((Audio) -> Unit)? = null,
    onPlayAudio: ((Audio) -> Unit)? = null,
    actionHandler: AudioActionHandler = LocalAudioActionHandler.current
) {
    var menuVisible by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .combinedClickable(
                onClick = {
                    if (!isPlaceholder)
                        if (onClick != null) onClick(audio)
                        else if (onPlayAudio != null) onPlayAudio(audio)
                        else actionHandler(AudioItemAction.Play(audio))
                },
                onLongClick = {
                    menuVisible = true
                }
            )
            .fillMaxWidth()
            .padding(PaddingValues(horizontal = 16.dp, vertical = 8.dp))
    ) {
        AudioRowItem(
            audio = audio,
            isPlaceholder = isPlaceholder,
            imageSize = imageSize,
            modifier = Modifier
        )

        Box(modifier = Modifier.weight(1f))

        if (!isPlaceholder) {
            AudioDropdownMenu(
                expanded = menuVisible,
                onExpandedChange = { menuVisible = it },
                modifier = Modifier
                    .align(Alignment.CenterVertically),
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
}

@Composable
fun AudioRowItem(
    audio: Audio,
    modifier: Modifier = Modifier,
    imageSize: Dp = AudiosDefaults.imageSize,
    isPlaceholder: Boolean = false,
    maxLines: Int = AudiosDefaults.maxLines,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    val loadingModifier = Modifier.placeholder(visible = isPlaceholder)
    val nowPlayingAudio by playbackConnection.nowPlaying.collectAsStateWithLifecycle()
    val isCurrentAudio = nowPlayingAudio.id == audio.id
    val titleTextColor =
        if (isCurrentAudio) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground

    Row(
        horizontalArrangement = Arrangement.spacedBy(Specs.padding),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        CoverImage(
            data = if (isCurrentAudio) Icons.Play else audio.coverImage,
            size = imageSize,
            imageModifier = Modifier.then(loadingModifier),
        )

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = audio.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                color = titleTextColor,
                modifier = loadingModifier
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(Specs.paddingTiny),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val artistAndDuration = listOf(
                    audio.subtitle,
                    audio.duration.millisToDuration()
                ).interpunctize()
                Text(
                    text = artistAndDuration,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    maxLines = maxLines,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .alignByBaseline()
                        .then(loadingModifier)
                )
            }
        }
    }
}

private fun List<String?>.interpunctize(interpunct: String = " ꞏ ") = joinToString(interpunct)

package com.sarahang.playback.ui.player.mini

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.artwork
import com.sarahang.playback.core.artworkUri
import com.sarahang.playback.core.isActive
import com.sarahang.playback.core.isBuffering
import com.sarahang.playback.core.isError
import com.sarahang.playback.core.isPlayEnabled
import com.sarahang.playback.core.isPlaying
import com.sarahang.playback.core.models.Audio
import com.sarahang.playback.core.models.LocalPlaybackConnection
import com.sarahang.playback.core.models.toAudio
import com.sarahang.playback.core.playPause
import com.sarahang.playback.ui.R
import com.sarahang.playback.ui.audio.Dismissable
import com.sarahang.playback.ui.audio.materialYouAdaptiveColor
import com.sarahang.playback.ui.audio.nowPlayingArtworkAdaptiveColor
import com.sarahang.playback.ui.components.CoverImage
import com.sarahang.playback.ui.components.animatePlaybackProgress
import com.sarahang.playback.ui.components.icons.Icons
import com.sarahang.playback.ui.components.isWideScreen
import com.sarahang.playback.ui.sheet.PlayerNextControl
import com.sarahang.playback.ui.sheet.PlayerPlayControl
import com.sarahang.playback.ui.sheet.PlayerPreviousControl
import com.sarahang.playback.ui.theme.Specs
import com.sarahang.playback.ui.theme.orNa

object PlaybackMiniControlsDefaults {
    val Height = 56.dp
}

@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
    openPlaybackSheet: () -> Unit = {}
) {
    val playbackState by playbackConnection.playbackState.collectAsStateWithLifecycle()
    val nowPlaying by playbackConnection.nowPlaying.collectAsStateWithLifecycle()

    AnimatedVisibility(
        visible = (playbackState to nowPlaying).isActive,
        modifier = modifier,
        enter = slideInVertically(initialOffsetY = { it / 2 }),
        exit = slideOutVertically(targetOffsetY = { it / 2 })
    ) {
        PlaybackMiniControls(
            playbackState = playbackState,
            nowPlaying = nowPlaying,
            onPlayPause = { playbackConnection.mediaController?.playPause() },
            openPlaybackSheet = openPlaybackSheet,
            modifier = Modifier.testTag("mini_player")
        )
    }
}

@Composable
fun PlaybackMiniControls(
    playbackState: PlaybackStateCompat,
    nowPlaying: MediaMetadataCompat,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = PlaybackMiniControlsDefaults.Height,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
    openPlaybackSheet: () -> Unit
) {
    val adaptiveColor by nowPlayingArtworkAdaptiveColor()
    val backgroundColor = adaptiveColor.primary
    val contentColor = adaptiveColor.onPrimary

    BoxWithConstraints {
        val isWideLayout = isWideScreen()
        Dismissable(onDismiss = { playbackConnection.transportControls?.stop() }) {
            var dragOffset by remember { mutableFloatStateOf(0f) }
            Surface(
                color = Color.Transparent,
                shape = MaterialTheme.shapes.small,
                modifier = modifier
                    .animateContentSize()
                    .semantics(mergeDescendants = true) {}
                    .clickable(onClickLabel = stringResource(R.string.cd_open_player)) { openPlaybackSheet() }
                    // open playback sheet on swipe up
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState(
                            onDelta = {
                                dragOffset = it.coerceAtMost(0f)
                            }
                        ),
                        onDragStarted = {
                            if (dragOffset < 0) openPlaybackSheet()
                        },
                    )
            ) {
                Column {
                    var aspectRatio by remember { mutableFloatStateOf(0f) }
                    var controlsVisible by remember { mutableStateOf(true) }
                    var nowPlayingVisible by remember { mutableStateOf(true) }
                    var controlsEndPadding by remember { mutableStateOf(0.dp) }
                    val controlsEndPaddingAnimated by animateDpAsState(controlsEndPadding)
                    val smallPadding = 8.dp
                    val tinyPadding = 4.dp

                    CompositionLocalProvider(LocalContentColor provides contentColor) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(backgroundColor)
                                .onGloballyPositioned {
                                    aspectRatio = it.size.height.toFloat() / it.size.width.toFloat()
                                    controlsVisible = aspectRatio < 0.9
                                    nowPlayingVisible = aspectRatio < 0.5
                                    controlsEndPadding = when (aspectRatio) {
                                        in 0.0..0.15 -> 0.dp
                                        in 0.15..0.35 -> tinyPadding
                                        else -> smallPadding
                                    }
                                }
                                .padding(if (controlsVisible) PaddingValues(end = controlsEndPaddingAnimated) else PaddingValues())
                        ) {
                            PlaybackNowPlaying(
                                nowPlaying = nowPlaying,
                                maxHeight = height,
                                coverOnly = !nowPlayingVisible
                            )
                            if (controlsVisible && !isWideLayout)
                                PlaybackPlayPause(
                                    playbackState = playbackState,
                                    onPlayPause = onPlayPause
                                )
                        }

                        if (isWideLayout) {
                            WidePlayerControls(
                                playbackState = playbackState,
                                color = backgroundColor
                            )
                        }

                        PlaybackProgress(
                            playbackState = playbackState,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.PlaybackNowPlaying(
    nowPlaying: MediaMetadataCompat,
    maxHeight: Dp,
    modifier: Modifier = Modifier,
    coverOnly: Boolean = false,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.weight(if (coverOnly) 3f else 7f),
    ) {
        CoverImage(
            data = nowPlaying.artwork ?: nowPlaying.artworkUri,
            size = maxHeight - 12.dp,
            modifier = Modifier.padding(8.dp)
        )

        if (!coverOnly)
            PlaybackNowPlaying(nowPlaying.toAudio(), modifier = Modifier)
    }
}

@Composable
private fun PlaybackNowPlaying(audio: Audio, modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .padding(vertical = Specs.paddingSmall)
            .fillMaxWidth()
            .then(modifier)
    ) {
        Text(
            text = audio.title.orNa(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.basicMarquee()
        )
        Text(
            text = audio.subtitle.orNa(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.basicMarquee()
        )
    }
}

@Composable
internal fun RowScope.PlaybackPlayPause(
    playbackState: PlaybackStateCompat,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = Specs.iconSize,
) {
    IconButton(
        onClick = onPlayPause,
        modifier = modifier.weight(1f)
    ) {
        Icon(
            imageVector = when {
                playbackState.isError -> Icons.ErrorOutline
                playbackState.isPlaying -> Icons.Pause
                playbackState.isPlayEnabled -> Icons.PlayCircle
                else -> Icons.Hourglass
            },
            modifier = Modifier.size(size),
            contentDescription = when {
                playbackState.isError -> stringResource(R.string.cd_play_error)
                playbackState.isPlaying -> stringResource(R.string.cd_pause)
                playbackState.isPlayEnabled -> stringResource(R.string.cd_play)
                else -> stringResource(R.string.cd_play)
            }
        )
    }
}

@Composable
private fun WidePlayerControls(
    playbackState: PlaybackStateCompat,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.background,
    smallRippleRadius: Dp = 30.dp,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current
) {
    Row(
        modifier = modifier
            .background(color)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(Specs.paddingLarge))
        PlayerPreviousControl(
            playbackConnection = playbackConnection,
            smallRippleRadius = smallRippleRadius,
            modifier = Modifier
                .size(24.dp)
                .weight(1f),
            playbackState = playbackState
        )

        Spacer(Modifier.width(Specs.padding))
        PlayerPlayControl(playbackConnection, playbackState, Modifier.size(44.dp))
        Spacer(Modifier.width(Specs.padding))
        PlayerNextControl(
            playbackConnection = playbackConnection,
            smallRippleRadius = smallRippleRadius,
            modifier = Modifier
                .size(24.dp)
                .weight(1f),
            playbackState = playbackState
        )
        Spacer(Modifier.width(Specs.paddingLarge))
    }
}

@Composable
private fun PlaybackProgress(
    playbackState: PlaybackStateCompat,
    color: Color,
    modifier: Modifier = Modifier,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    val progressState by playbackConnection.playbackProgress.collectAsStateWithLifecycle()
    val sizeModifier = Modifier
        .height(2.dp)
        .fillMaxWidth()
    when {
        playbackState.isBuffering -> {
            LinearProgressIndicator(
                color = color,
                modifier = sizeModifier.then(modifier)
            )
        }

        else -> {
            val progress by animatePlaybackProgress(progressState.progress)
            LinearProgressIndicator(
                progress = progress,
                color = color,
                trackColor = color.copy(alpha = 0.24f),
                modifier = sizeModifier.then(modifier)
            )
        }
    }
}

@Preview
@Composable
fun PlaybackMiniControlsPreview() {
    MiniPlayer(Modifier.fillMaxWidth())
}

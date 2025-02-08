package com.sarahang.playback.ui.player.mini

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.isActive
import com.sarahang.playback.core.models.LocalPlaybackConnection
import com.sarahang.playback.core.models.MediaMetadata
import com.sarahang.playback.core.models.PlaybackState
import com.sarahang.playback.ui.audio.Dismissable
import com.sarahang.playback.ui.components.CoverImage
import com.sarahang.playback.ui.components.animatePlaybackProgress
import com.sarahang.playback.ui.components.icons.Icons
import com.sarahang.playback.ui.sheet.PlayerNextControl
import com.sarahang.playback.ui.sheet.PlayerPlayControl
import com.sarahang.playback.ui.sheet.PlayerPreviousControl
import com.sarahang.playback.ui.theme.Specs
import com.sarahang.playback.ui.theme.orNa
import kafka.ui_playback.generated.resources.Res
import kafka.ui_playback.generated.resources.cd_open_player
import kafka.ui_playback.generated.resources.cd_pause
import kafka.ui_playback.generated.resources.cd_play
import kafka.ui_playback.generated.resources.cd_play_error
import org.jetbrains.compose.resources.stringResource

object PlaybackMiniControlsDefaults {
    val Height = 56.dp
}

typealias MiniPlayer = @Composable ColumnScope.(Boolean, () -> Unit) -> Unit

@Composable
fun MiniPlayer(
    useDarkTheme: Boolean,
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
//        DynamicTheme(model = nowPlaying.coverImage, useDarkTheme = useDarkTheme) {
            PlaybackMiniControls(
                playbackState = playbackState,
                nowPlaying = nowPlaying,
                onPlayPause = { playbackConnection.playPause() },
                openPlaybackSheet = openPlaybackSheet,
                modifier = Modifier.testTag("mini_player"),
            )
//        }
    }
}

@Composable
private fun PlaybackMiniControls(
    playbackState: PlaybackState,
    nowPlaying: MediaMetadata,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = PlaybackMiniControlsDefaults.Height,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
    openPlaybackSheet: () -> Unit
) {
    val isWideLayout = currentWindowAdaptiveInfo()
        .windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED

    Dismissable(onDismiss = { playbackConnection.stop() }) {
        var dragOffset by remember { mutableFloatStateOf(0f) }
        Surface(
            color = Color.Transparent,
            shape = MaterialTheme.shapes.small,
            modifier = modifier
                .animateContentSize()
                .semantics(mergeDescendants = true) {}
                .clickable(onClickLabel = stringResource(Res.string.cd_open_player)) { openPlaybackSheet() }
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

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
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
                        color = MaterialTheme.colorScheme.primary
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

@Composable
private fun RowScope.PlaybackNowPlaying(
    nowPlaying: MediaMetadata,
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
            data = nowPlaying.coverImage,
            size = maxHeight - 12.dp,
            modifier = Modifier.padding(8.dp)
        )

        if (!coverOnly)
            PlaybackNowPlaying(nowPlaying, modifier = Modifier)
    }
}

@Composable
private fun PlaybackNowPlaying(audio: MediaMetadata, modifier: Modifier = Modifier) {
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
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.basicMarquee()
        )
        Text(
            text = audio.subtitle.orNa(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.basicMarquee()
        )
    }
}

@Composable
internal fun RowScope.PlaybackPlayPause(
    playbackState: PlaybackState,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = Specs.iconSize,
    color: Color = MaterialTheme.colorScheme.onPrimary
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
            tint = color,
            contentDescription = when {
                playbackState.isError -> stringResource(Res.string.cd_play_error)
                playbackState.isPlaying -> stringResource(Res.string.cd_pause)
                playbackState.isPlayEnabled -> stringResource(Res.string.cd_play)
                else -> stringResource(Res.string.cd_play)
            }
        )
    }
}

@Composable
private fun WidePlayerControls(
    playbackState: PlaybackState,
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
    playbackState: PlaybackState,
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
                progress = { progress },
                color = color,
                trackColor = color.copy(alpha = 0.24f),
                modifier = sizeModifier.then(modifier)
            )
        }
    }
}

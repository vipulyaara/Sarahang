package com.sarahang.playback.ui.sheet

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.models.LocalPlaybackConnection
import com.sarahang.playback.core.models.MediaMetadata
import com.sarahang.playback.core.models.PlaybackModeState
import com.sarahang.playback.core.models.PlaybackState
import com.sarahang.playback.core.models.REPEAT_MODE_ALL
import com.sarahang.playback.core.models.REPEAT_MODE_ONE
import com.sarahang.playback.core.models.SHUFFLE_MODE_ALL
import com.sarahang.playback.core.models.SHUFFLE_MODE_NONE
import com.sarahang.playback.ui.components.AnimatedVisibilityFade
import com.sarahang.playback.ui.components.IconButton
import com.sarahang.playback.ui.icons.ErrorOutline
import com.sarahang.playback.ui.icons.Repeat
import com.sarahang.playback.ui.icons.RepeatOn
import com.sarahang.playback.ui.icons.RepeatOneOn
import com.sarahang.playback.ui.icons.Shuffle
import com.sarahang.playback.ui.icons.ShuffleOn
import com.sarahang.playback.ui.playback.speed.PlaybackSpeed
import com.sarahang.playback.ui.playback.speed.PlaybackSpeedViewModel
import com.sarahang.playback.ui.playback.timer.SleepTimer
import com.sarahang.playback.ui.playback.timer.SleepTimerViewModel
import com.sarahang.playback.ui.playback.timer.widget.AnimatedClock
import com.sarahang.playback.ui.theme.Specs
import com.sarahang.playback.ui.theme.disabledAlpha
import com.sarahang.playback.ui.theme.orNa
import com.sarahang.playback.ui.theme.simpleClickable
import kafka.ui_playback.generated.resources.Res
import kafka.ui_playback.generated.resources.cd_change_playback_speed
import kafka.ui_playback.generated.resources.cd_fast_forward
import kafka.ui_playback.generated.resources.cd_next
import kafka.ui_playback.generated.resources.cd_open_sleep_timer
import kafka.ui_playback.generated.resources.cd_pause
import kafka.ui_playback.generated.resources.cd_play
import kafka.ui_playback.generated.resources.cd_play_error
import kafka.ui_playback.generated.resources.cd_previous
import kafka.ui_playback.generated.resources.cd_repeat_all_on
import kafka.ui_playback.generated.resources.cd_repeat_off
import kafka.ui_playback.generated.resources.cd_repeat_one_on
import kafka.ui_playback.generated.resources.cd_rewind
import kafka.ui_playback.generated.resources.cd_shuffle
import kafka.ui_playback.generated.resources.cd_sleep_timer
import org.jetbrains.compose.resources.stringResource
import com.sarahang.playback.ui.components.icons.Icons as PlayerIcons

object PlaybackNowPlayingDefaults {
    val titleTextStyle @Composable get() = MaterialTheme.typography.titleLarge
    val artistTextStyle @Composable get() = MaterialTheme.typography.titleSmall
}

@Composable
internal fun PlaybackNowPlayingWithControls(
    nowPlaying: MediaMetadata,
    playbackState: PlaybackState,
    onTitleClick: () -> Unit,
    onArtistClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleTextStyle: TextStyle = PlaybackNowPlayingDefaults.titleTextStyle,
    artistTextStyle: TextStyle = PlaybackNowPlayingDefaults.artistTextStyle,
    isMiniPlayer: Boolean = false,
    sleepTimerViewModelFactory: () -> SleepTimerViewModel,
    playbackSpeedViewModelFactory: () -> PlaybackSpeedViewModel,
) {
    val playbackConnection = LocalPlaybackConnection.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(if (isMiniPlayer) 8.dp else 32.dp)
    ) {
        if (!isMiniPlayer) {
            val playbackMode by playbackConnection.playbackMode.collectAsStateWithLifecycle()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
            ) {
                Text(
                    text = nowPlaying.title.orNa(),
                    style = titleTextStyle,
                    color = MaterialTheme.colorScheme.primary,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier
                        .simpleClickable(onClick = onTitleClick)
                        .basicMarquee()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = nowPlaying.artist.orNa(),
                    style = artistTextStyle,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.primary.disabledAlpha(condition = true),
                    modifier = Modifier
                        .simpleClickable(onClick = onArtistClick)
                        .basicMarquee()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RepeatButton(
                        playbackConnection = playbackConnection,
                        playbackMode = playbackMode
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlaybackSpeedButton(playbackSpeedViewModelFactory)
                        SleepTimerButton(sleepTimerViewModelFactory)
                    }
                }
            }
        }

        PlaybackProgress(playbackState = playbackState)

        PlaybackControls(
            playbackState = playbackState,
            compactControls = isMiniPlayer,
            modifier = Modifier.padding(top = if (isMiniPlayer) 4.dp else 12.dp)
        )
    }
}

@Composable
internal fun PlaybackControls(
    playbackState: PlaybackState,
    modifier: Modifier = Modifier,
    smallRippleRadius: Dp = SmallRippleRadius,
    compactControls: Boolean = false,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RewindControl(
            playbackConnection = playbackConnection,
            smallRippleRadius = smallRippleRadius,
            modifier = Modifier
                .size(20.dp)
                .weight(if (compactControls) 8f else 2f)
        )

        if (!compactControls) {
            Spacer(Modifier.width(Specs.paddingLarge))
            PlayerPreviousControl(
                playbackConnection = playbackConnection,
                smallRippleRadius = smallRippleRadius,
                modifier = Modifier
                    .size(40.dp)
                    .weight(4f),
                playbackState = playbackState
            )

        }

        Spacer(Modifier.width(Specs.padding))

        PlayerPlayControl(
            playbackConnection = playbackConnection,
            playbackState = playbackState,
            modifier = Modifier
                .size(80.dp)
                .weight(8f)
        )

        Spacer(Modifier.width(Specs.padding))

        if (!compactControls) {
            PlayerNextControl(
                playbackConnection = playbackConnection,
                smallRippleRadius = smallRippleRadius,
                playbackState = playbackState,
                modifier = Modifier
                    .size(40.dp)
                    .weight(4f)
            )

            Spacer(Modifier.width(Specs.paddingLarge))
        }

        FastForwardControl(
            playbackConnection = playbackConnection,
            smallRippleRadius = smallRippleRadius,
            modifier = Modifier
                .size(20.dp)
                .weight(if (compactControls) 8f else 2f)
        )
    }
}

@Composable
internal fun PlayerPreviousControl(
    playbackConnection: PlaybackConnection,
    smallRippleRadius: Dp,
    playbackState: PlaybackState,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = { playbackConnection.skipToPrevious() },
        rippleRadius = smallRippleRadius,
        modifier = modifier
    ) {
        Icon(
            painter = rememberVectorPainter(PlayerIcons.Previous),
            tint = MaterialTheme.colorScheme.primary.disabledAlpha(playbackState.hasPrevious),
            modifier = Modifier.fillMaxSize(),
            contentDescription = stringResource(Res.string.cd_previous)
        )
    }
}

@Composable
private fun RewindControl(
    playbackConnection: PlaybackConnection,
    smallRippleRadius: Dp,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = { playbackConnection.rewind() },
        rippleRadius = smallRippleRadius,
        modifier = modifier
    ) {
        Icon(
            painter = rememberVectorPainter(PlayerIcons.Rewind),
            modifier = Modifier.fillMaxSize(),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = stringResource(Res.string.cd_rewind)
        )
    }
}

@Composable
private fun FastForwardControl(
    playbackConnection: PlaybackConnection,
    smallRippleRadius: Dp,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = { playbackConnection.fastForward() },
        rippleRadius = smallRippleRadius,
        modifier = modifier
    ) {
        Icon(
            painter = rememberVectorPainter(PlayerIcons.FastForward),
            modifier = Modifier.fillMaxSize(),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = stringResource(Res.string.cd_fast_forward)
        )
    }
}

@Composable
internal fun PlayerNextControl(
    playbackConnection: PlaybackConnection,
    smallRippleRadius: Dp,
    playbackState: PlaybackState,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = { playbackConnection.skipToNext() },
        rippleRadius = smallRippleRadius,
        modifier = modifier
    ) {
        Icon(
            painter = rememberVectorPainter(PlayerIcons.Next),
            tint = MaterialTheme.colorScheme.primary.disabledAlpha(playbackState.hasNext),
            modifier = Modifier.fillMaxSize(),
            contentDescription = stringResource(Res.string.cd_next)
        )
    }
}

@Composable
internal fun PlayerPlayControl(
    playbackConnection: PlaybackConnection,
    playbackState: PlaybackState,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = { playbackConnection.playPause() },
        modifier = modifier,
        rippleRadius = 35.dp,
    ) {
        val painter = rememberVectorPainter(
            when {
                playbackState.isError -> Icons.Filled.ErrorOutline
                playbackState.isPlaying -> PlayerIcons.Pause
                playbackState.isPlayEnabled -> PlayerIcons.PlayCircle
                else -> PlayerIcons.PlayCircle
            }
        )
        Icon(
            painter = painter,
            modifier = Modifier.fillMaxSize(),
            tint = MaterialTheme.colorScheme.primary,
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
private fun SleepTimerButton(
    viewModelFactory: () -> SleepTimerViewModel,
    modifier: Modifier = Modifier
) {
    val timerViewModel = viewModel { viewModelFactory() }
    val timerViewState by timerViewModel.state.collectAsStateWithLifecycle()

    var showTimer by remember { mutableStateOf(false) }
    if (showTimer) {
        SleepTimer(viewModelFactory) { showTimer = false }
    }

    IconButton(
        onClick = { showTimer = true },
        onClickLabel = stringResource(Res.string.cd_open_sleep_timer),
        rippleRadius = SmallRippleRadius,
        modifier = modifier.size(24.dp)
    ) {
        AnimatedVisibilityFade(timerViewState.isTimerRunning) {
            AnimatedClock(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.5.dp),
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        }

        AnimatedVisibilityFade(!timerViewState.isTimerRunning) {
            Icon(
                painter = rememberVectorPainter(PlayerIcons.TimerOff),
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = stringResource(Res.string.cd_sleep_timer)
            )
        }
    }
}

@Composable
private fun PlaybackSpeedButton(
    viewModelFactory: () -> PlaybackSpeedViewModel,
    modifier: Modifier = Modifier
) {
    val viewModel = viewModel { viewModelFactory() }
    val currentSpeed by viewModel.currentSpeed.collectAsStateWithLifecycle()

    var showPlaybackSpeed by remember { mutableStateOf(false) }
    if (showPlaybackSpeed) {
        PlaybackSpeed(viewModelFactory) { showPlaybackSpeed = false }
    }

    Box(
        modifier.simpleClickable(
            indication = ripple(bounded = false),
            label = stringResource(Res.string.cd_change_playback_speed)
        ) { showPlaybackSpeed = true }
    ) {
        val speed = if ((currentSpeed * 10) % 10f == 0f) {
            currentSpeed.toInt().toString()
        } else {
            currentSpeed.toString()
        }

        Text(
            text = "${speed}x",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Composable
private fun RepeatButton(
    playbackConnection: PlaybackConnection,
    playbackMode: PlaybackModeState,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = { playbackConnection.toggleRepeatMode() },
        rippleRadius = SmallRippleRadius,
        modifier = modifier.size(24.dp)
    ) {
        Icon(
            painter = rememberVectorPainter(
                when (playbackMode.repeatMode) {
                    REPEAT_MODE_ONE -> Icons.Default.RepeatOneOn
                    REPEAT_MODE_ALL -> Icons.Default.RepeatOn
                    else -> Icons.Default.Repeat
                }
            ),
            modifier = Modifier.fillMaxSize(),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = when (playbackMode.repeatMode) {
                REPEAT_MODE_ONE -> stringResource(Res.string.cd_repeat_one_on)
                REPEAT_MODE_ALL -> stringResource(Res.string.cd_repeat_all_on)
                else -> stringResource(Res.string.cd_repeat_off)
            }
        )
    }
}

@Composable
private fun ShuffleButton(
    playbackConnection: PlaybackConnection,
    smallRippleRadius: Dp,
    playbackMode: PlaybackModeState,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = { playbackConnection.toggleShuffleMode() },
        modifier = modifier.size(24.dp),
        rippleRadius = smallRippleRadius,
    ) {
        Icon(
            painter = rememberVectorPainter(
                when (playbackMode.shuffleMode) {
                    SHUFFLE_MODE_NONE -> Icons.Default.Shuffle
                    SHUFFLE_MODE_ALL -> Icons.Default.ShuffleOn
                    else -> Icons.Default.Shuffle
                }
            ),
            tint = contentColor,
            modifier = Modifier.fillMaxSize(),
            contentDescription = stringResource(Res.string.cd_shuffle)
        )
    }
}

private val SmallRippleRadius = 30.dp

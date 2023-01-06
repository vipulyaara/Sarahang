package com.sarahang.playback.ui.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.sarahang.playback.core.models.LocalPlaybackConnection

@Composable
fun PlaybackHost(
    viewModel: PlaybackViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {

    CompositionLocalProvider(
        LocalPlaybackConnection provides viewModel.playbackConnection,
    ) {
        content()
    }
}

@Composable
fun AudioActionHost(
    audioActionHandler: AudioActionHandler = audioActionHandler(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAudioActionHandler provides audioActionHandler
    ) {
        content()
    }
}

package com.sarahang.playback.ui.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.SET_MEDIA_STATE
import com.sarahang.playback.core.models.LocalPlaybackConnection

@Composable
fun PlaybackHost(playbackConnection: PlaybackConnection, content: @Composable () -> Unit) {
    val isConnected by playbackConnection.isConnected.collectAsStateWithLifecycle()
    LaunchedEffect(isConnected) {
        if (isConnected) {
            playbackConnection.sendCustomAction(SET_MEDIA_STATE, null)
        }
    }

    CompositionLocalProvider(LocalPlaybackConnection provides playbackConnection) {
        content()
    }
}

@Composable
fun AudioActionHost(
    showMessage: (String) -> Unit,
    audioActionHandler: AudioActionHandler = audioActionHandler(showMessage = showMessage),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAudioActionHandler provides audioActionHandler
    ) {
        content()
    }
}

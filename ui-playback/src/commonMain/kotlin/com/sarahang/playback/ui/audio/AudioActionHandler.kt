package com.sarahang.playback.ui.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.models.LocalPlaybackConnection
import com.sarahang.playback.ui.AudioItemAction

val LocalAudioActionHandler = staticCompositionLocalOf<AudioActionHandler> {
    error("No LocalAudioActionHandler provided")
}

typealias AudioActionHandler = (AudioItemAction) -> Unit

@Composable
fun audioActionHandler(
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
    clipboardManager: ClipboardManager = LocalClipboardManager.current,
    showMessage: (String) -> Unit
): AudioActionHandler {
    return { action ->
        when (action) {
            is AudioItemAction.Play -> playbackConnection.playAudio(action.audio)
            is AudioItemAction.PlayNext -> playbackConnection.playNextAudio(action.audio)
            is AudioItemAction.CopyLink -> {
                clipboardManager.setText(AnnotatedString(action.audio.coverImage.orEmpty()))
                showMessage("Copied to clipboard")
            }

            else -> {}
        }
    }
}

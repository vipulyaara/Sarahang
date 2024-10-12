package com.sarahang.playback.core.models

import androidx.compose.runtime.staticCompositionLocalOf
import com.sarahang.playback.core.PlaybackConnection

val LocalPlaybackConnection = staticCompositionLocalOf<PlaybackConnection> {
    error("No LocalPlaybackConnection provided")
}

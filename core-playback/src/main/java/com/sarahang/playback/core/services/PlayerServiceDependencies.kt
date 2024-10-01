package com.sarahang.playback.core.services

import com.sarahang.playback.core.MediaNotifications
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.apis.AudioDataSource
import com.sarahang.playback.core.apis.Logger
import com.sarahang.playback.core.players.SarahangPlayer
import com.sarahang.playback.core.timer.SleepTimer

// todo: I couldn't find a way to provide these dependencies through kotlin-inject
// these dependencies can be provided through a custom component but then they do not maintain same instance.
// each component creates a new instance even if appScope is applied
// The only solution for now is to provide these through application class by implementing this interface
interface PlayerServiceDependencies {
    val player: SarahangPlayer
    val timer: SleepTimer
    val logger: Logger
    val mediaNotifications: MediaNotifications
    val audioDataSource: AudioDataSource
    val playbackConnection: PlaybackConnection
}

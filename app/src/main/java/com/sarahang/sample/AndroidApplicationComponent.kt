package com.sarahang.sample

import android.app.Application
import android.content.Context
import com.sarahang.playback.core.MediaNotifications
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.apis.AudioDataSource
import com.sarahang.playback.core.apis.Logger
import com.sarahang.playback.core.players.SarahangPlayer
import com.sarahang.playback.core.timer.SleepTimer
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import org.kafka.base.ApplicationScope

@Component
@ApplicationScope
abstract class AndroidApplicationComponent(
    @get:Provides val application: Application,
) : PlaybackModule {
    abstract val player: SarahangPlayer
    abstract val timer: SleepTimer
    abstract val logger: Logger
    abstract val mediaNotifications: MediaNotifications
    abstract val audioDataSource: AudioDataSource
    abstract val playbackConnection: PlaybackConnection

    companion object
}

internal fun AndroidApplicationComponent.Companion.from(context: Context): AndroidApplicationComponent {
    return (context.applicationContext as SarahangApplication).component
}

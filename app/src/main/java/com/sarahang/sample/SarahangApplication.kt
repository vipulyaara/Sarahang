package com.sarahang.sample

import android.app.Application
import com.sarahang.playback.core.MediaNotifications
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.apis.AudioDataSource
import com.sarahang.playback.core.apis.Logger
import com.sarahang.playback.core.players.SarahangPlayer
import com.sarahang.playback.core.services.PlayerServiceDependencies
import com.sarahang.playback.core.timer.SleepTimer
import timber.log.Timber

class SarahangApplication : Application(), PlayerServiceDependencies {
    internal val component: AndroidApplicationComponent by lazy(LazyThreadSafetyMode.NONE) {
        AndroidApplicationComponent::class.create(this)
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

    override val player: SarahangPlayer
        get() = component.player
    override val timer: SleepTimer
        get() = component.timer
    override val logger: Logger
        get() = component.logger
    override val mediaNotifications: MediaNotifications
        get() = component.mediaNotifications
    override val audioDataSource: AudioDataSource
        get() = component.audioDataSource
    override val playbackConnection: PlaybackConnection
        get() = component.playbackConnection
}

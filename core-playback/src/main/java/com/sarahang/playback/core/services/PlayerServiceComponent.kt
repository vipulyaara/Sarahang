package com.sarahang.playback.core.services

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.kafka.data.injection.DatabaseModule
import com.sarahang.playback.core.MediaNotificationsImpl
import com.sarahang.playback.core.apis.AudioDataSource
import com.sarahang.playback.core.apis.Logger
import com.sarahang.playback.core.audio.AudioFocusHelper
import com.sarahang.playback.core.injection.PlayerModule
import com.sarahang.playback.core.players.SarahangPlayer
import com.sarahang.playback.core.timer.SleepTimer
import kotlinx.coroutines.CoroutineScope
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import org.kafka.analytics.AnalyticsPlatformComponent
import org.kafka.base.ProcessLifetime

@Component
abstract class PlayerServiceComponent(
    @get:Provides val application: Application,
): PlayerModule, DatabaseModule, AnalyticsPlatformComponent {
    @Provides
    @ProcessLifetime
    fun provideLongLifetimeScope(): CoroutineScope {
        return ProcessLifecycleOwner.get().lifecycleScope
    }

    abstract val audioFocusHelper: AudioFocusHelper
    abstract val player: SarahangPlayer
    abstract val timer: SleepTimer
    abstract val logger: Logger
    abstract val mediaNotifications: MediaNotificationsImpl
    abstract val audioDataSource: AudioDataSource

    companion object
}

package com.sarahang.playback.core.injection

import com.kafka.base.ApplicationScope
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.PreferencesStore
import com.sarahang.playback.core.createDataStore
import com.sarahang.playback.core.dataStoreFileName
import com.sarahang.playback.core.players.SarahangPlayer
import com.sarahang.playback.core.timer.SleepTimer
import com.sarahang.playback.core.timer.SleepTimerImpl
import me.tatarka.inject.annotations.Provides

@ApplicationScope
actual interface PlaybackCoreComponent {

    @Provides
    @ApplicationScope
    fun playbackConnection(): PlaybackConnection = FakePlaybackConnection()

    @Provides
    @ApplicationScope
    fun provideSarahangPlayer(): SarahangPlayer = FakeSarahangPlayer()

    @Provides
    @ApplicationScope
    fun provideSleepTimer(bind: SleepTimerImpl): SleepTimer = bind

    @ApplicationScope
    @Provides
    fun providePlayerPreferencesStore(): PreferencesStore {
        return PreferencesStore(createDataStore { dataStoreFileName })
    }
}

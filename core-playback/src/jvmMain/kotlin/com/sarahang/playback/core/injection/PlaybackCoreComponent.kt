package com.sarahang.playback.core.injection

import com.kafka.base.ApplicationScope
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.players.SarahangPlayer
import me.tatarka.inject.annotations.Provides

@ApplicationScope
actual interface PlaybackCoreComponent {

    @Provides
    @ApplicationScope
    fun playbackConnection(): PlaybackConnection = FakePlaybackConnection()

    @Provides
    @ApplicationScope
    fun provideSarahangPlayer(): SarahangPlayer = FakeSarahangPlayer()

}

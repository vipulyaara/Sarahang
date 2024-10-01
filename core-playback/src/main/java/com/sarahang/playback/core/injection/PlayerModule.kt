package com.sarahang.playback.core.injection

import com.sarahang.playback.core.apis.AudioDataSource
import com.sarahang.playback.core.apis.Logger
import com.sarahang.playback.core.apis.PlayerEventLogger
import com.sarahang.playback.core.apis.impl.KafkaPlayerEventLogger
import com.sarahang.playback.core.apis.impl.PlayerAudioDataSource
import com.sarahang.playback.core.apis.impl.PlayerLogger
import me.tatarka.inject.annotations.Provides
import org.kafka.base.ApplicationScope

@ApplicationScope
interface PlayerModule : PlaybackCoreModule {

    @Provides
    @ApplicationScope
    fun audioDataSource(bind: PlayerAudioDataSource): AudioDataSource = bind

    @Provides
    @ApplicationScope
    fun playerEventLogger(bind: KafkaPlayerEventLogger): PlayerEventLogger = bind

    @Provides
    @ApplicationScope
    fun playerLogger(bind: PlayerLogger): Logger = bind
}

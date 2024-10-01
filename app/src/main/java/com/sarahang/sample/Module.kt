package com.sarahang.sample

import com.sarahang.playback.core.apis.AudioDataSource
import com.sarahang.playback.core.apis.Logger
import com.sarahang.playback.core.apis.PlayerEventLogger
import com.sarahang.playback.core.injection.PlaybackCoreModule
import com.sarahang.playback.core.models.Audio
import com.sarahang.sample.FakeData.audios
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import org.kafka.base.ApplicationScope

@Component
@ApplicationScope
interface PlaybackModule : PlaybackCoreModule {

    @Provides
    @ApplicationScope
    fun audioDataSource(): AudioDataSource = object : AudioDataSource {
        override suspend fun getByIds(ids: List<String>): List<Audio> {
            return audios
        }

        override suspend fun findAudio(id: String): Audio? {
            return audios.firstOrNull { it.id == id }
        }

        override suspend fun findAudioList(id: String): List<Audio> {
            return audios
        }

        override suspend fun findAudiosByItemId(itemId: String): List<Audio> {
            return audios
        }
    }

    @Provides
    @ApplicationScope
    fun audioLogger(): PlayerEventLogger = object : PlayerEventLogger {

    }

    @Provides
    @ApplicationScope
    fun playerLogger(): Logger = object : Logger {
        override fun i(message: String) {
            println("INFO: $message")
        }

        override fun d(message: String) {
            println("DEBUG: $message")
        }

        override fun w(message: String) {
            println("WARN: $message")
        }

        override fun e(message: String) {
            println("ERROR: $message")
        }

        override fun e(throwable: Throwable, message: String) {
            println("ERROR: $message")
            throwable.printStackTrace()
        }
    }
}

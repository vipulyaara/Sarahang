package com.sarahang.sample

import com.sarahang.playback.core.apis.AudioDataSource
import com.sarahang.playback.core.apis.PlayerEventLogger
import com.sarahang.playback.core.models.Audio
import com.sarahang.sample.FakeData.audios
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class PlaybackModule {

    @Provides
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
    fun audioLogger(): PlayerEventLogger = object : PlayerEventLogger {

    }
}

package com.sarahang.playback.core.apis

import com.sarahang.playback.core.models.Audio

interface AudioDataSource {
    suspend fun getByIds(ids: List<String>): List<Audio>
    suspend fun findAudio(id: String): Audio?
    suspend fun findAudioList(id: String): List<Audio>
    suspend fun findAudiosByItemId(itemId: String): List<Audio>
}

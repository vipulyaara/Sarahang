/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package com.sarahang.playback.core

import com.sarahang.playback.core.apis.AudioDataSource
import com.sarahang.playback.core.models.*
import javax.inject.Inject

class MediaQueueBuilder @Inject constructor(
    private val audioDataSource: AudioDataSource
) {
    suspend fun buildAudioList(source: MediaId): List<Audio> = with(source) {
        when (type) {
            MEDIA_TYPE_AUDIO -> listOfNotNull(audioDataSource.findAudio(value))
            MEDIA_TYPE_ALBUM -> audioDataSource.findAudiosByItemId(value)
            else -> listOf(Audio.unknown)
        }
    }

    suspend fun buildQueueTitle(source: MediaId): QueueTitle = with(source) {
        when (type) {
            MEDIA_TYPE_AUDIO -> QueueTitle(QueueTitle.Type.AUDIO, audioDataSource.findAudio(value)?.title)
            MEDIA_TYPE_ALBUM -> QueueTitle(QueueTitle.Type.ALBUM, audioDataSource.findAudiosByItemId(value).firstOrNull()?.album)
            else -> QueueTitle()
        }
    }
}

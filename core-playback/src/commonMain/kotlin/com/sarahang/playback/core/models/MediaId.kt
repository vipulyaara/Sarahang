package com.sarahang.playback.core.models

import co.touchlab.kermit.Logger
import com.sarahang.playback.core.apis.AudioDataSource

const val MEDIA_TYPE_AUDIO = "Media.Audio"
const val MEDIA_TYPE_ALBUM = "Media.Album"
const val MEDIA_TYPE_AUDIO_QUERY = "Media.AudioQuery"

private const val MEDIA_ID_SEPARATOR = " | "
const val MEDIA_ID_INDEX_SHUFFLED = -1000

data class MediaId(
    val type: String = MEDIA_TYPE_AUDIO,
    val value: String = "0",
    val index: Int = 0,
    val caller: String = CALLER_SELF,
) {
    val hasIndex = index >= 0
    val isShuffleIndex = index == MEDIA_ID_INDEX_SHUFFLED

    companion object {
        const val CALLER_SELF = "self"
        const val CALLER_OTHER = "other"
    }

    override fun toString(): String {
        return type +
                MEDIA_ID_SEPARATOR + value +
                MEDIA_ID_SEPARATOR + index +
                MEDIA_ID_SEPARATOR + caller
    }
}

fun String?.toMediaId(): MediaId {
    if (this == null)
        return MediaId()

    val parts = split(MEDIA_ID_SEPARATOR)
    val type = parts[0]

    val knownTypes = listOf(MEDIA_TYPE_AUDIO, MEDIA_TYPE_AUDIO_QUERY, MEDIA_TYPE_ALBUM)
    if (type !in knownTypes) {
        Logger.e(messageString = "Unknown media type: $type", throwable = null, tag = "MediaId")
        return MediaId()
    }

    return if (parts.size > 1)
        MediaId(type, parts[1], parts[2].toInt(), parts[3])
    else MediaId()
}

suspend fun MediaId.toAudioList(
    audioDataSource: AudioDataSource,
): List<Audio> = when (type) {
    MEDIA_TYPE_AUDIO -> listOfNotNull(audioDataSource.findAudio(value))
    MEDIA_TYPE_AUDIO_QUERY -> audioDataSource.findAudioList(value)
    MEDIA_TYPE_ALBUM -> audioDataSource.findAudiosByItemId(value)

    else -> emptyList()
}

suspend fun MediaId.toQueueTitle(
    audioDataSource: AudioDataSource,
): QueueTitle = when (type) {
    MEDIA_TYPE_AUDIO -> QueueTitle(QueueTitle.Type.AUDIO, audioDataSource.findAudio(value)?.title)

    else -> QueueTitle()
}



package com.sarahang.playback.core.models

import com.sarahang.playback.core.apis.AudioDataSource
import timber.log.Timber

const val MEDIA_TYPE_AUDIO = "Media.Audio"
const val MEDIA_TYPE_ARTIST = "Media.Artist"
const val MEDIA_TYPE_ALBUM = "Media.Album"
const val MEDIA_TYPE_AUDIO_QUERY = "Media.AudioQuery"
const val MEDIA_TYPE_AUDIO_MINERVA_QUERY = "Media.AudioMinervaQuery"
const val MEDIA_TYPE_AUDIO_FLACS_QUERY = "Media.AudioFlacsQuery"

private const val MEDIA_ID_SEPARATOR = " | "

data class MediaId(
    val type: String = MEDIA_TYPE_AUDIO,
    val value: String = "0",
    val index: Int = 0,
    val caller: String = CALLER_SELF
) {

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

fun String.toMediaId(): MediaId {
    val parts = split(MEDIA_ID_SEPARATOR)
    val type = parts[0]

    val knownTypes = listOf(
        MEDIA_TYPE_AUDIO, MEDIA_TYPE_ARTIST,
        MEDIA_TYPE_ALBUM, MEDIA_TYPE_AUDIO_QUERY,
        MEDIA_TYPE_AUDIO_MINERVA_QUERY, MEDIA_TYPE_AUDIO_FLACS_QUERY
    )
    if (type !in knownTypes) {
        Timber.e("Unknown media type: $type")
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
    MEDIA_TYPE_AUDIO_QUERY, MEDIA_TYPE_AUDIO_MINERVA_QUERY, MEDIA_TYPE_AUDIO_FLACS_QUERY -> {
        audioDataSource.findAudioList(value)
    }

    else -> emptyList()
}

suspend fun MediaId.toQueueTitle(
    audioDataSource: AudioDataSource,
): QueueTitle = when (type) {
    MEDIA_TYPE_AUDIO -> QueueTitle(QueueTitle.Type.AUDIO, audioDataSource.findAudio(value)?.title)
    MEDIA_TYPE_AUDIO_QUERY, MEDIA_TYPE_AUDIO_MINERVA_QUERY, MEDIA_TYPE_AUDIO_FLACS_QUERY -> {
        QueueTitle(QueueTitle.Type.SEARCH, value)
    }

    else -> QueueTitle()
}

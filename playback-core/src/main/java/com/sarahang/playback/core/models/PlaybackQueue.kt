package com.sarahang.playback.core.models

import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.session.MediaControllerCompat
import com.sarahang.playback.core.currentIndex

data class PlaybackQueue(
    val ids: List<String> = emptyList(),
    val audios: List<Audio> = emptyList(),
    val title: String? = null,
    val initialMediaId: String = "",
    val currentIndex: Int = 0,
) : List<Audio> by audios {
    val isLastAudio = currentIndex == lastIndex
    val isValid = ids.isNotEmpty() && audios.isNotEmpty() && currentIndex >= 0
    val currentAudio get() = get(currentIndex)
}

fun fromMediaController(mediaController: MediaControllerCompat) = PlaybackQueue(
    title = mediaController.queueTitle?.toString(),
    ids = mediaController.queue.mapNotNull { it.description.mediaId },
    initialMediaId = mediaController.metadata?.getString(METADATA_KEY_MEDIA_ID) ?: "",
    currentIndex = mediaController.playbackState.currentIndex
)

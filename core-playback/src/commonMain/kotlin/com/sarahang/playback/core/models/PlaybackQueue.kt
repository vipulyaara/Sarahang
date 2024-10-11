package com.sarahang.playback.core.models

data class PlaybackQueue(
    val ids: List<String> = emptyList(),
    val audios: List<Audio> = emptyList(),
    val title: String? = null,
    val initialMediaId: String = "",
    val currentIndex: Int = 0,
    val isIndexValid: Boolean = true
) : List<Audio> by audios {
    val isValid = ids.isNotEmpty() && audios.isNotEmpty() && currentIndex >= 0
    val currentAudio get() = get(currentIndex)

    data class NowPlayingAudio(val audio: Audio, val index: Int) {
        companion object {

            fun from(queue: PlaybackQueue) = NowPlayingAudio(queue.currentAudio, queue.currentIndex)

            fun NowPlayingAudio?.isCurrentAudio(audio: Audio, audioIndex: Int? = null) =
                (this?.audio?.id == audio.id && (audioIndex == null || audioIndex == this.index))
        }
    }
}

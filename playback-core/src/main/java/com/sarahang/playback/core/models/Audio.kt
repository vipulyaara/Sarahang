package com.sarahang.playback.core.models

data class Audio(
    val id: String,
    val title: String,
    val playbackUrl: String,
    val duration: Long,
    val artist: String? = null,
    val album: String? = null,
    val coverImage: String? = null,
) {
    val subtitle: String
        get() = listOfNotNull(artist, album).filter { it.isNotEmpty() }.joinToString(" - ")

    companion object {
        val unknown = Audio(
            id = "unknown",
            title = "unknown",
            playbackUrl = "unknown",
            duration = 0,
            artist = "unknown",
            album = "unknown",
            coverImage = "unknown"
        )
    }
}

const val UNKNOWN_ARTIST = "Unknown Artist"
const val UNTITLED_SONG = "Untitled Song"
const val UNTITLED_ALBUM = "Untitled"

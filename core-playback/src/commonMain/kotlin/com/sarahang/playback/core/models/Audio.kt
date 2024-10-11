package com.sarahang.playback.core.models

data class Audio(
    val id: String,
    val title: String,
    val playbackUrl: String,
    val localUri: String?,
    val duration: Long,
    val albumId: String,
    val artist: String? = null,
    val album: String? = null,
    val coverImage: String? = null,
) {
    val subtitle: String
        get() = listOfNotNull(album, artist).filter { it.isNotEmpty() }
            .joinToString(" • ")

    fun durationMillis() = duration * 1000

    companion object {
        val unknown = Audio(
            id = "unknown",
            title = "unknown",
            playbackUrl = "unknown",
            localUri = "unknown",
            duration = 0,
            artist = "unknown",
            album = "unknown",
            coverImage = "unknown",
            albumId = "unknown"
        )
    }
}

const val UNTITLED_SONG = "Untitled Song"

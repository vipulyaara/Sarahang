package com.sarahang.playback.core.models

import com.sarahang.playback.core.millisToDuration

data class Audio(
    val id: String,
    val title: String,
    val playbackUrl: String,
    val duration: Long,
    val albumId: String,
    val artist: String? = null,
    val album: String? = null,
    val coverImage: String? = null,
) {
    val subtitle: String
        get() = listOfNotNull(album, artist).filter { it.isNotEmpty() }
            .joinToString(" • ")

    val audioRowSubtitle: String
        get() = listOfNotNull(truncatedAlbum, durationMillis().millisToDuration())
            .filter { it.isNotEmpty() }.joinToString(" • ")

    val truncatedAlbum: String
        get() = album?.substring(0, album.length.coerceAtMost(30)) +
                if (album != null && album.length > 30) "…" else ""

    fun durationMillis() = duration * 1000

    companion object {
        val unknown = Audio(
            id = "unknown",
            title = "unknown",
            playbackUrl = "unknown",
            duration = 0,
            artist = "unknown",
            album = "unknown",
            coverImage = "unknown",
            albumId = "unknown"
        )
    }
}

const val UNTITLED_SONG = "Untitled Song"

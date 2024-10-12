package com.sarahang.playback.core.models

interface MediaMetadata {
    val id: String?
    val albumId: String?
    val fileId: String
    val title: String?
    val artist: String?
    val album: String?
    val coverImage: String?

    val subtitle: String
        get() = listOfNotNull(album, artist).filter { it.isNotEmpty() }
            .joinToString(" • ")
}

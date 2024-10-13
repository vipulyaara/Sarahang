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

    companion object {
        val NONE_PLAYING = object : MediaMetadata {
            override val id: String? = null
            override val albumId: String? = null
            override val fileId: String = ""
            override val title: String? = null
            override val artist: String? = null
            override val album: String? = null
            override val coverImage: String = ""
        }
    }
}

package com.sarahang.playback.core.playback

import android.support.v4.media.MediaMetadataCompat
import com.sarahang.playback.core.album
import com.sarahang.playback.core.albumId
import com.sarahang.playback.core.artist
import com.sarahang.playback.core.artworkUri
import com.sarahang.playback.core.fileId
import com.sarahang.playback.core.id
import com.sarahang.playback.core.models.MediaMetadata
import com.sarahang.playback.core.title

fun MediaMetadataCompat.asMediaMetadata() = object : MediaMetadata {
    override val id: String? = this@asMediaMetadata.id
    override val albumId: String? = this@asMediaMetadata.albumId
    override val fileId: String = this@asMediaMetadata.fileId
    override val title: String? = this@asMediaMetadata.title
    override val artist: String? = this@asMediaMetadata.artist
    override val album: String? = this@asMediaMetadata.album
    override val coverImage: String = this@asMediaMetadata.artworkUri.toString()
}

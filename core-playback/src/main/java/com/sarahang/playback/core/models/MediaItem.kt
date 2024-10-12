package com.sarahang.playback.core.models

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.net.toUri

fun List<MediaSessionCompat.QueueItem>?.toMediaIdList(): List<MediaId> {
    return this?.map { it.description.mediaId?.toMediaId() ?: MediaId() } ?: emptyList()
}

fun List<String>.toMediaAudioIds(): List<String> {
    return this.map { it.toMediaId().value }
}

fun Audio.toMediaDescription(): MediaDescriptionCompat {
    return MediaDescriptionCompat.Builder()
        .setTitle(title)
        .setMediaId(MediaId(MEDIA_TYPE_AUDIO, id).toString())
        .setSubtitle(subtitle)
        .setDescription(subtitle)
        .setIconUri(coverImage?.toUri()).build()
}

fun Audio.toMediaItem(): MediaBrowserCompat.MediaItem {
    return MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
            .setMediaId(MediaId(MEDIA_TYPE_AUDIO, id).toString())
            .setTitle(title)
            .setIconUri(coverImage?.toUri())
            .setSubtitle(subtitle)
            .build(),
        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
    )
}

fun List<Audio>?.toMediaItems() = this?.map { it.toMediaItem() } ?: emptyList()

fun List<Audio?>.toQueueItems(): List<MediaSessionCompat.QueueItem> {
    return filterNotNull().mapIndexed { index, audio ->
        MediaSessionCompat.QueueItem(
            audio.toMediaDescription(),
            (audio.id + index).hashCode().toLong()
        )
    }
}

fun Audio.toMediaMetadata(builder: MediaMetadataCompat.Builder): MediaMetadataCompat.Builder =
    builder.apply {
        putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
        putString(METADATA_KEY_ALBUM_ID, albumId)
        putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
        putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
        putString(
            MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
            MediaId(MEDIA_TYPE_AUDIO, id).toString()
        )
        putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMillis())
        putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, coverImage)
        putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, null)
    }

const val METADATA_KEY_ALBUM_ID = "album_id"

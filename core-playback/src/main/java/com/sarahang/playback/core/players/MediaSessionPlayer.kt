package com.sarahang.playback.core.players

import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

interface MediaSessionPlayer {
    fun getSession(): MediaSessionCompat
    fun setPlaybackState(state: PlaybackStateCompat)
}

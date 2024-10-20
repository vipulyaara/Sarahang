package com.sarahang.playback.core

import com.sarahang.playback.core.models.MediaMetadata
import com.sarahang.playback.core.models.PlaybackState

inline val Pair<PlaybackState, MediaMetadata>.isActive
    get() = (first.state != PlaybackState.STATE_NONE && second != MediaMetadata.NONE_PLAYING)

//todo: test
val NONE_PLAYING_STATE: PlaybackState = object : PlaybackState {
    override val state: Int = PlaybackState.STATE_NONE
    override val isPlaying: Boolean = false
    override val isIdle: Boolean = true
    override val position: Long = 0
    override val currentIndex: Int = 0
    override val isBuffering: Boolean = false
    override val isError: Boolean = false
    override val isPlayEnabled: Boolean = false
    override val hasNext: Boolean = false
    override val hasPrevious: Boolean = false
}

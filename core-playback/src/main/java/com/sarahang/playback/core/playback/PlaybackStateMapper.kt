package com.sarahang.playback.core.playback

import android.support.v4.media.session.PlaybackStateCompat
import com.sarahang.playback.core.currentIndex
import com.sarahang.playback.core.hasNext
import com.sarahang.playback.core.hasPrevious
import com.sarahang.playback.core.isBuffering
import com.sarahang.playback.core.isError
import com.sarahang.playback.core.isIdle
import com.sarahang.playback.core.isPlayEnabled
import com.sarahang.playback.core.isPlaying
import com.sarahang.playback.core.models.PlaybackState

fun PlaybackStateCompat.asPlaybackState() = object : PlaybackState {
    override val state: Int = this@asPlaybackState.state
    override val isPlaying: Boolean = this@asPlaybackState.isPlaying
    override val isIdle: Boolean = this@asPlaybackState.isIdle
    override val position: Long = this@asPlaybackState.position
    override val currentIndex: Int = this@asPlaybackState.currentIndex
    override val isBuffering: Boolean = this@asPlaybackState.isBuffering
    override val isError: Boolean = this@asPlaybackState.isError
    override val isPlayEnabled: Boolean = this@asPlaybackState.isPlayEnabled
    override val hasNext: Boolean = this@asPlaybackState.hasNext
    override val hasPrevious: Boolean = this@asPlaybackState.hasPrevious
}

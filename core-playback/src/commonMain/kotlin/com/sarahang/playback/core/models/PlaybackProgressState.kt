package com.sarahang.playback.core.models

import com.sarahang.playback.core.millisToDuration

data class PlaybackProgressState(
    val total: Long = 0L,
    val position: Long = 0L,
    val elapsed: Long = 0L,
    val buffered: Long = 0L,
    val isPlaying: Boolean = false,
) {
    val progress get() = ((position.toFloat() + elapsed) / (total + 1).toFloat()).coerceIn(0f, 1f)
    val bufferedProgress get() = ((buffered.toFloat()) / (total + 1).toFloat()).coerceIn(0f, 1f)
    val currentDuration get() = (position + elapsed).millisToDuration()
    val totalDuration get() = total.millisToDuration()
}

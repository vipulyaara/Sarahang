package com.sarahang.playback.core.models

data class PlaybackModeState(
    val shuffleMode: Int = SHUFFLE_MODE_INVALID,
    val repeatMode: Int = REPEAT_MODE_INVALID,
)

val SHUFFLE_MODE_INVALID: Int = -1
val SHUFFLE_MODE_NONE: Int = 0
val SHUFFLE_MODE_ALL: Int = 1
val SHUFFLE_MODE_GROUP: Int = 2

val REPEAT_MODE_INVALID: Int = -1
val REPEAT_MODE_NONE: Int = 0
val REPEAT_MODE_ONE: Int = 1
val REPEAT_MODE_ALL: Int = 2
val REPEAT_MODE_GROUP: Int = 3

package com.sarahang.playback.core.models

interface PlaybackState {
    val state: Int
    val isPlaying: Boolean
    val isIdle: Boolean
    val position: Long
    val currentIndex: Int
    val isBuffering: Boolean
    val isError: Boolean
    val isPlayEnabled: Boolean
    val hasNext: Boolean
    val hasPrevious: Boolean
}

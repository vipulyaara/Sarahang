package com.sarahang.playback.core.models

import kotlinx.serialization.Serializable

@Serializable
data class QueueState(
    val queue: List<String>,
    val currentIndex: Int = 0,
    val title: String? = null,
    val repeatMode: Int = 0,
    val shuffleMode: Int = 0,
    val seekPosition: Long = 0,
    val state: Int = 0
)

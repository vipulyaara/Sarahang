package com.sarahang.playback.ui.playback.speed

import androidx.lifecycle.ViewModel
import com.sarahang.playback.core.apis.PlayerEventLogger
import com.sarahang.playback.core.players.SarahangPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class PlaybackSpeedViewModel @Inject constructor(
    private val sarahangPlayer: SarahangPlayer,
    private val playerEventLogger: PlayerEventLogger,
) : ViewModel() {
    val currentSpeed = sarahangPlayer.playbackSpeed

    fun setSpeedRaw(speed: Float) {
        setSpeed(speed.roundToInt().toFloat() / 10)
    }

    fun setSpeed(speed: Float) {
        playerEventLogger.logEvent("set_playback_speed", mapOf("speed" to speed.toString()))

        if (speed > 0) { // UI slider can return negative values if swiped too fast
            sarahangPlayer.setPlaybackSpeed(speed)
        }
    }

    val quickSpeedIntervals = listOf(0.5f, 0.8f, 1.0f, 1.2f, 1.5f, 2.0f, 2.5f, 3.0f, 5.0f)
}

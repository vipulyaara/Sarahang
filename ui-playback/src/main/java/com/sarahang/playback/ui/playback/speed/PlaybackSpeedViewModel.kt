package com.sarahang.playback.ui.playback.speed

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.ViewModel
import com.sarahang.playback.core.PreferencesStore
import com.sarahang.playback.core.apis.PlayerEventLogger
import com.sarahang.playback.core.players.SarahangPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class PlaybackSpeedViewModel @Inject constructor(
    private val sarahangPlayer: SarahangPlayer,
    private val playerEventLogger: PlayerEventLogger,
    preferences: PreferencesStore,
) : ViewModel() {
    val currentSpeed = MutableStateFlow(1f)

    init {
        currentSpeed.onEach {
            sarahangPlayer.setPlaybackSpeed(it)
        }
    }

    fun setSpeed(speed: Float) {
        currentSpeed.value = speed
    }
}

private val defaultSpeedIntervalKey = longPreferencesKey("playback_speed")

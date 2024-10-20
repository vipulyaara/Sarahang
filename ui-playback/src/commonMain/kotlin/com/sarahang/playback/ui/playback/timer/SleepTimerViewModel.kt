package com.sarahang.playback.ui.playback.timer

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarahang.playback.core.PreferencesStore
import com.sarahang.playback.core.apis.PlayerEventLogger
import com.sarahang.playback.core.timer.SleepTimer
import com.sarahang.playback.core.timer.TimerInterval
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class SleepTimerViewModel @Inject constructor(
    private val sleepTimer: SleepTimer,
    private val playerEventLogger: PlayerEventLogger,
    preferences: PreferencesStore,
) : ViewModel() {
    private val currentTimerInterval = preferences.getStateFlow(
        keyName = defaultTimerIntervalKey,
        scope = viewModelScope,
        initialValue = TimerInterval.FifteenMinutes.millis()
    )

    val state: StateFlow<SleepTimerViewState> = combine(
        flowOf(TimerInterval.all()),
        currentTimerInterval.map { TimerInterval.find(it) },
        sleepTimer.observeRunningStatus(),
        ::SleepTimerViewState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SleepTimerViewState()
    )

    fun startTimer(timerInterval: TimerInterval) {
        playerEventLogger.logEvent(
            event = "player_timer_started",
            data = mapOf("duration" to timerInterval.formattedTime())
        )
        sleepTimer.start(
            time = timerInterval.time,
            timeUnit = timerInterval.timeUnit
        )
    }

    fun stopTimer() {
        playerEventLogger.logEvent("player_timer_stopped")
        sleepTimer.cancelAlarm()
    }
}

data class SleepTimerViewState(
    val timerIntervals: List<TimerInterval> = TimerInterval.all(),
    val currentTimerInterval: TimerInterval = TimerInterval.FifteenMinutes,
    val isTimerRunning: Boolean = false
)

internal val defaultTimerIntervalKey = longPreferencesKey("default_timer_interval")

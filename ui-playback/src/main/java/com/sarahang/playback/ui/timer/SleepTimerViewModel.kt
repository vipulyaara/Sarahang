package com.sarahang.playback.ui.timer

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarahang.playback.core.PreferencesStore
import com.sarahang.playback.core.timer.SleepTimer
import com.sarahang.playback.core.timer.TimerInterval
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SleepTimerViewModel @Inject constructor(
    private val sleepTimer: SleepTimer,
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
        sleepTimer.start(timerInterval.time, timerInterval.timeUnit)
    }

    fun stopTimer() {
        sleepTimer.cancelAlarm()
    }
}

data class SleepTimerViewState(
    val timerIntervals: List<TimerInterval> = TimerInterval.all(),
    val currentTimerInterval: TimerInterval = TimerInterval.FifteenMinutes,
    val isTimerRunning: Boolean = false
)

private val defaultTimerIntervalKey = longPreferencesKey("default_timer_interval")

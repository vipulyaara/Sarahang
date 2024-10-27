package com.sarahang.playback.core.timer

import kotlinx.coroutines.flow.Flow
import kotlin.time.DurationUnit

interface SleepTimer {
    fun start(time: Long, timeUnit: DurationUnit)
    fun cancelAlarm()
    fun observeRunningStatus(): Flow<Boolean>
}

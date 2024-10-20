package com.sarahang.playback.core.timer

import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

interface SleepTimer {
    fun start(time: Long, timeUnit: TimeUnit)
    fun cancelAlarm()
    fun observeRunningStatus(): Flow<Boolean>
}

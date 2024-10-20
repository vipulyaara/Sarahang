package com.sarahang.playback.core.timer

import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SleepTimerImpl @Inject constructor(): SleepTimer {
    override fun start(time: Long, timeUnit: TimeUnit) {
        TODO("Not yet implemented")
    }

    override fun cancelAlarm() {
        TODO("Not yet implemented")
    }

    override fun observeRunningStatus(): Flow<Boolean> {
        TODO("Not yet implemented")
    }
}

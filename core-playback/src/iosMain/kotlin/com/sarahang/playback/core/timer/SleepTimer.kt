package com.sarahang.playback.core.timer

import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject
import kotlin.time.DurationUnit

@Inject
class SleepTimerImpl : SleepTimer {
    override fun start(time: Long, timeUnit: DurationUnit) {
        TODO("Not yet implemented")
    }

    override fun cancelAlarm() {
        TODO("Not yet implemented")
    }

    override fun observeRunningStatus(): Flow<Boolean> {
        TODO("Not yet implemented")
    }
}

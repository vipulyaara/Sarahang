package com.sarahang.playback.core.timer

import com.sarahang.playback.core.millisToDuration
import java.util.concurrent.TimeUnit

sealed class TimerInterval(val time: Long, val timeUnit: TimeUnit, val text: String) {
    data object FiveSeconds : TimerInterval(5, TimeUnit.SECONDS, "5 seconds")
    data object TenSeconds : TimerInterval(10, TimeUnit.SECONDS, "10 seconds")
    data object FiveMinutes : TimerInterval(5, TimeUnit.MINUTES, "5 minutes")
    data object FifteenMinutes : TimerInterval(15, TimeUnit.MINUTES, "15 minutes")
    data object ThirtyMinutes : TimerInterval(30, TimeUnit.MINUTES, "30 minutes")
    data object SixtyMinutes : TimerInterval(60, TimeUnit.MINUTES, "60 minutes")
    data object NinetyMinutes : TimerInterval(90, TimeUnit.MINUTES, "90 minutes")
    data object TwoHours : TimerInterval(120, TimeUnit.MINUTES, "120 minutes")

    fun millis() = timeUnit.toMillis(time)

    fun formattedTime() = timeUnit.toMillis(time).millisToDuration()

    companion object {
        fun find(timeInMillis: Long) = all().first { it.millis() == timeInMillis }

        fun all() = buildList {
            addAll(
                listOf(
                    FiveMinutes,
                    FifteenMinutes,
                    ThirtyMinutes,
                    SixtyMinutes,
                    NinetyMinutes,
                    TwoHours
                )
            )
        }
    }
}

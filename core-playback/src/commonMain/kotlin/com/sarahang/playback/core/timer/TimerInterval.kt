package com.sarahang.playback.core.timer

import com.sarahang.playback.core.millisToDuration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

sealed class TimerInterval(val time: Long, val timeUnit: DurationUnit, val text: String) {
    data object FiveMinutes : TimerInterval(5, DurationUnit.MINUTES, "5 minutes")
    data object FifteenMinutes : TimerInterval(15, DurationUnit.MINUTES, "15 minutes")
    data object ThirtyMinutes : TimerInterval(30, DurationUnit.MINUTES, "30 minutes")
    data object SixtyMinutes : TimerInterval(60, DurationUnit.MINUTES, "60 minutes")
    data object NinetyMinutes : TimerInterval(90, DurationUnit.MINUTES, "90 minutes")
    data object TwoHours : TimerInterval(120, DurationUnit.MINUTES, "120 minutes")

    fun millis() = time.toDuration(timeUnit).inWholeMilliseconds

    fun formattedTime() = time.toDuration(timeUnit).inWholeMilliseconds.millisToDuration()

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

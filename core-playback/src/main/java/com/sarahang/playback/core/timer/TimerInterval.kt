package com.sarahang.playback.core.timer

import com.google.android.exoplayer2.common.BuildConfig
import com.sarahang.playback.core.millisToDuration
import java.util.concurrent.TimeUnit

sealed class TimerInterval(val time: Long, val timeUnit: TimeUnit, val text: String) {
    object FiveSeconds : TimerInterval(5, TimeUnit.SECONDS, "5 seconds")
    object TenSeconds : TimerInterval(10, TimeUnit.SECONDS, "10 seconds")
    object FiveMinutes : TimerInterval(5, TimeUnit.MINUTES, "5 minutes")
    object FifteenMinutes : TimerInterval(15, TimeUnit.MINUTES, "15 minutes")
    object ThirtyMinutes : TimerInterval(30, TimeUnit.MINUTES, "30 minutes")
    object SixtyMinutes : TimerInterval(60, TimeUnit.MINUTES, "60 minutes")
    object NinetyMinutes : TimerInterval(90, TimeUnit.MINUTES, "90 minutes")
    object TwoHours : TimerInterval(120, TimeUnit.MINUTES, "120 minutes")

    fun millis() = timeUnit.toMillis(time)

    fun formattedTime() = timeUnit.toMillis(time).millisToDuration()

    companion object {
        fun find(timeInMillis: Long) = all().first { it.millis() == timeInMillis }

        fun all() = buildList {
            if (BuildConfig.DEBUG) addAll(listOf(FiveSeconds, TenSeconds))
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

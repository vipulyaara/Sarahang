package com.sarahang.playback.core

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun Long.millisToDuration(): String {
    val seconds = (this / 1000).toInt() % 60
    val minutes = (this / (1000 * 60) % 60).toInt()
    val hours = (this / (1000 * 60 * 60) % 24).toInt()
    "${timeAddZeros(hours)}:${timeAddZeros(minutes, "0")}:${timeAddZeros(seconds, "00")}".apply {
        return if (startsWith(":")) replaceFirst(":", "") else this
    }
}

fun timeAddZeros(number: Int?, ifZero: String = ""): String {
    return when (number) {
        0 -> ifZero
        1, 2, 3, 4, 5, 6, 7, 8, 9 -> "0$number"
        else -> number.toString()
    }
}

fun flowInterval(interval: Long, timeUnit: DurationUnit = DurationUnit.MILLISECONDS): Flow<Int> {
    val delayMillis = interval.toDuration(timeUnit).inWholeMilliseconds
    return channelFlow {
        var tick = 0
        send(tick)
        while (true) {
            delay(delayMillis)
            send(++tick)
        }
    }
}

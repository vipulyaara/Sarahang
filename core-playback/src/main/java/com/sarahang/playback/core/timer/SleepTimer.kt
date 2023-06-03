package com.sarahang.playback.core.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.content.getSystemService
import com.sarahang.playback.core.ACTION_QUIT
import com.sarahang.playback.core.services.PlayerService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface SleepTimer {
    fun start(time: Long, timeUnit: TimeUnit)
    fun isRunning(): Boolean
    fun cancelAll()
}

class SleepTimerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SleepTimer {
    private val alarmManager by lazy { context.getSystemService<AlarmManager>() }
    private val intent by lazy { makeTimerIntent() }

    override fun start(time: Long, timeUnit: TimeUnit) {
        alarmManager?.setExact(
            /* type = */ AlarmManager.ELAPSED_REALTIME_WAKEUP,
            /* triggerAtMillis = */ SystemClock.elapsedRealtime() + timeUnit.toMillis(time),
            /* operation = */ makeTimerPendingIntent()
        )
    }

    override fun cancelAll() {
        alarmManager?.cancel(makeTimerPendingIntent())
    }

    override fun isRunning(): Boolean {
        return PendingIntent.getBroadcast(
            /* context = */ context,
            /* requestCode = */ 0,
            /* intent = */ intent,
            /* flags = */ PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) != null
    }

    private fun makeTimerPendingIntent(): PendingIntent? {
        return PendingIntent.getService(
            /* context = */ context,
            /* requestCode = */ 0,
            /* intent = */ intent,
            /* flags = */ PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun makeTimerIntent(): Intent {
        val intent = Intent(context, PlayerService::class.java)
        return intent.setAction(ACTION_QUIT)
    }
}


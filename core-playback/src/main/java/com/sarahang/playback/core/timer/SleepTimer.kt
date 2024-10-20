package com.sarahang.playback.core.timer

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.SystemClock
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.kafka.base.ProcessLifetime
import com.sarahang.playback.core.ACTION_QUIT
import com.sarahang.playback.core.PreferencesStore
import com.sarahang.playback.core.R
import com.sarahang.playback.core.services.PlayerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SleepTimerImpl @Inject constructor(
    private val context: Application,
    @ProcessLifetime private val processScope: CoroutineScope,
    private val preferencesStore: PreferencesStore,
) : SleepTimer {
    private val alarmManager by lazy { context.getSystemService<AlarmManager>() }

    init {
        setRunningStatus()
    }

    override fun start(time: Long, timeUnit: TimeUnit) {
        cancelAlarm()
        val alarmTime = SystemClock.elapsedRealtime() + timeUnit.toMillis(time)

        makeTimerPendingIntent(PendingIntent.FLAG_CANCEL_CURRENT)?.let { pendingIntent ->
            setInexactAlarm(alarmTime = alarmTime, pendingIntent = pendingIntent)
            onAlarmSet()
        }
    }

    private fun setInexactAlarm(alarmTime: Long, pendingIntent: PendingIntent) {
        alarmManager?.set(
            /* type = */ AlarmManager.ELAPSED_REALTIME_WAKEUP,
            /* triggerAtMillis = */ alarmTime,
            /* operation = */ pendingIntent
        )
    }

    private fun onAlarmSet() {
        setRunningStatus()
        Toast.makeText(context, context.getString(R.string.timer_is_set), Toast.LENGTH_SHORT).show()
    }

    override fun cancelAlarm() {
        alarmIntent()?.let { alarmIntent ->
            alarmManager?.cancel(alarmIntent)
            alarmIntent.cancel()
        }
        setRunningStatus()
    }

    override fun observeRunningStatus(): Flow<Boolean> =
        preferencesStore.data(alarmRunningPreferenceKey).map { it ?: false }

    private fun setRunningStatus() {
        saveAlarmRunning(isRunning())
    }

    private fun isRunning() = alarmIntent() != null

    private fun saveAlarmRunning(isRunning: Boolean) {
        processScope.launch(Dispatchers.IO) {
            preferencesStore.save(alarmRunningPreferenceKey, isRunning)
        }
    }

    private fun alarmIntent() = makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE)

    private fun makeTimerPendingIntent(flag: Int): PendingIntent? {
        return PendingIntent.getService(
            /* context = */ context,
            /* requestCode = */ 0,
            /* intent = */ makeTimerIntent(),
            /* flags = */ flag or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun makeTimerIntent(): Intent {
        val intent = Intent(context, PlayerService::class.java)
        return intent.setAction(ACTION_QUIT)
    }
}

private val alarmRunningPreferenceKey = booleanPreferencesKey("is_alarm_running")


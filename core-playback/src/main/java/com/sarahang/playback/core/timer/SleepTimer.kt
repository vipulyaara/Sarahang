package com.sarahang.playback.core.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.sarahang.playback.core.ACTION_QUIT
import com.sarahang.playback.core.PreferencesStore
import com.sarahang.playback.core.R
import com.sarahang.playback.core.injection.ProcessLifetime
import com.sarahang.playback.core.services.PlayerService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface SleepTimer {
    fun start(time: Long, timeUnit: TimeUnit, context: Context, isExactAlarmEnabled: Boolean)

    // alarm PendingIntent, if an alarm is set, or null if no alarm is set
    fun alarmIntent(): PendingIntent?
    fun cancelAlarm()

    fun observeRunningStatus(): Flow<Boolean>
}

class SleepTimerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @ProcessLifetime private val processScope: CoroutineScope,
    private val preferencesStore: PreferencesStore
) : SleepTimer {
    private val alarmManager by lazy { context.getSystemService<AlarmManager>() }

    init {
        setRunningStatus()
    }

    override fun start(
        time: Long,
        timeUnit: TimeUnit,
        context: Context,
        isExactAlarmEnabled: Boolean
    ) {
        cancelAlarm()
        val alarmTime = SystemClock.elapsedRealtime() + timeUnit.toMillis(time)

        makeTimerPendingIntent(PendingIntent.FLAG_CANCEL_CURRENT)?.let { pendingIntent ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isExactAlarmEnabled) {
                    if (alarmManager?.canScheduleExactAlarms() == false) {
                        setExactAlarmWithExplicitPermission(context)
                    } else {
                        setExactAlarm(alarmTime = alarmTime, pendingIntent = pendingIntent)
                        onAlarmSet()
                    }
                } else {
                    setInexactAlarm(alarmTime = alarmTime, pendingIntent = pendingIntent)
                    onAlarmSet()
                }
            } else {
                setExactAlarm(alarmTime = alarmTime, pendingIntent = pendingIntent)
                onAlarmSet()
            }
        }

    }

    private fun setExactAlarm(alarmTime: Long, pendingIntent: PendingIntent) {
        alarmManager?.setExact(
            /* type = */ AlarmManager.ELAPSED_REALTIME_WAKEUP,
            /* triggerAtMillis = */ alarmTime,
            /* operation = */ pendingIntent
        )
    }

    private fun setInexactAlarm(alarmTime: Long, pendingIntent: PendingIntent) {
        alarmManager?.set(
            /* type = */ AlarmManager.ELAPSED_REALTIME_WAKEUP,
            /* triggerAtMillis = */ alarmTime,
            /* operation = */ pendingIntent
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setExactAlarmWithExplicitPermission(context: Context) {
        Toast.makeText(
            /* context = */ context,
            /* text = */ context.getString(R.string.enable_alarm_permission_text),
            /* duration = */ Toast.LENGTH_SHORT
        ).show()
        Intent().also { intent ->
            intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
            context.startActivity(intent)
        }
    }

    private fun onAlarmSet() {
        setRunningStatus()
        Toast.makeText(
            /* context = */ context,
            /* text = */ context.getString(R.string.timer_is_set),
            /* duration = */ Toast.LENGTH_SHORT
        ).show()
    }

    override fun cancelAlarm() {
        alarmIntent()?.let { alarmManager?.cancel(it) }
        alarmIntent()?.cancel()
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

    override fun alarmIntent() = makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE)

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


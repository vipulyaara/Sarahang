package com.sarahang.playback.core.services

import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.sarahang.playback.core.ACTION_QUIT
import com.sarahang.playback.core.MediaNotificationsImpl
import com.sarahang.playback.core.NEXT
import com.sarahang.playback.core.NOTIFICATION_ID
import com.sarahang.playback.core.PLAY_PAUSE
import com.sarahang.playback.core.PREVIOUS
import com.sarahang.playback.core.STOP_PLAYBACK
import com.sarahang.playback.core.apis.AudioDataSource
import com.sarahang.playback.core.isIdle
import com.sarahang.playback.core.models.MediaId
import com.sarahang.playback.core.models.MediaId.Companion.CALLER_OTHER
import com.sarahang.playback.core.models.MediaId.Companion.CALLER_SELF
import com.sarahang.playback.core.models.toAudioList
import com.sarahang.playback.core.models.toMediaId
import com.sarahang.playback.core.models.toMediaItems
import com.sarahang.playback.core.playPause
import com.sarahang.playback.core.players.SarahangPlayerImpl
import com.sarahang.playback.core.receivers.BecomingNoisyReceiver
import com.sarahang.playback.core.timer.SleepTimer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PlayerService : MediaBrowserServiceCompat(), CoroutineScope by MainScope() {

    companion object {
        var IS_FOREGROUND = false
    }

    @Inject
    protected lateinit var player: SarahangPlayerImpl

    @Inject
    protected lateinit var audioDataSource: AudioDataSource

    @Inject
    protected lateinit var timer: SleepTimer

    @Inject
    protected lateinit var mediaNotifications: MediaNotificationsImpl

    private lateinit var becomingNoisyReceiver: BecomingNoisyReceiver

    override fun onCreate() {
        super.onCreate()

        sessionToken = player.getSession().sessionToken
        becomingNoisyReceiver = BecomingNoisyReceiver(this, sessionToken!!)

        player.onPlayingState { isPlaying, byUi ->
            val isIdle = player.getSession().controller.playbackState.isIdle
            if (!isPlaying && isIdle) {
                pauseForeground(byUi)
                mediaNotifications.clearNotifications()
            } else {
                startForeground()
            }

            mediaNotifications.updateNotification(getSession())
        }

        player.onMetaDataChanged {
            mediaNotifications.updateNotification(getSession())
        }
    }

    private fun startForeground() {
        if (IS_FOREGROUND) {
            Timber.w("Tried to start foreground, but was already in foreground")
            return
        }
        Timber.d("Starting foreground service")
        startForeground(
            NOTIFICATION_ID,
            mediaNotifications.buildNotification(player.getSession())
        )
        becomingNoisyReceiver.register()
        IS_FOREGROUND = true
    }

    private fun pauseForeground(removeNotification: Boolean) {
        if (!IS_FOREGROUND) {
            Timber.w("Tried to stop foreground, but was already NOT in foreground")
            return
        }
        Timber.d("Stopping foreground service")
        becomingNoisyReceiver.unregister()
        stopForeground(if (removeNotification) STOP_FOREGROUND_REMOVE else STOP_FOREGROUND_DETACH)
        IS_FOREGROUND = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return START_STICKY
        }

        val mediaSession = player.getSession()
        val controller = mediaSession.controller

        when (intent.action) {
            PLAY_PAUSE -> controller.playPause()
            NEXT -> controller.transportControls.skipToNext()
            PREVIOUS -> controller.transportControls.skipToPrevious()
            STOP_PLAYBACK -> controller.transportControls.stop()
            ACTION_QUIT -> {
                Timber.d("Quitting service by request")
                controller.transportControls.pause()
                timer.cancelAlarm()
                pauseForeground(true)
            }
        }

        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return START_STICKY
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        val caller =
            if (clientPackageName == applicationContext.packageName) CALLER_SELF else CALLER_OTHER
        return BrowserRoot(MediaId("-1", caller = caller).toString(), null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.detach()
        launch {
            val itemList = withContext(Dispatchers.IO) { loadChildren(parentId) }
            result.sendResult(itemList)
        }
    }

    private suspend fun loadChildren(parentId: String): MutableList<MediaBrowserCompat.MediaItem> {
        val list = mutableListOf<MediaBrowserCompat.MediaItem>()
        val mediaId = parentId.toMediaId()
        list.addAll(mediaId.toAudioList(audioDataSource).toMediaItems())
        return list
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        launch {
            player.pause()
            player.saveQueueState()
            player.stop(false)
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        launch {
            player.saveQueueState()
            player.release()
        }
    }
}

package com.sarahang.playback.core.services

import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.sarahang.playback.core.ACTION_QUIT
import com.sarahang.playback.core.NEXT
import com.sarahang.playback.core.NOTIFICATION_ID
import com.sarahang.playback.core.PLAY_PAUSE
import com.sarahang.playback.core.PREVIOUS
import com.sarahang.playback.core.STOP_PLAYBACK
import com.sarahang.playback.core.isIdle
import com.sarahang.playback.core.models.MediaId
import com.sarahang.playback.core.models.MediaId.Companion.CALLER_OTHER
import com.sarahang.playback.core.models.MediaId.Companion.CALLER_SELF
import com.sarahang.playback.core.models.toAudioList
import com.sarahang.playback.core.models.toMediaId
import com.sarahang.playback.core.models.toMediaItems
import com.sarahang.playback.core.playPause
import com.sarahang.playback.core.receivers.BecomingNoisyReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerService : MediaBrowserServiceCompat(), CoroutineScope by MainScope() {
    private lateinit var component: PlayerServiceDependencies
    private val player by lazy { component.player }
    private val sessionPlayer by lazy { component.sessionPlayer }
    private val mediaNotifications by lazy { component.mediaNotifications }
    private val logger by lazy { component.logger }
    private val timer by lazy { component.timer }
    private val audioDataSource by lazy { component.audioDataSource }

    companion object {
        var IS_FOREGROUND = false
    }

    private lateinit var becomingNoisyReceiver: BecomingNoisyReceiver

    override fun onCreate() {
        super.onCreate()

        component = this.application as PlayerServiceDependencies

        sessionToken = sessionPlayer.getSession().sessionToken
        becomingNoisyReceiver = BecomingNoisyReceiver(this, sessionToken!!)

        player.onPlayingState { isPlaying, byUi ->
            val isIdle = sessionPlayer.getSession().controller.playbackState.isIdle
            if (!isPlaying && isIdle) {
                pauseForeground(byUi)
                mediaNotifications.clearNotifications()
            } else {
                startForeground()
            }

            mediaNotifications.updateNotification(sessionPlayer.getSession())
        }

        player.onMetaDataChanged {
            mediaNotifications.updateNotification(sessionPlayer.getSession())
        }
    }

    private fun startForeground() {
        if (IS_FOREGROUND) {
            logger.w("Tried to start foreground, but was already in foreground")
            return
        }
        logger.d("Starting foreground service")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                mediaNotifications.buildNotification(sessionPlayer.getSession()),
                FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(
                NOTIFICATION_ID,
                mediaNotifications.buildNotification(sessionPlayer.getSession())
            )
        }
        becomingNoisyReceiver.register()
        IS_FOREGROUND = true
    }

    private fun pauseForeground(removeNotification: Boolean) {
        if (!IS_FOREGROUND) {
            logger.w("Tried to stop foreground, but was already NOT in foreground")
            return
        }
        logger.d("Stopping foreground service")
        becomingNoisyReceiver.unregister()
        stopForeground(removeNotification)
        IS_FOREGROUND = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return START_STICKY
        }

        val mediaSession = sessionPlayer.getSession()
        val controller = mediaSession.controller

        when (intent.action) {
            PLAY_PAUSE -> controller.playPause()
            NEXT -> controller.transportControls.skipToNext()
            PREVIOUS -> controller.transportControls.skipToPrevious()
            STOP_PLAYBACK -> controller.transportControls.stop()
            ACTION_QUIT -> {
                logger.d("Quitting service by request")
                controller.transportControls.pause()
                timer.cancelAlarm()
            }
        }

        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return START_STICKY
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?,
    ): BrowserRoot {
        val caller =
            if (clientPackageName == applicationContext.packageName) CALLER_SELF else CALLER_OTHER
        return BrowserRoot(MediaId("-1", caller = caller).toString(), null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>,
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
            super.onDestroy()
        }
    }
}

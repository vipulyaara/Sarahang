package com.sarahang.playback.core
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat.ACTION_STOP
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.palette.graphics.Palette
import com.sarahang.playback.core.receivers.MediaButtonReceiver.Companion.buildMediaButtonPendingIntent
import com.sarahang.playback.core.services.PlayerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.kafka.base.ProcessLifetime
import javax.inject.Inject
import androidx.media.app.NotificationCompat as NotificationMediaCompat

const val NOTIFICATION_ID = 2000
const val CHANNEL_ID = "audio-player"

const val APP_PACKAGE_NAME = "com.kafka.user"
const val PREVIOUS = "action_previous"
const val NEXT = "action_next"
const val STOP_PLAYBACK = "action_stop"
const val PLAY_PAUSE = "action_play_or_pause"
const val REPEAT_ONE = "action_repeat_one"
const val REPEAT_ALL = "action_repeat_all"
const val PLAY_ALL_SHUFFLED = "action_play_all_shuffled"
const val PLAY_NEXT = "action_play_next"
const val REMOVE_QUEUE_ITEM_BY_POSITION = "action_remove_queue_item_by_position"
const val REMOVE_QUEUE_ITEM_BY_ID = "action_remove_queue_item_by_id"
const val UPDATE_QUEUE = "action_update_queue"
const val SET_MEDIA_STATE = "action_set_media_state"
const val PLAY_ACTION = "action_play"
const val PAUSE_ACTION = "action_pause"
const val SWAP_ACTION = "swap_action"
const val BY_UI_KEY = "by_ui_key"
const val ACTION_QUIT = "$APP_PACKAGE_NAME.quitService"

interface MediaNotifications {
    fun updateNotification(mediaSession: MediaSessionCompat)
    fun buildNotification(mediaSession: MediaSessionCompat): Notification
    fun clearNotifications()
}

class MediaNotificationsImpl @Inject constructor(
    private val context: Application,
    @ProcessLifetime private val coroutineScope: CoroutineScope,
) : MediaNotifications {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun updateNotification(mediaSession: MediaSessionCompat) {
        if (!PlayerService.IS_FOREGROUND) return

        coroutineScope.launch {
            notificationManager.notify(NOTIFICATION_ID, buildNotification(mediaSession))
        }
    }

    override fun buildNotification(mediaSession: MediaSessionCompat): Notification {
        if (mediaSession.controller.metadata == null || mediaSession.controller.playbackState == null) {
            return createEmptyNotification()
        }

        val albumName = mediaSession.controller.metadata.album
        val artistName = mediaSession.controller.metadata.artist
        val trackName = mediaSession.controller.metadata.title
        val artwork = mediaSession.controller.metadata.artwork
        val isPlaying = mediaSession.isPlaying()
        val isBuffering = mediaSession.isBuffering()
        val description = mediaSession.controller.metadata.displayDescription

        val pm: PackageManager = context.packageManager
        val nowPlayingIntent = pm.getLaunchIntentForPackage(context.packageName)
        val clickIntent = PendingIntent.getActivity(
            context,
            0,
            nowPlayingIntent,
            FLAG_UPDATE_CURRENT or FLAG_MUTABLE
        )

        createNotificationChannel()

        val style = NotificationMediaCompat.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowCancelButton(true)
            .setShowActionsInCompactView(0)
            .setCancelButtonIntent(buildMediaButtonPendingIntent(context, ACTION_STOP))

        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setStyle(style)
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setLargeIcon(artwork)
            setContentIntent(clickIntent)
            setContentTitle(trackName)
            setContentText("$artistName - $albumName")
            setSubText(description)
            setColorized(true)
            setShowWhen(false)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setDeleteIntent(buildMediaButtonPendingIntent(context, ACTION_STOP))
            addAction(getPreviousAction(context))
            if (isBuffering)
                addAction(getBufferingAction())
            else
                addAction(
                    getPlayPauseAction(
                        context,
                        if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                    )
                )
            addAction(getNextAction(context))
            addAction(getStopAction(context))
        }

        if (artwork != null) {
            builder.color = Palette.from(artwork)
                .generate()
                .getDominantColor(Color.parseColor("#16053D"))
        }

        return builder.build()
    }

    override fun clearNotifications() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun getBufferingAction(): NotificationCompat.Action {
        return NotificationCompat.Action(R.drawable.ic_hourglass_empty, "", null)
    }

    private fun getStopAction(context: Context): NotificationCompat.Action {
        val actionIntent =
            Intent(context, PlayerService::class.java).apply { action = STOP_PLAYBACK }
        val pendingIntent = getService(context, 0, actionIntent, FLAG_IMMUTABLE)
        return NotificationCompat.Action(R.drawable.ic_stop, "", pendingIntent)
    }

    private fun getPreviousAction(context: Context): NotificationCompat.Action {
        val actionIntent = Intent(context, PlayerService::class.java).apply { action = PREVIOUS }
        val pendingIntent = getService(context, 0, actionIntent, FLAG_IMMUTABLE)
        return NotificationCompat.Action(R.drawable.ic_skip_previous, "", pendingIntent)
    }

    private fun getPlayPauseAction(
        context: Context,
        @DrawableRes playButtonResId: Int,
    ): NotificationCompat.Action {
        val actionIntent = Intent(context, PlayerService::class.java).apply { action = PLAY_PAUSE }
        val pendingIntent = getService(context, 0, actionIntent, FLAG_IMMUTABLE)
        return NotificationCompat.Action(playButtonResId, "", pendingIntent)
    }

    private fun getNextAction(context: Context): NotificationCompat.Action {
        val actionIntent = Intent(context, PlayerService::class.java).apply {
            action = NEXT
        }
        val pendingIntent = getService(context, 0, actionIntent, FLAG_IMMUTABLE)
        return NotificationCompat.Action(R.drawable.ic_skip_next, "", pendingIntent)
    }

    private fun createEmptyNotification(): Notification {
        createNotificationChannel()
        return NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setContentTitle(context.getString(R.string.player_name))
            setColorized(true)
            setShowWhen(false)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }.build()
    }

    private fun createNotificationChannel() {
        if (!isOreo()) return
        val name = context.getString(R.string.player_name)
        val channel = NotificationChannel(CHANNEL_ID, name, IMPORTANCE_LOW).apply {
            description = context.getString(R.string.player_name)
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)
    }
}

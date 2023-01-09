package com.sarahang.playback.core.players

import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat.Builder
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_NONE
import android.support.v4.media.session.PlaybackStateCompat.STATE_NONE
import androidx.core.os.bundleOf
import com.sarahang.playback.core.BY_UI_KEY
import com.sarahang.playback.core.PAUSE_ACTION
import com.sarahang.playback.core.PLAY_ACTION
import com.sarahang.playback.core.PLAY_ALL_SHUFFLED
import com.sarahang.playback.core.PLAY_NEXT
import com.sarahang.playback.core.REMOVE_QUEUE_ITEM_BY_ID
import com.sarahang.playback.core.REMOVE_QUEUE_ITEM_BY_POSITION
import com.sarahang.playback.core.REPEAT_ALL
import com.sarahang.playback.core.REPEAT_ONE
import com.sarahang.playback.core.SET_MEDIA_STATE
import com.sarahang.playback.core.SWAP_ACTION
import com.sarahang.playback.core.UPDATE_QUEUE
import com.sarahang.playback.core.audio.AudioFocusHelper
import com.sarahang.playback.core.isPlaying
import com.sarahang.playback.core.models.toMediaIdList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber

const val SEEK_TO = "action_seek_to"

const val QUEUE_MEDIA_ID_KEY = "queue_media_id_key"
const val QUEUE_TITLE_KEY = "queue_title_key"
const val QUEUE_LIST_KEY = "queue_list_key"

const val QUEUE_FROM_POSITION_KEY = "queue_from_position_key"
const val QUEUE_TO_POSITION_KEY = "queue_to_position_key"

class MediaSessionCallback(
    private val mediaSession: MediaSessionCompat,
    private val sarahangPlayer: SarahangPlayer,
    private val audioFocusHelper: AudioFocusHelper,
) : MediaSessionCompat.Callback(), CoroutineScope by MainScope() {

    init {
        audioFocusHelper.onAudioFocusGain {
            if (isAudioFocusGranted && !sarahangPlayer.getSession().isPlaying()) {
                sarahangPlayer.playAudio()
            } else {
                audioFocusHelper.setVolume(AudioManager.ADJUST_RAISE)
            }
            isAudioFocusGranted = false
        }
        audioFocusHelper.onAudioFocusLoss {
            abandonPlayback()
            isAudioFocusGranted = false
            sarahangPlayer.pause()
        }

        audioFocusHelper.onAudioFocusLossTransient {
            Timber.d("TRANSIENT")
            if (sarahangPlayer.getSession().isPlaying()) {
                isAudioFocusGranted = true
                sarahangPlayer.pause()
            }
        }

        audioFocusHelper.onAudioFocusLossTransientCanDuck {
            Timber.d("TRANSIENT_CAN_DUCK")
            audioFocusHelper.setVolume(AudioManager.ADJUST_LOWER)
        }
    }

    override fun onPause() {
        Timber.d("onPause")
        sarahangPlayer.pause()
    }

    override fun onPlay() {
        Timber.d("onPlay")
        playOnFocus()
    }

    override fun onPlayFromSearch(query: String?, extras: Bundle?) {
        Timber.d("onPlayFromSearch, query = $query, $extras")
        query?.let {
            // val audio = findAudioForQuery(query)
            // if (audio != null) {
            //     launch {
            //         musicPlayer.playAudio(audio)
            //     }
            // }
        } ?: onPlay()
    }

    override fun onFastForward() {
        Timber.d("onFastForward")
        sarahangPlayer.fastForward()
    }

    override fun onRewind() {
        Timber.d("onRewind")
        sarahangPlayer.rewind()
    }

    override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
        Timber.d("onPlayFromMediaId, $mediaId, $extras")
        launch { sarahangPlayer.setDataFromMediaId(mediaId, extras ?: bundleOf()) }
    }

    override fun onSeekTo(position: Long) {
        Timber.d("onSeekTo: position=$position")
        sarahangPlayer.seekTo(position)
    }

    override fun onSkipToNext() {
        Timber.d("onSkipToNext()")
        launch { sarahangPlayer.nextAudio() }
    }

    override fun onSkipToPrevious() {
        Timber.d("onSkipToPrevious()")
        launch { sarahangPlayer.previousAudio() }
    }

    override fun onSkipToQueueItem(id: Long) {
        Timber.d("onSkipToQueueItem: $id")
        launch { sarahangPlayer.skipTo(id.toInt()) }
    }

    override fun onStop() {
        Timber.d("onStop()")
        sarahangPlayer.stop()
    }

    override fun onSetRepeatMode(repeatMode: Int) {
        super.onSetRepeatMode(repeatMode)
        val bundle = mediaSession.controller.playbackState.extras ?: Bundle()
        sarahangPlayer.setPlaybackState(
            Builder(mediaSession.controller.playbackState)
                .setExtras(
                    bundle.apply {
                        putInt(REPEAT_MODE, repeatMode)
                    }
                ).build()
        )
    }

    override fun onSetShuffleMode(shuffleMode: Int) {
        super.onSetShuffleMode(shuffleMode)
        val bundle = mediaSession.controller.playbackState.extras ?: Bundle()
        sarahangPlayer.setPlaybackState(
            Builder(mediaSession.controller.playbackState)
                .setExtras(
                    bundle.apply {
                        putInt(SHUFFLE_MODE, shuffleMode)
                    }
                ).build()
        )
        sarahangPlayer.shuffleQueue(shuffleMode != SHUFFLE_MODE_NONE)
    }

    override fun onCustomAction(action: String?, extras: Bundle?) {
        when (action) {
            SET_MEDIA_STATE -> launch { setSavedMediaSessionState() }
            REPEAT_ONE -> launch { sarahangPlayer.repeatAudio() }
            REPEAT_ALL -> launch { sarahangPlayer.repeatQueue() }
            PAUSE_ACTION -> sarahangPlayer.pause(extras ?: bundleOf(BY_UI_KEY to true))
            PLAY_ACTION -> playOnFocus(extras ?: bundleOf(BY_UI_KEY to true))
            PLAY_NEXT -> sarahangPlayer.playNext(extras?.getString(QUEUE_MEDIA_ID_KEY) ?: return)
            REMOVE_QUEUE_ITEM_BY_POSITION -> sarahangPlayer.removeFromQueue(
                extras?.getInt(
                    QUEUE_FROM_POSITION_KEY
                ) ?: return
            )

            REMOVE_QUEUE_ITEM_BY_ID -> sarahangPlayer.removeFromQueue(
                extras?.getString(
                    QUEUE_MEDIA_ID_KEY
                ) ?: return
            )

            UPDATE_QUEUE -> {
                extras ?: return

                val queue = extras.getStringArray(QUEUE_LIST_KEY)?.toList() ?: emptyList()
                val queueTitle = extras.getString(QUEUE_TITLE_KEY)

                sarahangPlayer.updateData(queue, queueTitle)
            }

            PLAY_ALL_SHUFFLED -> {
                extras ?: return

                val controller = mediaSession.controller ?: return

                val queue = extras.getStringArray(QUEUE_LIST_KEY)?.toList() ?: emptyList()
                val queueTitle = extras.getString(QUEUE_TITLE_KEY)
                sarahangPlayer.setData(queue, queueTitle)

                controller.transportControls.setShuffleMode(SHUFFLE_MODE_ALL)

                launch {
                    sarahangPlayer.nextAudio()
                }
            }

            SWAP_ACTION -> {
                extras ?: return
                val from = extras.getInt(QUEUE_FROM_POSITION_KEY)
                val to = extras.getInt(QUEUE_TO_POSITION_KEY)

                sarahangPlayer.swapQueueAudios(from, to)
            }
        }
    }

    private suspend fun setSavedMediaSessionState() {
        val controller = mediaSession.controller ?: return
        Timber.d(controller.playbackState.toString())
        if (controller.playbackState == null || controller.playbackState.state == STATE_NONE) {
            sarahangPlayer.restoreQueueState()
        } else {
            restoreMediaSession()
        }
    }

    private fun restoreMediaSession() {
        mediaSession.setMetadata(mediaSession.controller.metadata)
        sarahangPlayer.setPlaybackState(mediaSession.controller.playbackState)
        sarahangPlayer.setData(
            mediaSession.controller?.queue.toMediaIdList().map { it.value },
            mediaSession.controller?.queueTitle.toString()
        )
    }

    private fun playOnFocus(extras: Bundle = bundleOf(BY_UI_KEY to true)) {
        if (audioFocusHelper.requestPlayback())
            sarahangPlayer.playAudio(extras)
    }
}

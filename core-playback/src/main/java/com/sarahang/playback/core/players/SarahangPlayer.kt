/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package com.sarahang.playback.core.players

import android.app.Application
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.media.session.PlaybackState
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM_ART
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_NONE
import android.support.v4.media.session.PlaybackStateCompat.STATE_BUFFERING
import android.support.v4.media.session.PlaybackStateCompat.STATE_ERROR
import android.support.v4.media.session.PlaybackStateCompat.STATE_NONE
import android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED
import android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING
import android.support.v4.media.session.PlaybackStateCompat.STATE_STOPPED
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media3.common.PlaybackParameters
import com.sarahang.playback.core.BY_UI_KEY
import com.sarahang.playback.core.MediaQueueBuilder
import com.sarahang.playback.core.PreferencesStore
import com.sarahang.playback.core.R
import com.sarahang.playback.core.REPEAT_ALL
import com.sarahang.playback.core.REPEAT_ONE
import com.sarahang.playback.core.apis.AudioDataSource
import com.sarahang.playback.core.apis.Logger
import com.sarahang.playback.core.apis.PlayerEventLogger
import com.sarahang.playback.core.audio.AudioFocusHelper
import com.sarahang.playback.core.audio.AudioQueueManager
import com.sarahang.playback.core.createDefaultPlaybackState
import com.sarahang.playback.core.getBitmap
import com.sarahang.playback.core.isPlaying
import com.sarahang.playback.core.models.Audio
import com.sarahang.playback.core.models.MEDIA_TYPE_AUDIO
import com.sarahang.playback.core.models.MediaId
import com.sarahang.playback.core.models.QueueState
import com.sarahang.playback.core.models.toMediaId
import com.sarahang.playback.core.models.toMediaMetadata
import com.sarahang.playback.core.plus
import com.sarahang.playback.core.position
import com.sarahang.playback.core.repeatMode
import com.sarahang.playback.core.shuffleMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.kafka.base.ApplicationScope
import javax.inject.Inject

typealias OnPrepared<T> = T.() -> Unit
typealias OnError<T> = T.(error: Throwable) -> Unit
typealias OnCompletion<T> = T.() -> Unit
typealias OnBuffering<T> = T.() -> Unit
typealias OnReady<T> = T.() -> Unit
typealias OnMetaDataChanged = SarahangPlayer.() -> Unit
typealias OnIsPlaying<T> = T.(playing: Boolean, byUi: Boolean) -> Unit
typealias OnPlaybackParametersChanged<T> = T.(params: PlaybackParameters) -> Unit

const val REPEAT_MODE = "repeat_mode"
const val SHUFFLE_MODE = "shuffle_mode"
const val QUEUE_CURRENT_INDEX = "queue_current_index"
const val QUEUE_HAS_PREVIOUS = "queue_has_previous"
const val QUEUE_HAS_NEXT = "queue_has_next"

const val DEFAULT_FORWARD_FORWARD = 10 * 1000
const val DEFAULT_FORWARD_REWIND = 10 * 1000

interface SarahangPlayer {
    fun getSession(): MediaSessionCompat
    fun playAudio(extras: Bundle = bundleOf(BY_UI_KEY to true))
    suspend fun playAudio(id: String, index: Int? = null, seekTo: Long? = null)
    suspend fun playAudio(audio: Audio, index: Int? = null, seekTo: Long? = null)
    fun seekTo(position: Long)
    fun fastForward()
    fun rewind()
    fun pause(extras: Bundle = bundleOf(BY_UI_KEY to true))
    suspend fun nextAudio(): String?
    suspend fun repeatAudio()
    suspend fun repeatQueue()
    suspend fun previousAudio()
    fun playNext(id: String)
    suspend fun skipTo(position: Int)
    fun removeFromQueue(position: Int)
    fun removeFromQueue(id: String)
    fun swapQueueAudios(from: Int, to: Int)
    fun stop(byUser: Boolean)
    fun release()
    fun onPlayingState(playing: OnIsPlaying<SarahangPlayer>)
    fun onPrepared(prepared: OnPrepared<SarahangPlayer>)
    fun onError(error: OnError<SarahangPlayer>)
    fun onCompletion(completion: OnCompletion<SarahangPlayer>)
    fun onMetaDataChanged(metaDataChanged: OnMetaDataChanged)
    fun updatePlaybackState(applier: PlaybackStateCompat.Builder.() -> Unit = {})
    fun setPlaybackState(state: PlaybackStateCompat)
    fun setShuffleMode(shuffleMode: Int)
    fun updateData(list: List<String> = emptyList(), title: String? = null)
    fun setData(list: List<String> = emptyList(), title: String? = null)
    suspend fun setDataFromMediaId(_mediaId: String, extras: Bundle = bundleOf())
    suspend fun saveQueueState()
    suspend fun restoreQueueState()
    fun clearRandomAudioPlayed()
    fun setCurrentAudioId(audioId: String, index: Int? = null)
    fun shuffleQueue(isShuffle: Boolean)
    val playbackSpeed: StateFlow<Float>
    fun setPlaybackSpeed(speed: Float)
}

@ApplicationScope
class SarahangPlayerImpl @Inject constructor(
    private val context: Application,
    private val audioPlayer: AudioPlayer,
    private val queueManager: AudioQueueManager,
    private val audioFocusHelper: AudioFocusHelper,
    private val audioDataSource: AudioDataSource,
    private val preferences: PreferencesStore,
    private val mediaQueueBuilder: MediaQueueBuilder,
    private val playerEventLogger: PlayerEventLogger,
    private val logger: Logger,
) : SarahangPlayer, CoroutineScope by MainScope() {

    companion object {
        private const val queueStateKey = "player_queue_state"
    }

    private var isInitialized: Boolean = false

    private var isPlayingCallback: OnIsPlaying<SarahangPlayer> = { _, _ -> }
    private var preparedCallback: OnPrepared<SarahangPlayer> = {}
    private var errorCallback: OnError<SarahangPlayer> = {}
    private var completionCallback: OnCompletion<SarahangPlayer> = {}
    private var metaDataChangedCallback: OnMetaDataChanged = {}

    private val metadataBuilder = MediaMetadataCompat.Builder()
    private val stateBuilder = createDefaultPlaybackState()

    override val playbackSpeed = MutableStateFlow(1f)

    private val pendingIntent =
        PendingIntent.getBroadcast(context, 0, Intent(Intent.ACTION_MEDIA_BUTTON), FLAG_IMMUTABLE)

    private val mediaSession = MediaSessionCompat(
        context,
        context.getString(R.string.player_name),
        null,
        pendingIntent
    ).apply {
        setCallback(
            MediaSessionCallback(
                mediaSession = this,
                sarahangPlayer = this@SarahangPlayerImpl,
                audioFocusHelper = audioFocusHelper,
                logger = logger
            )
        )
        setPlaybackState(stateBuilder.build())

        val sessionIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val sessionActivityPendingIntent =
            PendingIntent.getActivity(context, 0, sessionIntent, FLAG_IMMUTABLE)
        setSessionActivity(sessionActivityPendingIntent)
        isActive = true
    }

    init {
        queueManager.setMediaSession(mediaSession)
        audioPlayer.onPrepared {
            preparedCallback(this@SarahangPlayerImpl)
            launch {
                if (!mediaSession.isPlaying()) audioPlayer.seekTo(mediaSession.position())
                playAudio(bundleOf(SEEK_TO to mediaSession.position()))
            }
        }

        audioPlayer.onCompletion {
            completionCallback(this@SarahangPlayerImpl)
            val controller = getSession().controller
            when (controller.repeatMode) {
                REPEAT_MODE_ONE -> controller.transportControls.sendCustomAction(REPEAT_ONE, null)
                REPEAT_MODE_ALL -> controller.transportControls.sendCustomAction(REPEAT_ALL, null)
                else -> launch { if (nextAudio() == null) goToStart() }
            }
        }
        audioPlayer.onBuffering {
            updatePlaybackState {
                setState(STATE_BUFFERING, mediaSession.position(), 1F)
            }
        }
        audioPlayer.onIsPlaying { playing, byUi ->
            if (playing)
                updatePlaybackState {
                    setState(STATE_PLAYING, mediaSession.position(), 1F)
                    setExtras(
                        bundleOf(
                            REPEAT_MODE to getSession().repeatMode,
                            SHUFFLE_MODE to getSession().shuffleMode,
                            SEEK_TO to mediaSession.position()
                        )
                    )
                }
            isPlayingCallback(playing, byUi)
        }
        audioPlayer.onReady {
//            if (audioPlayer.playWhenReady()) {
            if (!audioPlayer.isPlaying()) {
                logger.d("Player ready but not currently playing, requesting to play")
                audioPlayer.play()
            }
            updatePlaybackState {
                setState(STATE_PLAYING, mediaSession.position(), 1F)
            }
//            }
        }
        audioPlayer.onError { throwable ->
            logger.e(throwable, "AudioPlayer error")
            errorCallback(this@SarahangPlayerImpl, throwable)
            isInitialized = false
            updatePlaybackState {
                setState(STATE_ERROR, 0, 1F)
            }
        }

        audioPlayer.onPlaybackParamsChanged {
            playbackSpeed.value = it.speed
        }
    }

    override fun getSession(): MediaSessionCompat = mediaSession

    override fun pause(extras: Bundle) {
        if (isInitialized && (audioPlayer.isPlaying() || audioPlayer.isBuffering())) {
            audioPlayer.pause()
            updatePlaybackState {
                setState(STATE_PAUSED, mediaSession.position(), 1F)
                setExtras(
                    extras + bundleOf(
                        REPEAT_MODE to getSession().repeatMode,
                        SHUFFLE_MODE to getSession().shuffleMode
                    )
                )
            }
        } else {
            logger.d("Couldn't pause player: ${audioPlayer.isPlaying()}, $isInitialized")
        }
    }

    override fun playAudio(extras: Bundle) {
        if (isInitialized) {
            audioPlayer.play(extras.getLong(SEEK_TO).takeIf { it != 0L })
            return
        }

        val isSourceSet = when (val audio = queueManager.currentAudio) {
            is Audio -> {
                val uri = audio.playbackUrl.toUri()
                audioPlayer.setSource(uri, false)
            }

            else -> false
        }

        if (isSourceSet) {
            isInitialized = true
            audioPlayer.prepare()
        } else {
            logger.e("Couldn't set new source")
        }
    }

    override suspend fun playAudio(id: String, index: Int?, seekTo: Long?) {
        if (audioFocusHelper.requestPlayback()) {
            val audio = audioDataSource.findAudio(id)
            if (audio != null) playAudio(audio = audio, index = index, seekTo = seekTo)
            else {
                logger.e("Audio by id: $id not found")
                updatePlaybackState {
                    setState(STATE_ERROR, seekTo ?: 0, 1F)
                }
            }
        }
    }

    override suspend fun playAudio(audio: Audio, index: Int?, seekTo: Long?) {
        setCurrentAudioId(audio.id, index)
        val refreshedAudio = queueManager.refreshCurrentAudio()
        isInitialized = false

        updatePlaybackState {
            setState(mediaSession.controller.playbackState.state, seekTo ?: 0, 1F)
        }
        setMetaData(refreshedAudio ?: audio)
        playAudio(bundleOf(SEEK_TO to seekTo))
    }

    override suspend fun skipTo(position: Int) {
        if (queueManager.currentAudioIndex == position) {
            logger.d("Not skipping to index=$position")
            return
        }
        queueManager.skipTo(position)
        playAudio(queueManager.currentAudioId, position)
        updatePlaybackState()
    }

    override fun seekTo(position: Long) {
        if (isInitialized) {
            audioPlayer.seekTo(position)
            updatePlaybackState {
                setState(
                    mediaSession.controller.playbackState.state,
                    position,
                    1F
                )
            }
        } else updatePlaybackState {
            setState(
                mediaSession.controller.playbackState.state,
                position,
                1F
            )
        }
        logEvent("seekTo")
    }

    override fun fastForward() {
        val forwardTo = mediaSession.position() + DEFAULT_FORWARD_FORWARD
        queueManager.currentAudio?.apply {
            val duration = durationMillis()
            if (forwardTo > duration) {
                seekTo(duration)
            } else {
                seekTo(forwardTo)
            }
        }
        logEvent("fastForward")
    }

    override fun rewind() {
        val rewindTo = mediaSession.position() - DEFAULT_FORWARD_REWIND
        if (rewindTo < 0) {
            seekTo(0)
        } else {
            seekTo(rewindTo)
        }
        logEvent("rewind")
    }

    override suspend fun nextAudio(): String? {
        val index = queueManager.nextAudioIndex
        if (index != null) {
            val id = queueManager.queue[index]
            playAudio(id, index)
            logEvent("nextAudio")
            return id
        }
        return null
    }

    override suspend fun previousAudio() {
        if (queueManager.queue.isNotEmpty())
            queueManager.previousAudioIndex?.let {
                playAudio(queueManager.queue[it], it)
                logEvent("previousAudio")
            } ?: repeatAudio()
    }

    override suspend fun repeatAudio() {
        playAudio(queueManager.currentAudioId)
        logEvent("repeatAudio")
    }

    override suspend fun repeatQueue() {
        if (queueManager.queue.isNotEmpty()) {
            if (queueManager.currentAudioId == queueManager.queue.last())
                playAudio(queueManager.queue.first())
            else {
                nextAudio()
            }
            logEvent("repeatQueue")
        }
    }

    override fun playNext(id: String) {
        if (queueManager.queue.isEmpty()) {
            launch {
                setDataFromMediaId(MediaId(MEDIA_TYPE_AUDIO, id).toString())
            }
        } else {
            queueManager.playNext(id)
        }
        logEvent("playNext", id)
    }

    override fun removeFromQueue(position: Int) {
        queueManager.remove(position)
        logEvent("removeFromQueue", "position=$position")
    }

    override fun removeFromQueue(id: String) {
        queueManager.remove(id)
        logEvent("removeFromQueue", id)
    }

    override fun swapQueueAudios(from: Int, to: Int) {
        queueManager.swap(from, to)
        queueManager.currentAudio?.apply { setMetaData(this) }
        logEvent("nextAudio")
    }

    override fun stop(byUser: Boolean) {
        updatePlaybackState {
            setState(if (byUser) STATE_NONE else STATE_STOPPED, 0, 1F)
        }
        isInitialized = false
        audioPlayer.stop()
        isPlayingCallback(false, byUser)
        queueManager.clear()
        launch { saveQueueState() }
    }

    override fun release() {
        mediaSession.apply {
            isActive = false
            release()
        }
        audioPlayer.release()
        queueManager.clear()
    }

    override fun setPlaybackSpeed(speed: Float) {
        audioPlayer.setPlaybackSpeed(speed)
    }

    override fun onPlayingState(playing: OnIsPlaying<SarahangPlayer>) {
        this.isPlayingCallback = playing
    }

    override fun onPrepared(prepared: OnPrepared<SarahangPlayer>) {
        this.preparedCallback = prepared
    }

    override fun onError(error: OnError<SarahangPlayer>) {
        this.errorCallback = error
    }

    override fun onCompletion(completion: OnCompletion<SarahangPlayer>) {
        this.completionCallback = completion
    }

    override fun onMetaDataChanged(metaDataChanged: OnMetaDataChanged) {
        this.metaDataChangedCallback = metaDataChanged
    }

    override fun updatePlaybackState(applier: PlaybackStateCompat.Builder.() -> Unit) {
        applier(stateBuilder)
        stateBuilder.setExtras(
            stateBuilder.build().extras + bundleOf(
                QUEUE_CURRENT_INDEX to queueManager.currentAudioIndex,
                QUEUE_HAS_PREVIOUS to (queueManager.previousAudioIndex != null),
                QUEUE_HAS_NEXT to (queueManager.nextAudioIndex != null)
            )
        )
        setPlaybackState(stateBuilder.build())
    }

    override fun setPlaybackState(state: PlaybackStateCompat) {
        mediaSession.setPlaybackState(state)
        state.extras?.let { bundle ->
            mediaSession.setRepeatMode(bundle.getInt(REPEAT_MODE))
            mediaSession.setShuffleMode(bundle.getInt(SHUFFLE_MODE))
        }
    }

    override fun setShuffleMode(shuffleMode: Int) {
        val bundle = mediaSession.controller.playbackState.extras ?: Bundle()
        setPlaybackState(
            PlaybackStateCompat.Builder(mediaSession.controller.playbackState)
                .setExtras(
                    bundle.apply {
                        putInt(SHUFFLE_MODE, shuffleMode)
                    }
                ).build()
        )
        shuffleQueue(shuffleMode != SHUFFLE_MODE_NONE)
    }

    override fun updateData(list: List<String>, title: String?) {
        if (mediaSession.shuffleMode == SHUFFLE_MODE_NONE)
            if (title == queueManager.queueTitle) {
                queueManager.queue = list
                queueManager.queueTitle = title
                queueManager.currentAudio?.apply { setMetaData(this) }
            }
    }

    override fun setData(list: List<String>, title: String?) {
        // reset shuffle mode on new data
        getSession().setShuffleMode(SHUFFLE_MODE_NONE)
        updatePlaybackState {
            setExtras(bundleOf(SHUFFLE_MODE to SHUFFLE_MODE_NONE))
        }

        queueManager.queue = list
        queueManager.queueTitle = title ?: ""
    }

    override suspend fun setDataFromMediaId(_mediaId: String, extras: Bundle) {
        val mediaId = _mediaId.toMediaId()
        var audioId = extras.getString(QUEUE_MEDIA_ID_KEY) ?: mediaId.value
        var queue = extras.getStringArray(QUEUE_LIST_KEY)?.toList()
        var queueTitle = extras.getString(QUEUE_TITLE_KEY)
        val seekTo = extras.getLong(SEEK_TO)

        if (seekTo > 0) seekTo(seekTo)

        if (queue == null)
            queue = mediaQueueBuilder.buildAudioList(mediaId).map { it.id }

        logger.d("setDataFromMediaId: $mediaId, queue: $queue, queueTitle: $queueTitle")

        if (queueTitle.isNullOrBlank())
            queueTitle = mediaQueueBuilder.buildQueueTitle(mediaId).toString()

        if (queue.isNotEmpty()) {
            with(queue) {
                when {
                    mediaId.isShuffleIndex -> audioId = shuffled().first()
                    mediaId.hasIndex -> audioId =
                        if (mediaId.index < size) get(mediaId.index) else first()
                }
            }

            setData(queue, queueTitle)
            playAudio(
                id = audioId,
                index = if (mediaId.hasIndex) mediaId.index else queue.indexOf(audioId),
                seekTo = seekTo
            )
            if (mediaId.isShuffleIndex) setShuffleMode(SHUFFLE_MODE_ALL)
            // delay for new queue to apply first
            delay(2000)
            saveQueueState()
        } else {
            logger.e("Queue is null or empty: $mediaId")
        }

        logEvent("playMedia", _mediaId)
    }

    override suspend fun saveQueueState() {
        val mediaSession = getSession()
        val controller = mediaSession.controller
        if (controller == null || controller.playbackState == null) {
            logger.d("Not saving queue state")
            return
        }

        val queueState = QueueState(
            queue = queueManager.queue,
            currentIndex = queueManager.currentAudioIndex,
            seekPosition = controller.playbackState?.position ?: 0,
            repeatMode = controller.repeatMode,
            shuffleMode = controller.shuffleMode,
            state = controller.playbackState?.state ?: PlaybackState.STATE_NONE,
            title = controller.queueTitle?.toString()
        )

        logger.d("Saving queue state: idx=${queueState.currentIndex}, size=${queueState.queue.size}, title=${queueState.title}")
        preferences.save(queueStateKey, queueState, QueueState.serializer())
    }

    override suspend fun restoreQueueState() {
        logger.d("Restoring queue state")
        var queueState =
            preferences.get(queueStateKey, QueueState.serializer(), QueueState(emptyList())).first()
        logger.d("Restoring state: ${queueState.currentIndex}, size=${queueState.queue.size}")

        if (queueState.state in listOf(
                STATE_PLAYING,
                STATE_BUFFERING,
                STATE_BUFFERING,
                STATE_ERROR
            )
        ) {
            queueState = queueState.copy(state = STATE_PAUSED)
        }

        if (queueState.queue.isNotEmpty()) {
            setCurrentAudioId(queueState.queue[queueState.currentIndex], queueState.currentIndex)

            setData(queueState.queue, queueState.title ?: "")

            queueManager.refreshCurrentAudio()?.apply {
                logger.d("Setting metadata from saved state: currentAudio=$id")
                setMetaData(this)
            }
        }

        val extras = bundleOf(
            REPEAT_MODE to queueState.repeatMode,
            SHUFFLE_MODE to SHUFFLE_MODE_NONE
        )

        updatePlaybackState {
            setState(queueState.state, queueState.seekPosition, 1F)
            setExtras(extras)
        }
    }

    override fun clearRandomAudioPlayed() {
        queueManager.clearPlayedAudios()
    }

    override fun setCurrentAudioId(audioId: String, index: Int?) {
        val audioIndex = index ?: queueManager.queue.indexOfFirst { it == audioId }
        if (audioIndex < 0) {
            error("Audio id isn't in the queue, what it do?")
        } else queueManager.currentAudioIndex = audioIndex
    }

    override fun shuffleQueue(isShuffle: Boolean) {
        launch {
            queueManager.shuffleQueue(isShuffle)
            queueManager.currentAudio?.apply { setMetaData(this) }
            updatePlaybackState {
                setState(mediaSession.controller.playbackState.state, mediaSession.position(), 1F)
            }
            logEvent("shuffleQueue")
        }
    }

    private fun goToStart() {
        isInitialized = false

        stop(byUser = false)

        if (queueManager.queue.isEmpty()) return

        launch {
            setCurrentAudioId(queueManager.queue.first())
            queueManager.refreshCurrentAudio()?.apply { setMetaData(this) }
        }
    }

    @Suppress("SENSELESS_COMPARISON")
    private fun setMetaData(audio: Audio) {
        val player = this
        launch {
            val mediaMetadata = audio.toMediaMetadata(metadataBuilder).apply {
                val artworkFromFile = null
                if (artworkFromFile != null) {
                    putBitmap(METADATA_KEY_ALBUM_ART, artworkFromFile)
                }
            }
            mediaSession.setMetadata(mediaMetadata.build())
            metaDataChangedCallback(player)

            // cover image is applied separately to avoid delaying metadata setting while fetching bitmap from network
            val smallCoverBitmap = context.getBitmap(audio.coverImage.toString().toUri(), 1200)
            val updatedMetadata =
                mediaMetadata.apply { putBitmap(METADATA_KEY_ALBUM_ART, smallCoverBitmap) }.build()
            mediaSession.setMetadata(updatedMetadata)
            metaDataChangedCallback(player)
        }
    }

    private fun logEvent(event: String, mediaId: String = queueManager.currentAudioId) =
        playerEventLogger.logEvent("player_$event", mapOf("mediaId" to mediaId))
}

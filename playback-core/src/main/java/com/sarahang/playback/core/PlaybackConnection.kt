package com.sarahang.playback.core

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.sarahang.playback.core.apis.AudioDataSource
import com.sarahang.playback.core.models.Audio
import com.sarahang.playback.core.models.MEDIA_TYPE_AUDIO
import com.sarahang.playback.core.models.MEDIA_TYPE_AUDIO_QUERY
import com.sarahang.playback.core.models.MediaId
import com.sarahang.playback.core.models.PlaybackModeState
import com.sarahang.playback.core.models.PlaybackProgressState
import com.sarahang.playback.core.models.PlaybackQueue
import com.sarahang.playback.core.models.QueueTitle
import com.sarahang.playback.core.models.toMediaAudioIds
import com.sarahang.playback.core.models.toMediaId
import com.sarahang.playback.core.players.AudioPlayer
import com.sarahang.playback.core.players.QUEUE_FROM_POSITION_KEY
import com.sarahang.playback.core.players.QUEUE_LIST_KEY
import com.sarahang.playback.core.players.QUEUE_MEDIA_ID_KEY
import com.sarahang.playback.core.players.QUEUE_TITLE_KEY
import com.sarahang.playback.core.players.QUEUE_TO_POSITION_KEY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

const val PLAYBACK_PROGRESS_INTERVAL = 1000L

interface PlaybackConnection {
    val isConnected: StateFlow<Boolean>
    val playbackState: StateFlow<PlaybackStateCompat>
    val nowPlaying: StateFlow<MediaMetadataCompat>
    val nowPlayingAudio: StateFlow<PlaybackQueue.NowPlayingAudio?>

    val playbackQueue: StateFlow<PlaybackQueue>

    val playbackProgress: StateFlow<PlaybackProgressState>
    val playbackMode: StateFlow<PlaybackModeState>

    var mediaController: MediaControllerCompat?
    val transportControls: MediaControllerCompat.TransportControls?

    fun playAudio(audio: Audio, title: QueueTitle = QueueTitle())
    fun playNextAudio(audio: Audio)
    fun playAudios(itemId: String)
    fun playAudios(audios: List<Audio>, index: Int = 0, title: QueueTitle = QueueTitle())
    fun playWithQuery(query: String, audioId: String)

    fun addToQueue(audios: List<Audio>, title: QueueTitle = QueueTitle())
    fun swapQueue(from: Int, to: Int)

    fun removeByPosition(position: Int)
    fun removeById(id: String)
}

class PlaybackConnectionImpl(
    context: Context,
    serviceComponent: ComponentName,
    private val audioPlayer: AudioPlayer,
    private val audioDataSource: AudioDataSource,
    private val coroutineScope: CoroutineScope = ProcessLifecycleOwner.get().lifecycleScope,
) : PlaybackConnection, CoroutineScope by coroutineScope {

    override val isConnected = MutableStateFlow(false)
    override val playbackState = MutableStateFlow(NONE_PLAYBACK_STATE)
    override val nowPlaying = MutableStateFlow(NONE_PLAYING)
    private val playbackQueueState = MutableStateFlow(PlaybackQueue())

    override val playbackQueue = combine(nowPlaying, playbackState, playbackQueueState, ::Triple)
        .map(::buildPlaybackQueue)
        .distinctUntilChanged()
        .stateIn(this, SharingStarted.WhileSubscribed(5000), PlaybackQueue())

    override val nowPlayingAudio = combine(playbackQueue, playbackState, ::Pair)
        .map { (queue, playbackState) ->
            when (queue.isIndexValid && queue.isValid && !playbackState.isIdle) {
                true -> PlaybackQueue.NowPlayingAudio.from(queue)
                else -> null
            }
        }
        .distinctUntilChanged()
        .stateIn(this, SharingStarted.WhileSubscribed(5000), null)


    private var playbackProgressInterval: Job = Job()
    override val playbackProgress = MutableStateFlow(PlaybackProgressState())

    override val playbackMode = MutableStateFlow(PlaybackModeState())

    override var mediaController: MediaControllerCompat? = null
    override val transportControls get() = mediaController?.transportControls
    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)
    private val mediaBrowser = MediaBrowserCompat(
        context,
        serviceComponent,
        mediaBrowserConnectionCallback,
        null
    ).apply { connect() }

    init {
        startPlaybackProgress()
    }

    private fun startPlaybackProgress() = launch {
        combine(playbackState, nowPlaying, ::Pair).collectLatest { (state, current) ->
            playbackProgressInterval.cancel()
            val duration = current.duration
            val position = state.position

            if (state == NONE_PLAYBACK_STATE || current == NONE_PLAYING || duration < 1)
                return@collectLatest

            val initial = PlaybackProgressState(
                total = duration,
                position = position,
                buffered = audioPlayer.bufferedPosition()
            )
            playbackProgress.value = initial

            if (state.isPlaying && !state.isBuffering)
                starPlaybackProgressInterval(initial)
        }
    }

    /**
     * Resolves audios from given playback queue ids and validates current queues index.
     */
    private suspend fun buildPlaybackQueue(
        data: Triple<MediaMetadataCompat, PlaybackStateCompat, PlaybackQueue>
    ): PlaybackQueue {
        val (nowPlaying, state, queue) = data
        val nowPlayingId = nowPlaying.id.toMediaId().value
        val audios = audioDataSource.getByIds(queue.ids.toMediaAudioIds())

        return queue.copy(audios = audios, currentIndex = state.currentIndex).let {
            // check if now playing id and current audio's id by index matches
            val synced = when {
                it.isEmpty() -> false
                state.currentIndex >= it.size -> false
                else -> nowPlayingId == it[state.currentIndex].id
            }

            // if not, try to override current index by finding audio via now playing id
            when (synced) {
                true -> it
                else -> it.copy(currentIndex = it.indexOfFirst { a -> a.id == nowPlayingId })
            }
        }
    }

    private fun starPlaybackProgressInterval(initial: PlaybackProgressState) {
        playbackProgressInterval = launch {
            flowInterval(PLAYBACK_PROGRESS_INTERVAL).collect { ticks ->
                val elapsed = PLAYBACK_PROGRESS_INTERVAL * (ticks + 1)
                playbackProgress.value =
                    initial.copy(elapsed = elapsed, buffered = audioPlayer.bufferedPosition())
            }
        }
    }

    override fun playAudio(audio: Audio, title: QueueTitle) {
        playAudios(audios = listOf(audio), index = 0, title = title)
    }

    override fun playAudios(itemId: String) {
        coroutineScope.launch {
            val audios = audioDataSource.findAudiosByItemId(itemId)
            playAudios(audios)
        }
    }

    override fun playAudios(audios: List<Audio>, index: Int, title: QueueTitle) {
        val audiosIds = audios.map { it.id }.toTypedArray()
        val audio = audios[index]
        transportControls?.playFromMediaId(
            MediaId(MEDIA_TYPE_AUDIO, audio.id).toString(),
            Bundle().apply {
                putStringArray(QUEUE_LIST_KEY, audiosIds)
                putString(QUEUE_TITLE_KEY, title.toString())
            }
        )
    }

    override fun addToQueue(audios: List<Audio>, title: QueueTitle) {
        val audiosIds = audios.map { it.id }.toTypedArray()
        val audio = audios[0]
        transportControls?.prepareFromMediaId(
            MediaId(MEDIA_TYPE_AUDIO, audio.id).toString(),
            Bundle().apply {
                putStringArray(QUEUE_LIST_KEY, audiosIds)
                putString(QUEUE_TITLE_KEY, title.toString())
            }
        )
    }

    override fun playNextAudio(audio: Audio) {
        transportControls?.sendCustomAction(
            PLAY_NEXT,
            bundleOf(
                QUEUE_MEDIA_ID_KEY to audio.id
            )
        )
    }

    override fun playWithQuery(query: String, audioId: String) {
        transportControls?.playFromMediaId(
            MediaId(MEDIA_TYPE_AUDIO_QUERY, query, -1).toString(),
            bundleOf(
                QUEUE_MEDIA_ID_KEY to audioId
            )
        )
    }

    override fun swapQueue(from: Int, to: Int) {
        transportControls?.sendCustomAction(
            SWAP_ACTION,
            bundleOf(
                QUEUE_FROM_POSITION_KEY to from,
                QUEUE_TO_POSITION_KEY to to,
            )
        )
    }

    override fun removeByPosition(position: Int) {
        transportControls?.sendCustomAction(
            REMOVE_QUEUE_ITEM_BY_POSITION,
            bundleOf(
                QUEUE_FROM_POSITION_KEY to position,
            )
        )
    }

    override fun removeById(id: String) {
        transportControls?.sendCustomAction(
            REMOVE_QUEUE_ITEM_BY_ID,
            bundleOf(
                QUEUE_MEDIA_ID_KEY to id,
            )
        )
    }

    private inner class MediaBrowserConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }

            isConnected.value = true
        }

        override fun onConnectionSuspended() {
            isConnected.value = false
        }

        override fun onConnectionFailed() {
            isConnected.value = false
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            playbackState.value = state ?: return
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            nowPlaying.value = metadata ?: return
        }

        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
//            val newQueue = fromMediaController(mediaController ?: return)

            val newQueue = PlaybackQueue(
                title = mediaController!!.queueTitle?.toString(),
                ids = mediaController!!.queue.mapNotNull { it.description.mediaId },
                initialMediaId = mediaController!!.metadata?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
                    ?: "",
                currentIndex = mediaController!!.playbackState.currentIndex
            )
            Timber.d("Controller $mediaController")
            Timber.d("Old queue: size=${queue?.size} ${queue?.joinToString()}")
            Timber.d("New queue: size=${newQueue?.size} ${newQueue?.joinToString()}")
            this@PlaybackConnectionImpl.playbackQueueState.value = newQueue
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            playbackMode.value = playbackMode.value.copy(repeatMode = repeatMode)
        }

        override fun onShuffleModeChanged(shuffleMode: Int) {
            playbackMode.value = playbackMode.value.copy(shuffleMode = shuffleMode)
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}

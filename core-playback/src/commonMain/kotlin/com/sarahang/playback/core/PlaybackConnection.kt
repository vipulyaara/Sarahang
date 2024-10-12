package com.sarahang.playback.core

import com.sarahang.playback.core.models.Audio
import com.sarahang.playback.core.models.MediaMetadata
import com.sarahang.playback.core.models.PlaybackModeState
import com.sarahang.playback.core.models.PlaybackProgressState
import com.sarahang.playback.core.models.PlaybackQueue
import com.sarahang.playback.core.models.QueueTitle
import com.sarahang.playback.core.models.PlaybackState
import kotlinx.coroutines.flow.StateFlow

interface PlaybackConnection {
    val isConnected: StateFlow<Boolean>
    val nowPlayingAudio: StateFlow<PlaybackQueue.NowPlayingAudio?>

    val playbackState: StateFlow<PlaybackState>
    val nowPlaying: StateFlow<MediaMetadata>
    val playbackMode: StateFlow<PlaybackModeState>

    val playbackQueue: StateFlow<PlaybackQueue>

    val playbackProgress: StateFlow<PlaybackProgressState>

    fun sendCustomAction(action: String, extras: Map<String, Any?>?)

    fun playAudio(audio: Audio, title: QueueTitle = QueueTitle())
    fun playNextAudio(audio: Audio)
    fun playAlbum(albumId: String, index: Int = 0, timestamp: Long? = null)
    fun playAudios(audios: List<Audio>, index: Int = 0, title: QueueTitle = QueueTitle())
    fun playWithQuery(query: String, audioId: String)
    fun playPause()
    fun stop()
    fun toggleRepeatMode()
    fun toggleShuffleMode()

    fun swapQueue(from: Int, to: Int)
    fun skipToQueueItem(index: Int)

    fun seekTo(position: Long)

    fun fastForward()
    fun rewind()
    fun skipToNext()
    fun skipToPrevious()

    fun removeByPosition(position: Int)
    fun removeById(id: String)
}

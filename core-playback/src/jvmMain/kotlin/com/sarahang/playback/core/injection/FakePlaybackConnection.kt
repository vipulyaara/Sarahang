package com.sarahang.playback.core.injection

import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.models.Audio
import com.sarahang.playback.core.models.MediaMetadata
import com.sarahang.playback.core.models.PlaybackModeState
import com.sarahang.playback.core.models.PlaybackProgressState
import com.sarahang.playback.core.models.PlaybackQueue
import com.sarahang.playback.core.models.PlaybackState
import com.sarahang.playback.core.models.QueueTitle
import kotlinx.coroutines.flow.StateFlow

class FakePlaybackConnection : PlaybackConnection {
    override val isConnected: StateFlow<Boolean>
        get() = TODO("Not yet implemented")
    override val nowPlayingAudio: StateFlow<PlaybackQueue.NowPlayingAudio?>
        get() = TODO("Not yet implemented")
    override val playbackState: StateFlow<PlaybackState>
        get() = TODO("Not yet implemented")
    override val nowPlaying: StateFlow<MediaMetadata>
        get() = TODO("Not yet implemented")
    override val playbackMode: StateFlow<PlaybackModeState>
        get() = TODO("Not yet implemented")
    override val playbackQueue: StateFlow<PlaybackQueue>
        get() = TODO("Not yet implemented")
    override val playbackProgress: StateFlow<PlaybackProgressState>
        get() = TODO("Not yet implemented")

    override fun sendCustomAction(action: String, extras: Map<String, Any?>?) {
        TODO("Not yet implemented")
    }

    override fun playAudio(audio: Audio, title: QueueTitle) {
        TODO("Not yet implemented")
    }

    override fun playNextAudio(audio: Audio) {
        TODO("Not yet implemented")
    }

    override fun playAlbum(albumId: String, index: Int, timestamp: Long?) {
        TODO("Not yet implemented")
    }

    override fun playAudios(audios: List<Audio>, index: Int, title: QueueTitle) {
        TODO("Not yet implemented")
    }

    override fun playWithQuery(query: String, audioId: String) {
        TODO("Not yet implemented")
    }

    override fun playPause() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun toggleRepeatMode() {
        TODO("Not yet implemented")
    }

    override fun toggleShuffleMode() {
        TODO("Not yet implemented")
    }

    override fun swapQueue(from: Int, to: Int) {
        TODO("Not yet implemented")
    }

    override fun skipToQueueItem(index: Int) {
        TODO("Not yet implemented")
    }

    override fun seekTo(position: Long) {
        TODO("Not yet implemented")
    }

    override fun fastForward() {
        TODO("Not yet implemented")
    }

    override fun rewind() {
        TODO("Not yet implemented")
    }

    override fun skipToNext() {
        TODO("Not yet implemented")
    }

    override fun skipToPrevious() {
        TODO("Not yet implemented")
    }

    override fun removeByPosition(position: Int) {
        TODO("Not yet implemented")
    }

    override fun removeById(id: String) {
        TODO("Not yet implemented")
    }
}
package com.sarahang.playback.core.injection

import com.sarahang.playback.core.NONE_PLAYING_STATE
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.models.Audio
import com.sarahang.playback.core.models.MediaMetadata
import com.sarahang.playback.core.models.PlaybackModeState
import com.sarahang.playback.core.models.PlaybackProgressState
import com.sarahang.playback.core.models.PlaybackQueue
import com.sarahang.playback.core.models.PlaybackState
import com.sarahang.playback.core.models.QueueTitle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakePlaybackConnection : PlaybackConnection {
    override val isConnected: StateFlow<Boolean>
        get() {
            // todo: implement
            return MutableStateFlow(false)
        }
    override val nowPlayingAudio: StateFlow<PlaybackQueue.NowPlayingAudio?>
        get() {
            // todo: implement
            return MutableStateFlow(null)
        }
    override val playbackState: StateFlow<PlaybackState>
        get() {
            // todo: implement
            return MutableStateFlow(NONE_PLAYING_STATE)
        }
    override val nowPlaying: StateFlow<MediaMetadata>
        get() {
            // todo: implement
            return MutableStateFlow(MediaMetadata.NONE_PLAYING)
        }
    override val playbackMode: StateFlow<PlaybackModeState>
        get() {
            // todo: implement
            return MutableStateFlow(PlaybackModeState())
        }
    override val playbackQueue: StateFlow<PlaybackQueue>
        get() {
            // todo: implement
            return MutableStateFlow(PlaybackQueue())
        }
    override val playbackProgress: StateFlow<PlaybackProgressState>
        get() {
            // todo: implement
            return MutableStateFlow(PlaybackProgressState())
        }

    override fun sendCustomAction(action: String, extras: Map<String, Any?>?) {
        // todo: implement
    }

    override fun playAudio(audio: Audio, title: QueueTitle) {
        // todo: implement
    }

    override fun playNextAudio(audio: Audio) {
        // todo: implement
    }

    override fun playAlbum(albumId: String, index: Int, timestamp: Long?) {
        // todo: implement
    }

    override fun playAudios(audios: List<Audio>, index: Int, title: QueueTitle) {
        // todo: implement
    }

    override fun playWithQuery(query: String, audioId: String) {
        // todo: implement
    }

    override fun playPause() {
        // todo: implement
    }

    override fun stop() {
        // todo: implement
    }

    override fun toggleRepeatMode() {
        // todo: implement
    }

    override fun toggleShuffleMode() {
        // todo: implement
    }

    override fun swapQueue(from: Int, to: Int) {
        // todo: implement
    }

    override fun skipToQueueItem(index: Int) {
        // todo: implement
    }

    override fun seekTo(position: Long) {
        // todo: implement
    }

    override fun fastForward() {
        // todo: implement
    }

    override fun rewind() {
        // todo: implement
    }

    override fun skipToNext() {
        // todo: implement
    }

    override fun skipToPrevious() {
        // todo: implement
    }

    override fun removeByPosition(position: Int) {
        // todo: implement
    }

    override fun removeById(id: String) {
        // todo: implement
    }
}
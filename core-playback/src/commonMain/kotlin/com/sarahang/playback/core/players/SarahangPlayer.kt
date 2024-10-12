package com.sarahang.playback.core.players

import com.sarahang.playback.core.models.Audio
import kotlinx.coroutines.flow.StateFlow

typealias OnPrepared<T> = T.() -> Unit
typealias OnError<T> = T.(error: Throwable) -> Unit
typealias OnCompletion<T> = T.() -> Unit
typealias OnBuffering<T> = T.() -> Unit
typealias OnReady<T> = T.() -> Unit
typealias OnMetaDataChanged = SarahangPlayer.() -> Unit
typealias OnIsPlaying<T> = T.(playing: Boolean, byUi: Boolean) -> Unit

const val BY_UI_KEY = "by_ui_key"
const val PLAYBACK_PROGRESS_INTERVAL = 1000L

interface SarahangPlayer {
    fun playAudio(extras: Map<String, Any?> = mapOf(BY_UI_KEY to true))
    suspend fun playAudio(id: String, index: Int? = null, seekTo: Long? = null)
    suspend fun playAudio(audio: Audio, index: Int? = null, seekTo: Long? = null)
    fun seekTo(position: Long)
    fun fastForward()
    fun rewind()
    fun pause(extras: Map<String, Any?> = mapOf(BY_UI_KEY to true))
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
    fun setShuffleMode(shuffleMode: Int)
    fun updateData(list: List<String> = emptyList(), title: String? = null)
    fun setData(list: List<String> = emptyList(), title: String? = null)
    suspend fun setDataFromMediaId(_mediaId: String, extras: Map<String, Any?> = mapOf())
    suspend fun saveQueueState()
    suspend fun restoreQueueState()
    fun clearRandomAudioPlayed()
    fun setCurrentAudioId(audioId: String, index: Int? = null)
    fun shuffleQueue(isShuffle: Boolean)
    val playbackSpeed: StateFlow<Float>
    fun setPlaybackSpeed(speed: Float)
}

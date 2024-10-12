package com.sarahang.playback.core.injection

import com.sarahang.playback.core.models.Audio
import com.sarahang.playback.core.players.OnCompletion
import com.sarahang.playback.core.players.OnError
import com.sarahang.playback.core.players.OnIsPlaying
import com.sarahang.playback.core.players.OnMetaDataChanged
import com.sarahang.playback.core.players.OnPrepared
import com.sarahang.playback.core.players.SarahangPlayer
import kotlinx.coroutines.flow.StateFlow

class FakeSarahangPlayer : SarahangPlayer {
    override fun playAudio(extras: Map<String, Any?>) {
        TODO("Not yet implemented")
    }

    override suspend fun playAudio(id: String, index: Int?, seekTo: Long?) {
        TODO("Not yet implemented")
    }

    override suspend fun playAudio(audio: Audio, index: Int?, seekTo: Long?) {
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

    override fun pause(extras: Map<String, Any?>) {
        TODO("Not yet implemented")
    }

    override suspend fun nextAudio(): String? {
        TODO("Not yet implemented")
    }

    override suspend fun repeatAudio() {
        TODO("Not yet implemented")
    }

    override suspend fun repeatQueue() {
        TODO("Not yet implemented")
    }

    override suspend fun previousAudio() {
        TODO("Not yet implemented")
    }

    override fun playNext(id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun skipTo(position: Int) {
        TODO("Not yet implemented")
    }

    override fun removeFromQueue(position: Int) {
        TODO("Not yet implemented")
    }

    override fun removeFromQueue(id: String) {
        TODO("Not yet implemented")
    }

    override fun swapQueueAudios(from: Int, to: Int) {
        TODO("Not yet implemented")
    }

    override fun stop(byUser: Boolean) {
        TODO("Not yet implemented")
    }

    override fun release() {
        TODO("Not yet implemented")
    }

    override fun onPlayingState(playing: OnIsPlaying<SarahangPlayer>) {
        TODO("Not yet implemented")
    }

    override fun onPrepared(prepared: OnPrepared<SarahangPlayer>) {
        TODO("Not yet implemented")
    }

    override fun onError(error: OnError<SarahangPlayer>) {
        TODO("Not yet implemented")
    }

    override fun onCompletion(completion: OnCompletion<SarahangPlayer>) {
        TODO("Not yet implemented")
    }

    override fun onMetaDataChanged(metaDataChanged: OnMetaDataChanged) {
        TODO("Not yet implemented")
    }

    override fun setShuffleMode(shuffleMode: Int) {
        TODO("Not yet implemented")
    }

    override fun updateData(list: List<String>, title: String?) {
        TODO("Not yet implemented")
    }

    override fun setData(list: List<String>, title: String?) {
        TODO("Not yet implemented")
    }

    override suspend fun setDataFromMediaId(_mediaId: String, extras: Map<String, Any?>) {
        TODO("Not yet implemented")
    }

    override suspend fun saveQueueState() {
        TODO("Not yet implemented")
    }

    override suspend fun restoreQueueState() {
        TODO("Not yet implemented")
    }

    override fun clearRandomAudioPlayed() {
        TODO("Not yet implemented")
    }

    override fun setCurrentAudioId(audioId: String, index: Int?) {
        TODO("Not yet implemented")
    }

    override fun shuffleQueue(isShuffle: Boolean) {
        TODO("Not yet implemented")
    }

    override val playbackSpeed: StateFlow<Float>
        get() = TODO("Not yet implemented")

    override fun setPlaybackSpeed(speed: Float) {
        TODO("Not yet implemented")
    }
}
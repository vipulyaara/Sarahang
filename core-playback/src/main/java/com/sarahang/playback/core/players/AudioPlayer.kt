package com.sarahang.playback.core.players

import android.app.Application
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.PriorityTaskManager
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.kafka.base.ApplicationScope
import com.kafka.base.Named
import com.sarahang.playback.core.apis.Logger
import me.tatarka.inject.annotations.Inject
import okhttp3.OkHttpClient

/**
 * Lower level player that handles the playback of a single audio file.
 * This is used by higher level players to interact with [ExoPlayer].
 * */
interface AudioPlayer {
    fun play(startAtPosition: Long? = null)
    fun setSource(uri: Uri, local: Boolean = false): Boolean
    fun prepare()
    fun seekTo(position: Long)
    fun duration(): Long
    fun isPrepared(): Boolean
    fun isBuffering(): Boolean
    fun isPlaying(): Boolean
    fun position(): Long
    fun bufferedPosition(): Long
    fun pause()
    fun stop()
    fun release()
    fun setPlaybackSpeed(speed: Float)
    fun onPrepared(prepared: OnPrepared<AudioPlayer>)
    fun onError(error: OnError<AudioPlayer>)
    fun onBuffering(buffering: OnBuffering<AudioPlayer>)
    fun onIsPlaying(playing: OnIsPlaying<AudioPlayer>)
    fun onReady(ready: OnReady<AudioPlayer>)
    fun onCompletion(completion: OnCompletion<AudioPlayer>)
    fun onPlaybackParamsChanged(playbackParameters: OnPlaybackParametersChanged<AudioPlayer>)
    fun playWhenReady(): Boolean
}

@ApplicationScope
@Inject
class AudioPlayerImpl(
    internal val context: Application,
    @Named("player") private val okHttpClient: OkHttpClient,
    private val logger: Logger,
) : AudioPlayer, Player.Listener {

    private var playerBase: ExoPlayer? = null
    private val player: ExoPlayer
        get() {
            if (playerBase == null) {
                playerBase = createPlayer(this)
            }
            return playerBase ?: throw IllegalStateException("Could not create an audio player")
        }

    private var isPrepared = false
    private var isBuffering = false
    private var onPrepared: OnPrepared<AudioPlayer> = { logger.d("Prepared") }
    private var onError: OnError<AudioPlayer> = { logger.e(it) }
    private var onBuffering: OnBuffering<AudioPlayer> = { logger.d("Buffering") }
    private var onIsPlaying: OnIsPlaying<AudioPlayer> =
        { playing, byUi -> logger.d("$playing $byUi") }
    private var onReady: OnReady<AudioPlayer> = {}
    private var onCompletion: OnCompletion<AudioPlayer> = {}
    private var onPlaybackParamsChanged: OnPlaybackParametersChanged<AudioPlayer> = {}

    override fun play(startAtPosition: Long?) {
        if (startAtPosition == null) {
            player.playWhenReady = true
            return
        }
        player.seekTo(startAtPosition)
        player.playWhenReady = true
    }

    @OptIn(UnstableApi::class)
    override fun setSource(uri: Uri, local: Boolean): Boolean {
        logger.d("Setting source: local=$local, uri=$uri")
        return try {
            if (local) {
                player.setMediaItem(MediaItem.fromUri(uri), true)
            } else {
                val mediaSource =
                    ProgressiveMediaSource.Factory(OkHttpDataSource.Factory(okHttpClient))
                        .createMediaSource(MediaItem.fromUri(uri))
                player.setMediaSource(mediaSource, true)
            }
            true
        } catch (ex: Exception) {
            onError(this, ex)
            false
        }
    }

    override fun prepare() {
        player.prepare()
    }

    override fun seekTo(position: Long) {
        player.seekTo(position)
    }

    override fun duration() = player.duration

    override fun isPrepared() = isPrepared

    override fun isBuffering() = isBuffering

    override fun isPlaying() = player.isPlaying

    override fun playWhenReady() = player.playWhenReady

    override fun position() = player.currentPosition

    override fun bufferedPosition() = player.bufferedPosition

    override fun pause() {
        player.playWhenReady = false
    }

    override fun stop() {
        player.stop()
    }

    override fun release() {
        player.release()
    }

    override fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
    }

    override fun onPrepared(prepared: OnPrepared<AudioPlayer>) {
        this.onPrepared = prepared
    }

    override fun onError(error: OnError<AudioPlayer>) {
        this.onError = error
    }

    override fun onBuffering(buffering: OnBuffering<AudioPlayer>) {
        this.onBuffering = buffering
    }

    override fun onIsPlaying(playing: OnIsPlaying<AudioPlayer>) {
        this.onIsPlaying = playing
    }

    override fun onReady(ready: OnReady<AudioPlayer>) {
        this.onReady = ready
    }

    override fun onCompletion(completion: OnCompletion<AudioPlayer>) {
        this.onCompletion = completion
    }

    override fun onPlaybackStateChanged(state: Int) {
        super.onPlaybackStateChanged(state)
        when (state) {
            Player.STATE_IDLE -> isBuffering = false
            Player.STATE_BUFFERING -> {
                isBuffering = true
                onBuffering(this)
            }

            Player.STATE_READY -> {
                isBuffering = false
                onReady(this)
            }

            Player.STATE_ENDED -> {
                isBuffering = false
                onCompletion(this)
            }

            else -> Unit
        }
    }

    override fun onPlaybackParamsChanged(playbackParameters: OnPlaybackParametersChanged<AudioPlayer>) {
        this.onPlaybackParamsChanged = playbackParameters
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
        super.onPlaybackParametersChanged(playbackParameters)
        this.onPlaybackParamsChanged(playbackParameters)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        onIsPlaying(isPlaying, false)
    }

    override fun onPlayerError(error: PlaybackException) {
        isPrepared = false
        onError(this, error)
    }

    @OptIn(UnstableApi::class)
    private fun createPlayer(owner: AudioPlayerImpl): ExoPlayer {
        return ExoPlayer.Builder(context, DefaultRenderersFactory(context).apply {
            setExtensionRendererMode(EXTENSION_RENDERER_MODE_PREFER)
        }).setLoadControl(object : DefaultLoadControl() {
            override fun onPrepared() {
                isPrepared = true
                onPrepared(owner)
            }
        }).build().apply {
            val attr = AudioAttributes.Builder().apply {
                setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                setUsage(C.USAGE_MEDIA)
            }.build()

            setAudioAttributes(attr, false)
            setPriorityTaskManager(PriorityTaskManager())
            addListener(owner)
        }
    }
}

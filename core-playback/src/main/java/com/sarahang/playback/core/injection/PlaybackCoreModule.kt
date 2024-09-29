package com.sarahang.playback.core.injection

import android.app.Application
import android.content.ComponentName
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.PlaybackConnectionImpl
import com.sarahang.playback.core.apis.AudioDataSource
import com.sarahang.playback.core.apis.Logger
import com.sarahang.playback.core.audio.AudioFocusHelper
import com.sarahang.playback.core.audio.AudioFocusHelperImpl
import com.sarahang.playback.core.players.AudioPlayer
import com.sarahang.playback.core.players.AudioPlayerImpl
import com.sarahang.playback.core.players.SarahangPlayer
import com.sarahang.playback.core.players.SarahangPlayerImpl
import com.sarahang.playback.core.services.PlayerService
import com.sarahang.playback.core.timer.SleepTimer
import com.sarahang.playback.core.timer.SleepTimerImpl
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.kafka.base.ApplicationScope
import org.kafka.base.Named
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Component
@ApplicationScope
interface PlaybackCoreModule {
    @Provides
    @ApplicationScope
    fun okHttpCache(app: Application) = Cache(app.cacheDir, (10 * 1024 * 1024).toLong())

    @Provides
    @Named("player")
    fun playerOkHttp(
        cache: Cache,
    ): OkHttpClient = getBaseBuilder(cache)
        .readTimeout(PLAYER_TIMEOUT, TimeUnit.MILLISECONDS)
        .writeTimeout(PLAYER_TIMEOUT, TimeUnit.MILLISECONDS)
        .connectTimeout(PLAYER_TIMEOUT_CONNECT, TimeUnit.MILLISECONDS)
        .build()

    @Provides
    @ApplicationScope
    fun playbackConnection(
        context: Application,
        audioPlayer: AudioPlayerImpl,
        audioDataSource: AudioDataSource,
        logger: Logger,
    ): PlaybackConnection = PlaybackConnectionImpl(
        context = context,
        serviceComponent = ComponentName(context, PlayerService::class.java),
        audioPlayer = audioPlayer,
        audioDataSource = audioDataSource,
        logger = logger
    )

    @Provides
    fun provideAudioFocusHelper(bind: AudioFocusHelperImpl): AudioFocusHelper = bind

    @Provides
    fun provideAudioPlayer(bind: AudioPlayerImpl): AudioPlayer = bind

    @Provides
    fun provideSarahangPlayer(bind: SarahangPlayerImpl): SarahangPlayer = bind

    @Provides
    fun provideSleepTimer(bind: SleepTimerImpl): SleepTimer = bind
}

private val PLAYER_TIMEOUT = 2.minutes.inWholeMilliseconds
private val PLAYER_TIMEOUT_CONNECT = 30.seconds.inWholeMilliseconds

private fun getBaseBuilder(cache: Cache): OkHttpClient.Builder {
    return OkHttpClient.Builder()
        .cache(cache)
        .retryOnConnectionFailure(true)
}
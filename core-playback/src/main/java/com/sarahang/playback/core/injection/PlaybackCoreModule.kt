package com.sarahang.playback.core.injection

import android.app.Application
import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.PlaybackConnectionImpl
import com.sarahang.playback.core.apis.AudioDataSource
import com.sarahang.playback.core.audio.AudioFocusHelper
import com.sarahang.playback.core.audio.AudioFocusHelperImpl
import com.sarahang.playback.core.players.AudioPlayer
import com.sarahang.playback.core.players.AudioPlayerImpl
import com.sarahang.playback.core.players.SarahangPlayer
import com.sarahang.playback.core.players.SarahangPlayerImpl
import com.sarahang.playback.core.services.PlayerService
import com.sarahang.playback.core.timer.SleepTimer
import com.sarahang.playback.core.timer.SleepTimerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@InstallIn(SingletonComponent::class)
@Module
abstract class PlaybackCoreModule {

    companion object {
        @Provides
        @Named("process")
        fun processScope(): CoroutineScope = ProcessLifecycleOwner.get().lifecycleScope

        @Provides
        @Singleton
        fun okHttpCache(app: Application) = Cache(app.cacheDir, (10 * 1024 * 1024).toLong())

        @Provides
        @Named("player")
        fun playerOkHttp(
            cache: Cache,
        ) = getBaseBuilder(cache)
            .readTimeout(PLAYER_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(PLAYER_TIMEOUT, TimeUnit.MILLISECONDS)
            .connectTimeout(PLAYER_TIMEOUT_CONNECT, TimeUnit.MILLISECONDS)
            .build()

        @Provides
        @Singleton
        fun playbackConnection(
            @ApplicationContext context: Context,
            audioPlayer: AudioPlayerImpl,
            audioDataSource: AudioDataSource
        ): PlaybackConnection = PlaybackConnectionImpl(
            context = context,
            serviceComponent = ComponentName(context, PlayerService::class.java),
            audioPlayer = audioPlayer,
            audioDataSource = audioDataSource
        )
    }

    @Binds
    abstract fun provideAudioFocusHelper(bind: AudioFocusHelperImpl): AudioFocusHelper

    @Binds
    abstract fun provideAudioPlayer(bind: AudioPlayerImpl): AudioPlayer

    @Binds
    abstract fun provideSarahangPlayer(bind: SarahangPlayerImpl): SarahangPlayer

    @Binds
    abstract fun provideSleepTimer(bind: SleepTimerImpl): SleepTimer
}

private val PLAYER_TIMEOUT = 2.minutes.inWholeMilliseconds
private val PLAYER_TIMEOUT_CONNECT = 30.seconds.inWholeMilliseconds

private fun getBaseBuilder(cache: Cache): OkHttpClient.Builder {
    return OkHttpClient.Builder()
        .cache(cache)
        .retryOnConnectionFailure(true)
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
@MustBeDocumented
annotation class Player

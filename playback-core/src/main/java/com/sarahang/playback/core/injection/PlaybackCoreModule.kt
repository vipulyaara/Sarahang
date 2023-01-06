package com.sarahang.playback.core.injection

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.sarahang.playback.core.audio.AudioFocusHelper
import com.sarahang.playback.core.audio.AudioFocusHelperImpl
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.PlaybackConnectionImpl
import com.sarahang.playback.core.apis.AudioDataSource
import com.sarahang.playback.core.players.AudioPlayer
import com.sarahang.playback.core.players.AudioPlayerImpl
import com.sarahang.playback.core.players.SarahangPlayer
import com.sarahang.playback.core.players.SarahangPlayerImpl
import com.sarahang.playback.core.services.PlayerService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class PlaybackCoreModule {

    companion object {
        @Provides
        @Named("process")
        fun processScope(): CoroutineScope = ProcessLifecycleOwner.get().lifecycleScope

        @Player
        @Provides
        fun provideOkHttpClientPlayer(): OkHttpClient {
            val builder = OkHttpClient.Builder().apply {
                readTimeout(60, TimeUnit.SECONDS)
                connectTimeout(60, TimeUnit.SECONDS)
            }

            return builder.build()
        }

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
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
@MustBeDocumented
annotation class Player

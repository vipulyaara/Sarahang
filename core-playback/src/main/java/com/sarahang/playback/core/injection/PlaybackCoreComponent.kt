package com.sarahang.playback.core.injection

import android.app.Application
import android.content.ComponentName
import com.kafka.base.ApplicationScope
import com.kafka.base.Named
import com.sarahang.playback.core.MediaNotifications
import com.sarahang.playback.core.MediaNotificationsImpl
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.PlaybackConnectionImpl
import com.sarahang.playback.core.PreferencesStore
import com.sarahang.playback.core.apis.AudioDataSource
import com.sarahang.playback.core.apis.Logger
import com.sarahang.playback.core.audio.AudioFocusHelper
import com.sarahang.playback.core.audio.AudioFocusHelperImpl
import com.sarahang.playback.core.audio.AudioQueueManager
import com.sarahang.playback.core.audio.AudioQueueManagerImpl
import com.sarahang.playback.core.createDataStore
import com.sarahang.playback.core.players.AudioPlayer
import com.sarahang.playback.core.players.AudioPlayerImpl
import com.sarahang.playback.core.players.MediaSessionPlayer
import com.sarahang.playback.core.players.SarahangPlayer
import com.sarahang.playback.core.players.SarahangPlayerImpl
import com.sarahang.playback.core.services.PlayerService
import com.sarahang.playback.core.timer.SleepTimer
import com.sarahang.playback.core.timer.SleepTimerImpl
import me.tatarka.inject.annotations.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private const val dataStoreFileName = "sarahang_preferences.preferences_pb"

@ApplicationScope
actual interface PlaybackCoreComponent {
    @ApplicationScope
    @Provides
    fun providePlayerPreferencesStore(context: Application): PreferencesStore {
        val dataStore = createDataStore(
            producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath }
        )

        return PreferencesStore(dataStore)
    }

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
        audioPlayer: AudioPlayer,
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
    @ApplicationScope
    fun provideAudioFocusHelper(bind: AudioFocusHelperImpl): AudioFocusHelper = bind

    @Provides
    @ApplicationScope
    fun provideAudioPlayer(bind: AudioPlayerImpl): AudioPlayer = bind

    @Provides
    @ApplicationScope
    fun provideAudioQueueManager(bind: AudioQueueManagerImpl): AudioQueueManager = bind

    @Provides
    @ApplicationScope
    fun provideSarahangPlayer(bind: SarahangPlayerImpl): SarahangPlayer = bind

    @Provides
    @ApplicationScope
    fun provideMediaSessionPlayer(bind: SarahangPlayerImpl): MediaSessionPlayer = bind

    @Provides
    @ApplicationScope
    fun provideSleepTimer(bind: SleepTimerImpl): SleepTimer = bind

    @Provides
    @ApplicationScope
    fun provideMediaNotifications(bind: MediaNotificationsImpl): MediaNotifications = bind
}

private val PLAYER_TIMEOUT = 2.minutes.inWholeMilliseconds
private val PLAYER_TIMEOUT_CONNECT = 30.seconds.inWholeMilliseconds

private fun getBaseBuilder(cache: Cache): OkHttpClient.Builder {
    return OkHttpClient.Builder()
        .cache(cache)
        .retryOnConnectionFailure(true)
}
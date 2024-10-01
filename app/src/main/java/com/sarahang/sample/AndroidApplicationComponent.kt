package com.sarahang.sample

import android.app.Application
import android.content.Context
import com.sarahang.playback.core.PlaybackConnection
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import org.kafka.base.ApplicationScope

@Component
@ApplicationScope
abstract class AndroidApplicationComponent(
    @get:Provides val application: Application,
) : PlaybackModule {
    abstract val playbackConnection: PlaybackConnection

    companion object
}

internal fun AndroidApplicationComponent.Companion.from(context: Context): AndroidApplicationComponent {
    return (context.applicationContext as SarahangApplication).component
}

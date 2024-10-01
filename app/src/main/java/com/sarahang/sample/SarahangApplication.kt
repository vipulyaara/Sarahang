package com.sarahang.sample

import android.app.Application
import com.kafka.user.injection.create
import timber.log.Timber

class SarahangApplication : Application() {
    internal val component: AndroidApplicationComponent by lazy(LazyThreadSafetyMode.NONE) {
        AndroidApplicationComponent::class.create(this)
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}

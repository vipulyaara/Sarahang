package com.sarahang.sample

import android.app.Activity
import com.sarahang.sample.AndroidApplicationComponent
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import org.kafka.base.ActivityScope

@ActivityScope
@Component
abstract class AndroidActivityComponent(
    @get:Provides val activity: Activity,
    @Component val applicationComponent: AndroidApplicationComponent,
) {
    companion object
}

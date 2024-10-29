plugins {
    id("com.android.library")
    id("com.kafka.compose")
    id("com.kafka.kotlin.multiplatform")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.base.annotations)

                implementation(compose.components.resources)

                implementation(libs.coil3.compose)
                implementation(libs.kotlininject.runtime)

                implementation(libs.dataStore)
                implementation(libs.kotlin.serialization)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kermit)
            }
        }

        androidMain {
            dependencies {
                api(libs.media)

                implementation(libs.media3.datasource.okhttp)
                implementation(libs.media3.exoplayer)
                implementation(libs.media3.exoplayer.dash)

                implementation(libs.androidx.core)
                implementation(libs.androidx.palette)
                implementation(libs.androidx.lifecycle.process)
            }
        }
    }
}

android {
    namespace = "com.sarahang.playback.core"
}

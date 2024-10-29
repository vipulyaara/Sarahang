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
                implementation(project(":core-playback"))

                implementation(compose.components.resources)
                implementation(compose.foundation)
                implementation(compose.runtime)
                implementation(compose.material)
                implementation(compose.material3)

                implementation(libs.androidx.collection)
                implementation(libs.coil3.compose)
                implementation(libs.kotlininject.runtime)

                implementation(libs.dataStore)
                implementation(libs.jetbrains.adaptive)

                implementation(libs.icons.font.awesome)
                implementation(libs.icons.tabler)

                implementation(libs.kotlin.serialization)
                implementation(libs.kermit)
                implementation(libs.material.kolor)

                implementation(libs.jetbrains.lifecycle.runtime.compose)
                implementation(libs.jetbrains.lifecycle.viewmodel.compose)
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
    namespace = "com.sarahang.playback.ui"
}

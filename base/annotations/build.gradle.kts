plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
}


dependencies {
    api(libs.kotlininject.runtime)
}

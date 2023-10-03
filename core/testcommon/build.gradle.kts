plugins {
    id("routinetracker.android.library")
    id("routinetracker.android.koin")
}

android {
    namespace = "com.rendox.routinetracker.core.testcommon"

    defaultConfig {
        minSdk = 21
    }
}

dependencies {
    implementation(libs.androidx.runner)
}
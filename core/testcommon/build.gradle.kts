plugins {
    id("routinetracker.android.library")
    id("routinetracker.android.koin")
}

android {
    namespace = "com.rendox.routinetracker.core.testcommon"
}

dependencies {
    implementation(libs.androidx.runner)
}
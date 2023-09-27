plugins {
    id("routinetracker.android.feature")
    id("routinetracker.android.library.compose")
    id("routinetracker.android.koin")
    id("routinetracker.android.library.instrumentationtestrunner")
}

android {
    namespace = "com.rendox.routinetracker.feature.routinedetails"
}

dependencies {
    implementation(libs.jetbrains.kotlinx.datetime)
}
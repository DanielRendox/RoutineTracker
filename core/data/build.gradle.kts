plugins {
    id("routinetracker.android.library")
    id("routinetracker.android.koin")
    id("routinetracker.android.library.instrumentationtestrunner")
}

android {
    namespace = "com.rendox.routinetracker.core.data"
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:model"))

    implementation(libs.jetbrains.kotlinx.datetime)

    androidTestImplementation(libs.jetbrains.kotlinx.coroutines.test)
}
plugins {
    id("routinetracker.jvm.library")
}

dependencies {
    implementation(project(":core:logic"))

    implementation(libs.jetbrains.kotlinx.datetime)
}

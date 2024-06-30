plugins {
    id("routinetracker.jvm.library")
    id("routinetracker.lint")
}

dependencies {
    implementation(project(":core:logic"))

    implementation(libs.jetbrains.kotlinx.datetime)
}

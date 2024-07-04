plugins {
    id("routinetracker.jvm.library")
    id("routinetracker.lint")
}

dependencies {
    implementation(libs.jetbrains.kotlinx.datetime)
    implementation(project(":core:model"))
}


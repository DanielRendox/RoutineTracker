plugins {
    id("routinetracker.kmp.library")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":core:logic"))
                implementation(libs.jetbrains.kotlinx.datetime)
            }
        }
    }
}

android {
    namespace = "com.rendox.routinetracker.core.model"
}

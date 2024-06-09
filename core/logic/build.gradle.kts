plugins {
    id("routinetracker.kmp.library")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.jetbrains.kotlinx.datetime)
            }
        }
    }
}

android {
    namespace = "com.rendox.routinetracker.core.logic"
}


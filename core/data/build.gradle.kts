plugins {
    id("routinetracker.kmp.library")
    id("routinetracker.kmp.koin")
    id("routinetracker.kmp.library.instrumentationtestrunner")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":core:database"))
                implementation(project(":core:model"))
                implementation(project(":core:logic"))

                implementation(libs.jetbrains.kotlinx.datetime)
            }
        }

        androidInstrumentedTest {
            dependencies {
                implementation(project(":core:testcommon"))
                implementation(libs.jetbrains.kotlinx.coroutines.test)
            }
        }
    }
}

android {
    namespace = "com.rendox.routinetracker.core.data"

    flavorDimensions += "minSdk"

    productFlavors {
        create("minSdk26") {
            dimension = "minSdk"
            minSdk = 26
            compileOptions {
                isCoreLibraryDesugaringEnabled = false
            }
        }
        create("minSdk21") {
            dimension = "minSdk"
            minSdk = 21
            compileOptions {
                isCoreLibraryDesugaringEnabled = true
            }
        }
    }
}
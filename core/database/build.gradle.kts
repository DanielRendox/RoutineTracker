plugins {
    id("routinetracker.kmp.library")
    id("routinetracker.kmp.koin")
    id("routinetracker.kmp.library.instrumentationtestrunner")
    alias(libs.plugins.app.cash.sqldelight)
}

sqldelight {
    databases {
        create("RoutineTrackerDatabase") {
            packageName.set("com.rendox.routinetracker.core.database")
        }
    }
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":core:logic"))
                implementation(project(":core:model"))

                implementation(libs.jetbrains.kotlinx.datetime)

                implementation(libs.app.cash.sqldelight.coroutines.extensions)
                implementation(libs.app.cash.sqldelight.primitive.adapters)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.app.cash.sqldelight.android.driver)
                implementation(libs.app.cash.sqldelight.sqlite.driver)
            }
        }

        androidUnitTest {
            dependencies {
                implementation(libs.jetbrains.kotlinx.coroutines.test)
            }
        }

        androidInstrumentedTest {
            dependencies {
                implementation(libs.jetbrains.kotlinx.coroutines.test)
            }
        }

        nativeMain {
            dependencies {
                implementation(libs.app.cash.sqldelight.native.driver)
            }
        }
    }
}

android {
    namespace = "com.rendox.routinetracker.core.database"

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
plugins {
    id("routinetracker.kmp.library")
    id("routinetracker.kmp.koin")
    id("routinetracker.kmp.library.instrumentationtestrunner")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":core:data"))
                implementation(project(":core:database"))
                implementation(project(":core:model"))
                implementation(project(":core:logic"))

                implementation(libs.jetbrains.kotlinx.datetime)
                implementation(libs.jetbrains.kotlinx.coroutines.core)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.app.cash.sqldelight.sqlite.driver)
            }
        }

        androidUnitTest {
            dependencies {
                implementation(libs.jetbrains.kotlinx.coroutines.test)
            }
        }
    }
}

android {
    namespace = "com.rendox.routinetracker.domain"

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
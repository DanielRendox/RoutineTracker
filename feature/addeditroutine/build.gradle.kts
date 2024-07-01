plugins {
    id("routinetracker.android.feature")
    id("routinetracker.android.library.compose")
    id("routinetracker.android.koin")
    id("routinetracker.android.library.instrumentationtestrunner")
    id("routinetracker.lint")
}

android {

    namespace = "com.rendox.routinetracker.feature.addeditroutine"

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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.kizitonwose.calendar.compose)
    implementation(libs.jetbrains.kotlinx.datetime)
}
plugins {
    id("routinetracker.android.feature")
    id("routinetracker.android.library.compose")
    id("routinetracker.android.koin")
    id("routinetracker.android.library.instrumentationtestrunner")
}

android {

    namespace = "com.rendox.routinetracker.feature.agenda"

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
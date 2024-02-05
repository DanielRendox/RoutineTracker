plugins {
    id("routinetracker.android.library")
    id("routinetracker.android.library.compose")
    id("routinetracker.android.koin")
}

android {
    namespace = "com.rendox.routinetracker.core.ui"

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

dependencies {
    implementation(project(":core:model"))

    implementation(libs.kizitonwose.calendar.compose)
    implementation("androidx.compose.ui:ui-util")
    implementation(libs.material)
    implementation(libs.androidx.core.splashscreen)
}
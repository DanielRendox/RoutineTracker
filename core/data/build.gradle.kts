plugins {
    id("routinetracker.android.library")
    id("routinetracker.android.koin")
    id("routinetracker.android.library.instrumentationtestrunner")
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

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:model"))
    implementation(project(":core:logic"))

    implementation(libs.jetbrains.kotlinx.datetime)

    androidTestImplementation(project(":core:testcommon"))
    androidTestImplementation(libs.jetbrains.kotlinx.coroutines.test)
}
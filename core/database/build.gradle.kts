plugins {
    id("routinetracker.android.library")
    id("routinetracker.android.koin")
    id("routinetracker.android.library.instrumentationtestrunner")
    id("routinetracker.lint")
    alias(libs.plugins.app.cash.sqldelight)
}

sqldelight {
    databases {
        create("RoutineTrackerDatabase") {
            packageName.set("com.rendox.routinetracker.core.database")
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

dependencies {
    implementation(project(":core:logic"))
    implementation(project(":core:model"))

    implementation(libs.jetbrains.kotlinx.datetime)

    implementation(libs.app.cash.sqldelight.android.driver)
    implementation(libs.app.cash.sqldelight.coroutines.extensions.jvm)
    implementation(libs.app.cash.sqldelight.primitive.adapters)
    implementation(libs.app.cash.sqldelight.sqlite.driver)

    testImplementation(libs.jetbrains.kotlinx.coroutines.test)
    androidTestImplementation(libs.jetbrains.kotlinx.coroutines.test)
}
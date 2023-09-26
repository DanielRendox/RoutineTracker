plugins {
    id("routinetracker.android.library")
    id("routinetracker.android.koin")
    id("routinetracker.android.library.instrumentationtestrunner")
    alias(libs.plugins.app.cash.sqldelight)
}

sqldelight {
    databases {
        create("RoutineTrackerDatabase") {
            packageName = "com.rendox.routinetracker.core.database"
        }
    }
}

android {
    namespace = "com.rendox.routinetracker.core.database"
}

dependencies {
    implementation(project(":core:logic"))
    implementation(project(":core:model"))

    implementation(libs.jetbrains.kotlinx.datetime)

    implementation(libs.app.cash.sqldelight.android.driver)
    implementation(libs.app.cash.sqldelight.coroutines.extensions.jvm)
    implementation(libs.app.cash.sqldelight.primitive.adapters)

    androidTestImplementation(libs.jetbrains.kotlinx.coroutines.test)
}
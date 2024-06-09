import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "com.rendox.routinetracker.buildlogic"

// Configure the build-logic plugins to target JDK 17
// This matches the JDK used to build the project, and is not related to what is running on device.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.sqldelight.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplicationCompose") {
            id = "routinetracker.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidApplication") {
            id = "routinetracker.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "routinetracker.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "routinetracker.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "routinetracker.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("jvmLibrary") {
            id = "routinetracker.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("kmpLibrary") {
            id = "routinetracker.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("androidKoin") {
            id = "routinetracker.android.koin"
            implementationClass = "AndroidKoinConventionPlugin"
        }
        register("instrumentationTestRunner") {
            id = "routinetracker.android.library.instrumentationtestrunner"
            implementationClass = "AndroidLibraryInstrumentationTestRunner"
        }
    }
}
plugins {
    id("routinetracker.android.application")
    id("routinetracker.android.application.compose")
    id("routinetracker.android.koin")
}

android {
    namespace = "com.rendox.routinetracker.app"

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

    defaultConfig {
        applicationId = "com.rendox.routinetracker"
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }

        testInstrumentationRunner =
            "com.rendox.routinetracker.core.testcommon.InstrumentationTestRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))

    testImplementation(project(":core:testcommon"))

    implementation(libs.jetbrains.kotlinx.datetime)
}
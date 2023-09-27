plugins {
    id("routinetracker.android.application")
    id("routinetracker.android.application.compose")
    id("routinetracker.android.application.flavors")
    id("routinetracker.android.koin")
}

android {
    namespace = "com.rendox.routinetracker.app"

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
    implementation(project(":feature:routinedetails"))
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:testcommon"))
    implementation(project(":core:ui"))

    implementation(libs.jetbrains.kotlinx.datetime)
}
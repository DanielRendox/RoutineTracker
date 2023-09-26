package com.rendox.routinetracker

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun configureBuildTypes(
    commonExtension: CommonExtension<*, *, *, *, *>,
) {
    commonExtension.apply {
        buildTypes {
            getByName("release") {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }
    }
}

internal fun configurePackaging(
    commonExtension: CommonExtension<*, *, *, *, *>,
) {
    commonExtension.apply {
        packaging {
            resources {
                excludes.add("/META-INF/{AL2.0,LGPL2.1}")
            }
        }
    }
}

internal fun Project.addLocalTestDependencies(
    commonExtension: CommonExtension<*, *, *, *, *>,
) {
    commonExtension.apply {
        dependencies {
            add("testImplementation", libs.findLibrary("androidx-test-core").get())
            add("testImplementation", libs.findLibrary("junit").get())
            add("testImplementation", libs.findLibrary("google-truth").get())
        }
    }
}

internal fun Project.addAndroidTestDependencies(
    commonExtension: CommonExtension<*, *, *, *, *>,
) {
    commonExtension.apply {
        dependencies {
            add("androidTestImplementation", libs.findLibrary("androidx-test-ext-junit").get())
            add("androidTestImplementation", libs.findLibrary("google-truth").get())
        }
    }
}

internal fun Project.addKoinDependencies(
    commonExtension: CommonExtension<*, *, *, *, *>,
) {
    commonExtension.apply {
        dependencies {
            val bom = libs.findLibrary("insert-koin-bom").get()
            add("implementation", platform(bom))
            add("implementation", "io.insert-koin:koin-android")
            add("implementation", "io.insert-koin:koin-test")
            add("implementation", "io.insert-koin:koin-android-test")
        }
    }
}
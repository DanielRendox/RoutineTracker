package com.rendox.routinetracker

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun configureBuildTypes(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
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
    commonExtension: CommonExtension<*, *, *, *, *, *>,
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
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        dependencies {
            add("testImplementation", libs.findLibrary("androidx-test-core").get())
            val junitBom = libs.findLibrary("junit-bom").get()
            add("testImplementation", platform(junitBom))
            add("testImplementation", "org.junit.jupiter:junit-jupiter-api")
            add("testImplementation", "org.junit.jupiter:junit-jupiter-engine")
            add("testImplementation", "org.junit.vintage:junit-vintage-engine")
            add("testImplementation", "org.junit.jupiter:junit-jupiter-params")
            add("testImplementation", libs.findLibrary("google-truth").get())
        }
        testOptions {
            unitTests.all { it.useJUnitPlatform() }
        }
    }
}

internal fun Project.addAndroidTestDependencies(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        dependencies {
            add("androidTestImplementation", libs.findLibrary("androidx-test-ext-junit").get())
            add("androidTestImplementation", libs.findLibrary("google-truth").get())
        }
    }
}
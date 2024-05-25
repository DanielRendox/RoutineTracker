package com.rendox.routinetracker

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.ApplicationProductFlavor
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.ProductFlavor

@Suppress("EnumEntryName")
enum class FlavorDimension {
    minSdk
}

@Suppress("EnumEntryName")
enum class RoutineTrackerFlavor(
    val dimension: FlavorDimension,
    val minSdk: Int,
    val coreLibraryDesugaringEnabled: Boolean,
) {
    minSdk26(dimension = FlavorDimension.minSdk, minSdk = 26, coreLibraryDesugaringEnabled = false),
    minSdk21(dimension = FlavorDimension.minSdk, minSdk = 21, coreLibraryDesugaringEnabled = true),
}

fun configureFlavors(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    flavorConfigurationBlock: ProductFlavor.(flavor: RoutineTrackerFlavor) -> Unit = {}
) {
    commonExtension.apply {
        flavorDimensions += FlavorDimension.minSdk.name
        productFlavors {
            RoutineTrackerFlavor.values().forEach {
                create(it.name) {
                    dimension = it.dimension.name
                    flavorConfigurationBlock(this, it)
                    if (this@apply is ApplicationExtension && this is ApplicationProductFlavor) {
                        minSdk = it.minSdk
                        compileOptions {
                            isCoreLibraryDesugaringEnabled = it.coreLibraryDesugaringEnabled
                        }
                    }
                }
            }
        }
    }
}
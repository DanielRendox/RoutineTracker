import com.android.build.api.dsl.LibraryExtension
import com.rendox.routinetracker.addAndroidTestDependencies
import com.rendox.routinetracker.addLocalTestDependencies
import com.rendox.routinetracker.configureBuildTypes
import com.rendox.routinetracker.configureKotlinAndroid
import com.rendox.routinetracker.configurePackaging
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.android.library")
            }
            extensions.configure<KotlinMultiplatformExtension> {
                applyDefaultHierarchyTemplate()

                androidTarget {
                    compilerOptions {
                        jvmTarget.set(JvmTarget.JVM_11)
                    }
                }
                iosX64()
                iosArm64()
                iosSimulatorArm64()
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 34
                configureBuildTypes(this)
                configurePackaging(this)
                addLocalTestDependencies(this)
                addAndroidTestDependencies(this)
            }
        }
    }
}
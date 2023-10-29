import com.android.build.gradle.LibraryExtension
import com.rendox.routinetracker.addAndroidTestDependencies
import com.rendox.routinetracker.addLocalTestDependencies
import com.rendox.routinetracker.configureBuildTypes
import com.rendox.routinetracker.configureKotlinAndroid
import com.rendox.routinetracker.configurePackaging
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
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
import com.android.build.api.dsl.ApplicationExtension
import com.rendox.routinetracker.addAndroidTestDependencies
import com.rendox.routinetracker.addLocalTestDependencies
import com.rendox.routinetracker.configureBuildTypes
import com.rendox.routinetracker.configureKotlinAndroid
import com.rendox.routinetracker.configurePackaging
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 34
                configureBuildTypes(this)
                configurePackaging(this)
                addLocalTestDependencies(this)
                addAndroidTestDependencies(this)
            }

            extensions.getByType<KotlinAndroidProjectExtension>().apply {
                configureKotlinAndroid(this)
            }

            dependencies {
                add("testImplementation", project(":core:testcommon"))
            }
        }
    }

}
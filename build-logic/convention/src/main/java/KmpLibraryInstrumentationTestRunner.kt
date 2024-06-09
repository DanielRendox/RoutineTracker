import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

class KmpLibraryInstrumentationTestRunner : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
            }

            extensions.configure<LibraryExtension> {
                defaultConfig {
                    testInstrumentationRunner =
                        "com.rendox.routinetracker.core.testcommon.InstrumentationTestRunner"
                }
            }

            dependencies {
                add("androidUnitTestImplementation", project(":core:testcommon"))
            }
        }
    }
}
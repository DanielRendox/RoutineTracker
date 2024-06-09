import com.rendox.routinetracker.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KmpKoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                val bom = libs.findLibrary("insert-koin-bom").get()

                add("commonMainImplementation", platform(bom))
                add("commonMainImplementation", "io.insert-koin:koin-core")
                add("androidUnitTestImplementation", "io.insert-koin:koin-test")
            }
        }
    }

}
import com.android.build.gradle.LibraryExtension
import com.rendox.routinetracker.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidKoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                val bom = libs.findLibrary("insert-koin-bom").get()
                add("implementation", platform(bom))
                add("implementation", "io.insert-koin:koin-android")
                add("implementation", "io.insert-koin:koin-test")
                add("implementation", "io.insert-koin:koin-android-test")
            }
        }
    }

}
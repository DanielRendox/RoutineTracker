import com.rendox.routinetracker.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")
            }
            extensions.configure<KotlinJvmProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
                dependencies {
                    val junitBom = libs.findLibrary("junit-bom").get()
                    add("testImplementation", platform(junitBom))
                    add("testImplementation", "org.junit.jupiter:junit-jupiter-api")
                    add("testImplementation", "org.junit.jupiter:junit-jupiter-engine")
                    add("testImplementation", "org.junit.vintage:junit-vintage-engine")
                    add("testImplementation", "org.junit.jupiter:junit-jupiter-params")
                    add("testImplementation", libs.findLibrary("google-truth").get())
                }
            }
        }
    }
}
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Bundling
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

class StaticAnalysisConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            applyKtLintDependencies()
            createKtLintTasks()
            configureAndroidLint()
        }
    }
}

internal fun Project.applyKtLintDependencies() = dependencies {
    val ktlint by configurations.creating

    ktlint("com.pinterest.ktlint:ktlint-cli:1.3.0") {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }
}

private fun Project.createKtLintTasks() {
    createTask(
        name = "ktlintCheck",
        description = "Check Kotlin code style.",
    )

    createTask(
        name = "ktlintFormat",
        description = "Fix Kotlin code style deviations.",
        args = listOf("-F"),
    )
}

private fun Project.createTask(
    name: String,
    description: String,
    args: List<String> = emptyList(),
) {
    val inputFiles = fileTree(
        mapOf("dir" to "src", "include" to "**/*.kt"),
    )
    val outputDir = "${project.layout.buildDirectory.asFile.get().path}/reports/ktlint/"
    val editorConfigPath = "${rootDir.path}/.editorconfig"

    tasks.register<JavaExec>(
        name = name,
    ) {
        dependsOn(tasks.getByName("clean"))

        inputs.files(inputFiles)
        outputs.dir(outputDir)

        group = "ktlint"
        this.description = description
        classpath = configurations.getByName("ktlint")

        mainClass.set("com.pinterest.ktlint.Main")
        // see https://pinterest.github.io/ktlint/install/cli/#command-line-usage for more information
        this.args = args + listOf(
            "src/**/*.kt",
            "--editorconfig", editorConfigPath,
        )
        jvmArgs = listOf("--add-opens=java.base/java.lang=ALL-UNNAMED")
    }
}

private fun Project.configureAndroidLint() {
    extensions.findByType(CommonExtension::class.java)?.apply {
        lint {
            abortOnError = true
            checkReleaseBuilds = true
            checkAllWarnings = true
            ignoreWarnings = false
            warningsAsErrors = true
            lintConfig = file("${rootDir.path}/lint.xml")
            htmlReport = true
        }
    }
}

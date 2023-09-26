pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Routine Tracker"
include(":app")
include(":core:database")
include(":feature:routinedetails")
include(":core:ui")
include(":core:logic")
include(":core:testcommon")
include(":core:data")
include(":core:model")

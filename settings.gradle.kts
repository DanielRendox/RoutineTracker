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
include(":core:ui")
include(":core:logic")
include(":core:testcommon")
include(":core:data")
include(":core:model")
include(":core:domain")
include(":feature")
include(":feature:routine_details")
include(":feature:agenda")
include(":feature:add_edit_routine")

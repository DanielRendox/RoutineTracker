plugins {
    id("routinetracker.jvm.library")
    id("routinetracker.lint")
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.jetbrains.kotlinx.datetime)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.google.truth)
}

tasks.test {
    useJUnitPlatform()
}
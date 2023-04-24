import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka") version "1.6.10"
    id("org.jetbrains.kotlinx.kover") version "0.5.1"
    id("klutter")
}

subprojects {
    plugins.apply("org.jetbrains.dokka")
}

kover {

    // KOVER destroys running with coverage from IDE
    isDisabled = hasProperty("nokover")

    coverageEngine.set(kotlinx.kover.api.CoverageEngine.JACOCO)

    jacocoEngineVersion.set("0.8.8")

    disabledProjects = setOf(
        // contains only annotations
        // and breaks Jacoco due to duplicate classes
        ":lib:annotations",

        // breaks Jacoco due to duplicate classes, haven't found a fix yet...
        ":lib:kompose",

        // a test-only module
        ":lib-test",

        // a test-only module
        ":test-integration",

        // for KSP testing only
        ":test-ksp",
    )
}

tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets {
        configureEach {
            includeNonPublic
        }
    }
}

tasks.dokkaHtmlMultiModule.configure {
    outputDirectory.set(layout.buildDirectory.dir("dokkaSite").map { it.asFile })
}

tasks.koverMergedXmlReport {
    isEnabled = true

    excludes = listOf(
        // A test-only module
        "dev.buijs.klutter.kore.test.*",
    )

    xmlReportFile.set(layout.buildDirectory.file("koverage.xml"))
}
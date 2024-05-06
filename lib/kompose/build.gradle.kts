plugins {
    kotlin("plugin.serialization") version "1.9.10"
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("maven-publish")
    id("klutter")
}

group = "dev.buijs.klutter"
version = dev.buijs.klutter.ProjectVersions.kompose

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {

    androidTarget {
        publishLibraryVariants("release", "debug")
    }

    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    cocoapods {
        summary = "Klutter Kompose module"
        homepage = "https://buijs.dev"
        ios.deploymentTarget = "9.0"
        framework {
            baseName = "Kompose"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation(project(":lib:annotations"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }

        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
                implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
                implementation(project(":lib:kore"))
                implementation(project(":lib:annotations"))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(project(":lib-test"))
            }
        }
    }
}

android {
    namespace = "dev.buijs.klutter.kompose"
    compileSdk = 31
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
    }
}

publishing {
    repositories {
        maven {
            credentials {
                username = dev.buijs.klutter.Repository.username
                password = dev.buijs.klutter.Repository.password
            }

            url = dev.buijs.klutter.Repository.endpoint
        }
    }

    publications.withType<MavenPublication> {
        artifactId = if (name == "kotlinMultiplatform") {
            "kompose"
        } else {
            "kompose-$name"
        }
    }

}
tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {

    outputDirectory.set(layout.buildDirectory.dir("dokka").get().asFile)

    dokkaSourceSets {
        register("kompose") {
            displayName.set("Kompose")
            platform.set(org.jetbrains.dokka.Platform.jvm)
            sourceRoots.from(kotlin.sourceSets.getByName("jvmMain").kotlin.srcDirs)
            sourceRoots.from(kotlin.sourceSets.getByName("commonMain").kotlin.srcDirs)
        }

    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}
plugins {
    kotlin("plugin.serialization") version "1.9.10"
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("maven-publish")
    id("klutter")
}

group = "dev.buijs.klutter"
version = dev.buijs.klutter.ProjectVersions.annotations

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
        summary = "Klutter module for annotations"
        homepage = "https://buijs.dev"
        ios.deploymentTarget = "9.0"
        framework {
            baseName = "Annotations"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
            }
        }

        val jvmTest by getting
        val androidMain by getting
        val androidUnitTest by getting

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }

    }
}

android {
    namespace = "dev.buijs.klutter.annotations"
    compileSdk = 31
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        //targetSdk = 31
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
            "annotations"
        } else {
            "annotations-$name"
        }
    }

}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {

    outputDirectory.set(layout.buildDirectory.dir("dokka").get().asFile)

    dokkaSourceSets {
        register("annotations4Jvm") {
            displayName.set("JVM")
            platform.set(org.jetbrains.dokka.Platform.jvm)
            sourceRoots.from(kotlin.sourceSets.getByName("jvmMain").kotlin.srcDirs)
            sourceRoots.from(kotlin.sourceSets.getByName("commonMain").kotlin.srcDirs)
        }

        register("annotations4Android") {
            displayName.set("Android")
            platform.set(org.jetbrains.dokka.Platform.jvm)
            sourceRoots.from(kotlin.sourceSets.getByName("androidMain").kotlin.srcDirs)
            sourceRoots.from(kotlin.sourceSets.getByName("commonMain").kotlin.srcDirs)
        }

        register("annotations4Ios") {
            displayName.set("IOS")
            platform.set(org.jetbrains.dokka.Platform.jvm)
            sourceRoots.from(kotlin.sourceSets.getByName("iosMain").kotlin.srcDirs)
            sourceRoots.from(kotlin.sourceSets.getByName("commonMain").kotlin.srcDirs)
        }
    }
}

tasks.named("iosX64Test", org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest::class.java).configure {
    // ENV VAR is set in GH where iPhone 14 is not available
    if(System.getenv("KLUTTER_PRIVATE_URL") == null) {
        device = "iPhone 14 Pro Max"
    }
}

tasks.named("iosSimulatorArm64Test", org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest::class.java).configure {
    // ENV VAR is set in GH where iPhone 14 is not available
    if(System.getenv("KLUTTER_PRIVATE_URL") == null) {
        device = "iPhone 14 Pro Max"
    }
}
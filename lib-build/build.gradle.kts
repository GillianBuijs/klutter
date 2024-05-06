plugins {
    kotlin("jvm") version "1.9.10"
    id("java-gradle-plugin")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

gradlePlugin {
    plugins.register("klutter") {
        id = "klutter"
        implementationClass = "dev.buijs.klutter.KlutterInternalPlugin"
    }
}

buildscript {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")
        //classpath("com.android.tools.build:gradle:8.1.4")
    }
}

allprojects {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")
    implementation("org.gradle.kotlin:gradle-kotlin-dsl-plugins:2.4.0")
    implementation(gradleApi())
}
/* Copyright (c) 2021 - 2022 Buijs Software
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package dev.buijs.klutter.plugins.gradle.tasks.adapter.flutter

import dev.buijs.klutter.core.*
import dev.buijs.klutter.core.KlutterPrinter
import java.io.File

/**
 * @author Gillian Buijs
 */
class AndroidBuildGradleGenerator(
    private val android: Android,
): KlutterFileGenerator() {

    override fun generate() = writer().write()

    override fun printer() = AndroidBuildGradlePrinter(aarFile(), app())

    override fun writer() = AndroidBuildGradleWriter(app().resolve("build.gradle"), printer().print())

    private fun aarFile() = app().resolve(".klutter/platform-release.aar")

    private fun app() = android.app().absoluteFile

}

class AndroidBuildGradlePrinter(
    private val aarFileLocation: File,
    private val androidLocation: File,
): KlutterPrinter {

    override fun print(): String {
        val dollar = "$"
        val kmpAarFile: String = aarFileLocation.relativeTo(androidLocation).toString()

        val dependencies = mutableListOf(
            "implementation \"dev.buijs.klutter:core:${dollar}klutter\"",
            "implementation \"org.jetbrains.kotlin:kotlin-stdlib-jdk8:${dollar}kotlin\"",
            "implementation \"dev.buijs.klutter:annotations-kmp-android:${dollar}klutter\"",
            "runtimeOnly \"org.jetbrains.kotlinx:kotlinx-coroutines-android:${dollar}kotlinxCoroutinesVersion\"",
            "implementation files(\"../$kmpAarFile\")"
        )

        return """
            |// Autogenerated by Klutter
            |// Do not edit directly
            |// See: https://buijs.dev/klutter
            |
            |import dev.buijs.klutter.core.*
            |
            |def localProperties = new Properties()
            |def localPropertiesFile = rootProject.file('local.properties')
            |if (localPropertiesFile.exists()) {
            |    localPropertiesFile.withReader('UTF-8') { reader ->
            |        localProperties.load(reader)
            |    }
            |}
            |
            |def flutterRoot = localProperties.getProperty('flutter.sdk')
            |if (flutterRoot == null) {
            |    throw new GradleException("Flutter SDK not found. Define location with flutter.sdk in the local.properties file.")
            |}
            |
            |apply plugin: 'com.android.application'
            |apply plugin: 'kotlin-android'
            |apply from: "${dollar}flutterRoot/packages/flutter_tools/gradle/flutter.gradle"
            |
            |def propertiesFile = new File("${dollar}{projectDir}/../../buildSrc/buildsrc.properties")
            |if (!propertiesFile.exists()) {
            |    throw new GradleException("File not found ${dollar}propertiesFile")
            |}
            |
            |def properties = new Properties()
            |propertiesFile.withReader('UTF-8') { reader ->
            |    properties.load(reader)
            |}
            |
            |def appId = properties.getProperty('app.id')
            |if (appId == null) {
            |    throw new GradleException("ApplicationId not found. Define app.id in buildSrc/buildsrc.properties")
            |}
            |
            |def minSdk = properties.getProperty('android.sdk.min')
            |if (minSdk == null) {
            |    throw new GradleException("Android min SDK version not found. Define android.sdk.min in buildSrc/buildsrc.properties")
            |}
            |
            |def targetSdk = properties.getProperty('android.sdk.target')
            |if (targetSdk == null) {
            |    throw new GradleException("Android target SDK version not found. Define android.target.sdk in buildSrc/buildsrc.properties")
            |}
            |
            |def compileSdk = properties.getProperty('android.sdk.compile')
            |if (compileSdk == null) {
            |    throw new GradleException("Android compile SDK version not found. Define android.compile.sdk in buildSrc/buildsrc.properties")
            |}
            |
            |def appVersionCode = properties.getProperty('app.version.code')
            |if (appVersionCode == null) {
            |    throw new GradleException("App version code not found. Define app.version.code in buildSrc/buildsrc.properties")
            |}
            |
            |def appVersionName = properties.getProperty('app.version.name')
            |if (appVersionName == null) {
            |    throw new GradleException("App version name not found. Define app.version.name in buildSrc/buildsrc.properties")
            |}
            |
            |def klutter = properties.getProperty('klutter')
            |if (klutter == null) {
            |    throw new GradleException("Klutter version not found. Define klutter in buildSrc/buildsrc.properties")
            |}
            |
            |def kotlin = properties.getProperty('kotlin')
            |if (kotlin == null) {
            |    throw new GradleException("Kotlin version not found. Define kotlin in buildSrc/buildsrc.properties")
            |}
            |
            |def kotlinxCoroutinesVersion = properties.getProperty('kotlinxCoroutinesVersion')
            |if (kotlinxCoroutinesVersion == null) {
            |    throw new GradleException("Kotlinx Coroutines version not found. Define kotlinxCoroutinesVersion in buildSrc/buildsrc.properties")
            |}      
            |
            |def secrets = Klutter.secrets(project)
            |
            |android {
            |    compileSdkVersion compileSdk.toInteger()
            |
            |    compileOptions {
            |        sourceCompatibility JavaVersion.VERSION_1_8
            |        targetCompatibility JavaVersion.VERSION_1_8
            |    }
            |
            |    kotlinOptions {
            |        jvmTarget = '1.8'
            |    }
            |
            |    sourceSets {
            |        main.java.srcDirs += 'src/main/kotlin'
            |    }
            |
            |    defaultConfig {
            |        applicationId appId
            |        minSdkVersion minSdk.toInteger()
            |        targetSdkVersion targetSdk.toInteger()
            |        versionCode appVersionCode.toInteger()
            |        versionName appVersionName
            |    }
            |
            |    signingConfigs {
            |        release {
            |           storeFile file(secrets.get("store.file.uri") ?: project.projectDir)
            |           storePassword secrets.get("store.password") ?: ""
            |           keyAlias secrets.get("key.alias") ?: ""
            |           keyPassword secrets.get("key.password") ?: ""
            |        }
            |    }
            |
            |    buildTypes {
            |
            |        release {
            |            minifyEnabled true
            |        }
            |
            |        debug {
            |            signingConfig signingConfigs.debug
            |            debuggable true
            |        }
            |
            |    }
            |}
            |
            |flutter {
            |    source '../..'
            |}
            |
            |dependencies {
            |${dependencies.sortedByDescending { it }.joinToString("\n") { "    $it" }}
            |}
            |
        """.trimMargin()
    }

}

class AndroidBuildGradleWriter(
    val file: File,
    val content: String,
): KlutterWriter {

    override fun write() {
        val logger = KlutterLogger()

        if(file.exists()) {
            file.delete().also {
                logger.debug("Deleted build.gradle: $file")
            }
        } else logger.error("Build.gradle file in android folder not found. Creating a new file!")

        file.createNewFile().also { logger.debug("Created new build.gradle: $file") }
        file.writeText(content).also { logger.debug("Written content to $file:\r\n$content") }
    }

}
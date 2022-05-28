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

package dev.buijs.klutter.core.tasks.adapter.flutter

import dev.buijs.klutter.core.*
import dev.buijs.klutter.core.KlutterPrinter
import org.gradle.api.logging.Logging
import java.io.File

/**
 * @author Gillian Buijs
 */
internal class AndroidBuildGradleGenerator(
    private val android: Android,
): KlutterFileGenerator() {

    override fun generate() = writer().write()

    override fun printer() = AndroidBuildGradlePrinter()

    override fun writer() = AndroidBuildGradleWriter(app()?.resolve("build.gradle"), printer().print())

    private fun app() = android.app()?.absoluteFile

}

internal class AndroidBuildGradlePrinter: KlutterPrinter {

    override fun print(): String {
        val dollar = "$"

        val dependencies = mutableListOf(
            "implementation \"dev.buijs.klutter:core:${dollar}{project.ext[\"klutterVersion\"]}\"",
            "runtimeOnly \"org.jetbrains.kotlinx:kotlinx-coroutines-android:${dollar}{project.ext[\"kotlinxVersion\"]}\"",
            "implementation project(\":platform\")"
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
            |def klutterGradleFile = new File("$dollar{projectDir}/../../klutter.gradle")
            |if (!klutterGradleFile.exists()) {
            |    throw new GradleException("File not found ${dollar}klutterGradleFile")
            |}
            |
            |apply plugin: 'com.android.application'
            |apply plugin: 'kotlin-android'
            |apply from: "${dollar}flutterRoot/packages/flutter_tools/gradle/flutter.gradle"
            |apply from: "${dollar}klutterGradleFile"
            |
            |def secrets = Klutter.secrets(project)
            |
            |android {
            |    compileSdkVersion project.ext["androidCompileSdk"].toInteger()
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
            |       applicationId project.ext["applicationId"]
            |       minSdkVersion project.ext["androidMinSdk"].toInteger()
            |       targetSdkVersion project.ext["androidTargetSdk"].toInteger()
            |       versionCode project.ext["appVersionCode"].toInteger()
            |       versionName project.ext["appVersionName"]
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
            |            signingConfig signingConfigs.release
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

internal class AndroidBuildGradleWriter(
    val file: File?,
    val content: String,
): KlutterWriter {

    private val log = Logging.getLogger(AndroidBuildGradleWriter::class.java)

    override fun write() {

        if(file == null) {
            log.error("Path to Build.gradle file in android folder is null. Failed to generate build.gradle.")
            return
        }

        if(file.exists()) {
            file.delete().also {
                log.debug("Deleted build.gradle: $file")
            }
        } else log.error("Build.gradle file in android folder not found. Creating a new file!")

        file.createNewFile().also { log.debug("Created new build.gradle: $file") }
        file.writeText(content).also { log.debug("Written content to $file:\r\n$content") }
    }

}
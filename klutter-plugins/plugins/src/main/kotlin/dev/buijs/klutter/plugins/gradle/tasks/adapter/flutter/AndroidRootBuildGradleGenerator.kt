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
import dev.buijs.klutter.plugins.gradle.dsl.KlutterRepository
import java.io.File
import kotlin.collections.HashMap

/**
 * @author Gillian Buijs
 */
internal class AndroidRootBuildGradleGenerator(
    private val root: Root,
    private val android: Android,
    private val repositories: List<KlutterRepository>
): KlutterFileGenerator() {

    override fun generate() = writer().write()

    override fun printer() = AndroidRootBuildGradlePrinter(properties(), repositories)

    override fun writer() = AndroidRootBuildGradleWriter(android.file.resolve("build.gradle"), printer().print())

    private fun properties() = KlutterPropertiesReader(root.folder.resolve(".klutter/klutter.properties")).read()

}

/**
 * @author Gillian Buijs
 */
internal class AndroidRootBuildGradlePrinter(
    private val props: HashMap<String, String>,
    private val repositories: List<KlutterRepository>,
): KlutterPrinter {

    override fun print(): String {
        val kotlinVersion: String = get("kotlin.version")
        val gradleVersion: String = get("gradle.version")
        return """    
            |// Autogenerated by Klutter
            |// Do not edit directly
            |// Do not check into VCS
            |// See also: https://buijs.dev/klutter
            |buildscript {
            |    
            |    // Do not edit directly! Source of value is klutter.yaml.
            |    ext.kotlin_version = "$kotlinVersion"
            |    repositories {
            |        google()
            |        mavenCentral()
            |    }
            |    
            |    // Do not edit directly! Source of value is klutter.yaml.
            |    dependencies {
            |        classpath "com.android.tools.build:gradle:$gradleVersion"
            |        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
            |    }
            |}
            |
            |allprojects {
            |   def kProps = new Properties()
            |   new File(project.projectDir, ".klutter/klutter.properties")
            |           .getCanonicalFile()
            |           .withReader('UTF-8') { reader -> kProps.load(reader)}
            |
            |    repositories {
            |        google()
            |        mavenCentral()
            |        ${repositories()}
            |    }
            |}
            |
            |rootProject.buildDir = '../build'
            |subprojects {
            |    project.buildDir = "${'$'}{rootProject.buildDir}/${'$'}{project.name}"
            |    project.evaluationDependsOn(':app')
            |}
            |
            |task clean(type: Delete) {
            |   delete rootProject.buildDir
            |}
    """.trimMargin()
    }

    private fun get(key: String) = props[key]
        ?: throw KlutterConfigException("klutter.properties is missing property: $key")

    private fun repositories(): String {
        return repositories.joinToString {
            if (it.username == null && it.password == null) {
                """
                |maven {
                |   url = uri(kProps.getProperty('${it.url}'))
                |}
            """.trimMargin()

            } else {
                """
                |maven {
                |   url = uri(kProps.getProperty('${it.url}'))
                |   credentials {
                |       username = kProps.getProperty('${it.username}')
                |       password = kProps.getProperty('${it.password}')
                |   }
                |}
            """.trimMargin()
            }
        }
    }
}


/**
 * @author Gillian Buijs
 */
internal class AndroidRootBuildGradleWriter(val file: File, val content: String): KlutterWriter {

    override fun write(): KlutterLogger {
        val logger = KlutterLogger()

        if(file.exists()) {
            file.delete().also {
                logger.debug("Deleted build.gradle: $file")
            }
        } else logger.error("Build.gradle file in android folder not found. Creating a new file!")

        file.createNewFile().also { logger.debug("Created new build.gradle: $file") }
        file.writeText(content).also { logger.debug("Written content to $file:\r\n$content") }
        return logger
    }

}
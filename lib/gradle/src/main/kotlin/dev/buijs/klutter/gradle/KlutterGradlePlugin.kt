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
package dev.buijs.klutter.gradle

import com.google.devtools.ksp.gradle.KspExtension
import com.google.devtools.ksp.gradle.KspGradleSubplugin
import dev.buijs.klutter.gradle.dsl.KlutterExtension
import dev.buijs.klutter.gradle.tasks.*
import dev.buijs.klutter.gradle.tasks.CopyAndroidAarFileGradleTask
import dev.buijs.klutter.gradle.tasks.CopyIosFrameworkGradleTask
import dev.buijs.klutter.gradle.tasks.GetProtocDartGradleTask
import dev.buijs.klutter.gradle.tasks.GetProtocGradleTask
import dev.buijs.klutter.gradle.tasks.KlutterGradleTaskName.*
import dev.buijs.klutter.kore.project.kspArgumentKlutterProjectFolder
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Gradle plugin for Klutter Framework.
 *
 * The plugin has extension [KlutterExtension] which can be used to configure the klutter project.
 * See [KlutterGradleTaskName] for all available tasks. Most tasks are run automatically when executing a Gradle build.
 * See [bindPostBuildTasks].
 *
 * The klutter compiler plugin is applied which handles annotation scanning, code generation and more.
 */
class KlutterGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            applyAndConfigureKspExtension()
            registerTasks()
            addKlutterExtension()
            project.afterEvaluate {
                configureKotlinMultiplatformExtension {
                    bindPostBuildTasks()
                }
            }
        }
    }
}

/**
 * Add the [KlutterExtension] to the Gradle [Project].
 */
private fun Project.addKlutterExtension() {
    extensions.add("klutter", KlutterExtension(project))
}

/**
 * Apply the [KspGradleSubplugin] and add required arguments for the compiler plugin.
 */
private fun Project.applyAndConfigureKspExtension() {
    plugins.apply(KspGradleSubplugin::class.java)
    val ksp = project.extensions.getByType(KspExtension::class.java)
    ksp.arg(kspArgumentKlutterProjectFolder, project.rootDir.absolutePath)
}

/**
 * Add KSP generated code to the KMP source sets.
 */
private fun Project.configureKotlinMultiplatformExtension(
    /**
     * Action to invoke if the [KotlinMultiplatformExtension] is found.
     *
     * The [KlutterGradlePlugin] is applied to the platform module which is a KMP module
     * and the android module which is not a KMP module. Some configuration can only be
     * done in a KMP module, which can be configured through this closure.
     */
    doIfExtensionIsPresent: () -> Unit
) {
    // KMP
    val kmp = try {
        project.extensions.getByType(KotlinMultiplatformExtension::class.java)
    } catch (_: Exception) {
        null
    }

    if (kmp != null) {
        kmp.sourceSets
            .getByName("commonMain")
            .kotlin
            .srcDir(layout.buildDirectory.file("generated/ksp/metadata/commonMain/kotlin"))
        doIfExtensionIsPresent()
    }
}

/**
 * Register Klutter tasks in the Gradle [Project].
 */
private fun Project.registerTasks() {
    tasks.registerTasks()
}

/**
 * Register Klutter tasks in the [TaskContainer].
 */
private fun TaskContainer.registerTasks() {
    for (gradleTaskName in KlutterGradleTaskName.entries) {
        when (gradleTaskName) {
            GenerateProtoSchemas -> {
                // Should be registered directly in build.gradle.kts.
            }

            CompileProtoSchemas ->
                registerTask<CompileProtoSchemasGradleTask>(gradleTaskName)

            CopyAndroidAarFile ->
                registerTask<CopyAndroidAarFileGradleTask>(gradleTaskName)

            CopyIosFramework ->
                registerTask<CopyIosFrameworkGradleTask>(gradleTaskName)

            GenerateFlutterLib ->
                registerTask<GenerateFlutterLibGradleTask>(gradleTaskName)

            GetProtocDart ->
                registerTask<GetProtocDartGradleTask>(gradleTaskName)

            GetProtoc ->
                registerTask<GetProtocGradleTask>(gradleTaskName)

            GetKradle ->
                registerTask<GetKradleGradleTask>(gradleTaskName)
        }
    }
}

private fun Project.bindPostBuildTasks() {
    tasks.bindPostBuildTasks()
}

private fun TaskContainer.bindPostBuildTasks() {
    val assembleXCFrameworkTaskName = "assemblePlatformReleaseXCFramework"

    finalizeTaskBy("build") {
        +assembleXCFrameworkTaskName
        +GenerateProtoSchemas
        +CopyAndroidAarFile
    }

    finalizeTaskBy(assembleXCFrameworkTaskName) {
        +CopyIosFramework
    }

    finalizeTaskBy(GenerateProtoSchemas.taskName) {
        +CompileProtoSchemas
    }
}

private fun TaskContainer.finalizeTaskBy(
    name: String,
    builder: StringListBuilder.() -> Unit
) {
    getByName(name).setFinalizedBy(StringListBuilder().also(builder).data)
}

private class StringListBuilder {
    val data: MutableList<String> = mutableListOf()

    operator fun String.unaryPlus() {
        data.add(this)
    }

    operator fun KlutterGradleTaskName.unaryPlus() {
        data.add(this.taskName)
    }
}

private inline fun <reified T : AbstractTask> TaskContainer.registerTask(task: KlutterGradleTaskName) {
    register(task.taskName, T::class.java)
}
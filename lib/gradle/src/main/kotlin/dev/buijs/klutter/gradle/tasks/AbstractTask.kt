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
package dev.buijs.klutter.gradle.tasks

import dev.buijs.klutter.gradle.dsl.KlutterExtension
import dev.buijs.klutter.kore.KlutterTask
import dev.buijs.klutter.kore.common.ExcludeFromJacocoGeneratedReport
import dev.buijs.klutter.kore.project.Project
import dev.buijs.klutter.kore.project.plugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Name of a Gradle Task added by Klutter.
 */
enum class KlutterGradleTaskName(private val identifier: String) {
    CopyAndroidAarFile("CopyAarFile"),

    CopyIosFramework("CopyFramework"),

    GenerateFlutterLib("GenerateFlutterLib"),

    GenerateProtoSchemas("GenerateProtoSchemas"),

    GetProtocDart("GetProtocDart"),

    GetProtoc("GetProtoc"),

    CompileProtoSchemas("CompileProtoSchemas");

    val taskName: String
        get() = "klutter$identifier"

}

/**
 * Parent of all Gradle Tasks.
 */
internal abstract class AbstractTask : DefaultTask() {

    init {
        group = "klutter"
    }

    internal abstract fun klutterTask(): KlutterTask

    @get:Internal
    internal abstract val gradleTaskName: KlutterGradleTaskName

    @ExcludeFromJacocoGeneratedReport(reason = "")
    @TaskAction
    fun execute() = klutterTask().run()

    fun project(): Project {
        val ext = project.klutterExtension()
        val root = ext.root ?: project.rootProject.projectDir
        return root.plugin()
    }

}

internal fun org.gradle.api.Project.klutterExtension(): KlutterExtension {
    return extensions.getByName("klutter").let {
        if (it is KlutterExtension) {
            it
        } else {
            throw IllegalStateException("klutter extension is not of the correct type")
        }
    }
}
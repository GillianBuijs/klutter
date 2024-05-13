/* Copyright (c) 2021 - 2024 Buijs Software
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

import dev.buijs.klutter.kore.KlutterException
import dev.buijs.klutter.kore.KlutterTask
import dev.buijs.klutter.kore.project.protocHome
import dev.buijs.klutter.kore.tasks.DownloadProtocTask
import dev.buijs.klutter.kore.tasks.GetDartProtocExeTask
import dev.buijs.klutter.kore.tasks.codegen.CompileProtoSchemaTask
import dev.buijs.klutter.kore.tasks.codegen.GenerateProtoSchemasTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

/**
 * Execute task [DownloadProtocTask] from Gradle.
 */
internal open class GetProtocGradleTask: AbstractTask() {

    override val gradleTaskName = KlutterGradleTaskName.GetProtoc

    override fun klutterTask(): KlutterTask =
        DownloadProtocTask(
            rootFolder = super.project().root.folder,
            overwrite = false,
            target = protocHome
        )

}

/**
 * Execute task [GetDartProtocExeTask] from Gradle.
 */
internal open class GetProtocDartGradleTask: AbstractTask() {

    override val gradleTaskName = KlutterGradleTaskName.GetProtocDart

    override fun klutterTask(): KlutterTask = GetDartProtocExeTask()
}

/**
 * Run the [GenerateProtoSchemasTask] from Gradle.
 *
 * This Task should be instantiated in the build.gradle.kts File,
 * which ensures all dependencies from the platform project
 * are loaded in the Classloader.
 */
abstract class GenerateProtoSchemasGradleTask: DefaultTask() {

    @get:Optional
    @get:Input
    abstract var classLoader: ClassLoader?

    companion object {
        /**
         * Name of this task to be registered.
         *
         * See [KlutterGradleTaskName.GenerateProtoSchemas].
         */
        val taskName: String = KlutterGradleTaskName.GenerateProtoSchemas.taskName
    }

    @TaskAction
    fun execute() {
        if(classLoader == null) {
            throw KlutterException("GenerateProtoSchemaGradleTask is missing property value 'classLoader'")
        } else {
            GenerateProtoSchemasTask(project.rootDir, classLoader!!).run()
        }
    }

}

/**
 * Run [CompileProtoSchemaTask] from Gradle.
 *
 * This Task depends on [GetProtocDartGradleTask] and [GetProtocGradleTask]
 * which installs the protoc toolchain for compiling schemas.
 *
 * This Task is finalized by [GenerateFlutterLibGradleTask],
 * because compiling protoc schemas results in new dart classes in the ./lib folder.
 * These new dart classes should be exported through the lib file.
 */
internal open class CompileProtoSchemasGradleTask: AbstractTask() {

    override val gradleTaskName = KlutterGradleTaskName.CompileProtoSchemas

    init {
        super.dependsOn(KlutterGradleTaskName.GetProtocDart.taskName, KlutterGradleTaskName.GetProtoc.taskName)
        super.finalizedBy(KlutterGradleTaskName.GenerateFlutterLib.taskName)
    }

    override fun klutterTask(): KlutterTask =
        CompileProtoSchemaTask(super.project().root.folder)

}
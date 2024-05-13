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
package dev.buijs.klutter.kore.tasks.codegen

import dev.buijs.klutter.kore.KlutterException
import dev.buijs.klutter.kore.KlutterTask
import dev.buijs.klutter.kore.common.maybeCreateFolder
import dev.buijs.klutter.kore.common.verifyExists
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.protobuf.schema.ProtoBufSchemaGenerator
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions

/**
 * Create an instance of [GenerateProtoSchemasTask] if the List
 * [GenerateCodeOptions.responseClassNames] is not empty.
 */
fun GenerateCodeOptions.toGenerateProtoSchemaTask() =
    if(responseClassNames.isNotEmpty()) {
        GenerateProtoSchemasTask(project.root.folder,
            gradleBuildInstanceClassLoader = this::class.java.classLoader)
    } else {
        object: GenerateCodeAction {
            override fun run() { }
        }
    }


/**
 * Generate the protocol buffer schemas.
 */
class GenerateProtoSchemasTask(
    pathToRoot: File,
    /**
     * Classloader instance which is retried from the Build_gradle class.
     *
     * Getting the Classloader through this instance is the only way to ensure
     * all dependencies from the SourceSet are present. If any dependency is missing,
     * then it is not possible to get the SerializationStrategy of the Serializable
     * Response classes.
     */
    private val gradleBuildInstanceClassLoader: ClassLoader,
): KlutterTask, GenerateCodeAction {

    private val pathToBuild = pathToRoot.resolve("platform/build")

    @OptIn(ExperimentalSerializationApi::class)
    override fun run() {
        val pathToSource = pathToBuild
            .resolve("klutter/response")

        if(!pathToSource.exists()) return

        val pathToOutput = pathToBuild
            .resolve("klutter/protoschema")
            .maybeCreateFolder(clearIfExists = true)

        val classNames = pathToSource
            .listFiles { _, fn -> fn.startsWith("proto_") }
            ?.map { it.readText() }
            ?.toSet()
            ?: emptySet()

        val url = pathToBuild
            .resolve("intermediates/aar_main_jar/release/classes.jar")
            .verifyExists()
            .toURI()
            .toURL()

        val classLoader = URLClassLoader(arrayOf<URL>(url), gradleBuildInstanceClassLoader)

        for (className in classNames) {
            val classToLoad = Class.forName(className, true, classLoader)

            val companionObject = classToLoad.kotlin.companionObject
                ?: throw KlutterException("Failed to find Companion")

            val companionInstance = classToLoad.kotlin.companionObjectInstance
                ?: throw KlutterException("Failed to find Companion Instance")

            val functionEx = companionObject.functions.firstOrNull { it.name == "serializer" }
                ?: throw KlutterException("Method 'serializer' not found in list of methods: ${companionObject.functions.map { it.name }}")

            val result: KSerializer<*> = functionEx.call(companionInstance) as KSerializer<*>

            val schema = ProtoBufSchemaGenerator.generateSchemaText(result.descriptor)

            pathToOutput.resolve("${className.lowercase()}.proto").let { file ->
                file.createNewFile()
                file.writeText(schema)
            }
        }
    }

}
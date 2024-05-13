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

import dev.buijs.klutter.kore.KlutterTask
import dev.buijs.klutter.kore.common.maybeCreate
import dev.buijs.klutter.kore.common.maybeCreateFolder
import dev.buijs.klutter.kore.common.write
import dev.buijs.klutter.kore.templates.ProtobufExtensions
import java.io.File

const val protoGenMarker = "ProtocolBufferGenerated"

fun GenerateCodeOptions.toGenerateProtoExtensionsTask() =
    GenerateProtoExtensionsTask(project.platform.folder
        .resolve("build")
        .resolve("generated")
        .resolve("ksp")
        .resolve("metadata")
        .resolve("commonMain")
        .resolve("kotlin")
        , responseClassNames)

class GenerateProtoExtensionsTask(
    private val sourceFolder: File,
    private val responseClassNames: List<String>,
) : KlutterTask, GenerateCodeAction {
    override fun run() {
        for(fqdn in responseClassNames) {
            val packageName = fqdn
                .substringBeforeLast(".")

            val pathToPackage = packageName
                .replace(".", "/")

            val className = fqdn.substringAfterLast(".")

            sourceFolder
                .resolve(pathToPackage)
                .maybeCreateFolder()
                .resolve("$protoGenMarker$className.kt")
                .maybeCreate()
                .write(ProtobufExtensions(packageName, className))
        }
    }
}
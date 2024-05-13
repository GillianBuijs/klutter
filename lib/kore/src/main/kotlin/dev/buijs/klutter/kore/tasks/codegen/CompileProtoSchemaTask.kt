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
import dev.buijs.klutter.kore.common.verifyExists
import dev.buijs.klutter.kore.project.kradleHome
import dev.buijs.klutter.kore.tasks.executor
import java.io.File

fun GenerateCodeOptions.toCompileProtoSchemaTask() =
    CompileProtoSchemaTask(project.root.folder)

/**
 * Generate protocol buffer schema.
 */
class CompileProtoSchemaTask(
    private val rootFolder: File,
): KlutterTask, GenerateCodeAction {

    override fun run() {

        val pathToBuild = rootFolder.resolve("platform/build")

        val sourceFolder = pathToBuild.resolve("klutter/protoschema")

        if(!sourceFolder.exists()) return

        val protoc = kradleHome
            .resolve(".cache")
            .resolve("protobuf")
            .resolve("protoc")
            .resolve("bin")
            .resolve("protoc")
            .verifyExists()
            .absolutePath

        val destination = rootFolder
            .resolve("lib/src")
            .verifyExists()
            .absolutePath

        val dartExecutable = kradleHome
            .resolve(".cache")
            .resolve("protobuf")
            .resolve("protoc_plugin.exe")
            .verifyExists()
            .absolutePath

        val schemas = sourceFolder
            .listFiles()
            ?.map { it.absolutePath }
            ?.toSet()
            ?: emptySet()

        val destinationFile = File(destination)

        for(pathToSchemaFile in schemas) {
            executor.execute(
                runFrom = rootFolder,
                command = "$protoc -I=${sourceFolder.absolutePath} --dart_out=$destination $pathToSchemaFile --plugin protoc-gen-dart=$dartExecutable")

            File(pathToSchemaFile).renameProtoGeneratedFiles(destinationFile)
            File(pathToSchemaFile).deleteObsoleteGeneratedFiles(destinationFile)
        }

    }

}

internal fun File.renameProtoGeneratedFiles(destination: File) = buildList {
    val generatedFileName = nameWithoutExtension.substringAfterLast(".")
    val generatedFile = destination.resolve("$nameWithoutExtension.pb.dart")
    val renamedFile = destination.resolve("${generatedFileName.substringAfterLast(".")}.pb.dart")
    generatedFile.copyTo(renamedFile)
    generatedFile.delete()

    if(renamedFile.readText().contains("class ")) {
        add(renamedFile.verifyExists())
    } else {
        renamedFile.delete()
    }

    val generatedEnumFile = destination.resolve("$nameWithoutExtension.pbenum.dart")
    val renamedEnumFile = destination.resolve("${generatedFileName.substringAfterLast(".")}.pbenum.dart")
    generatedEnumFile.copyTo(renamedEnumFile)
    generatedEnumFile.delete()

    if(renamedEnumFile.readText().contains("class ")) {
        add(renamedEnumFile.verifyExists())
    } else {
        renamedEnumFile.delete()
    }
}

internal fun File.deleteObsoleteGeneratedFiles(destination: File) {
    destination.resolve("$nameWithoutExtension.pbjson.dart").delete()
    destination.resolve("$nameWithoutExtension.pbserver.dart").delete()
}
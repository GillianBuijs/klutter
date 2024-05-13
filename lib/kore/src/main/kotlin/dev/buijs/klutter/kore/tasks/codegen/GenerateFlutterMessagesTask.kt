/* Copyright (c) 2021 - 2023 Buijs Software
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
import dev.buijs.klutter.kore.ast.*
import dev.buijs.klutter.kore.common.*
import dev.buijs.klutter.kore.project.*
import dev.buijs.klutter.kore.tasks.execute
import java.io.File

fun GenerateCodeOptions.toGenerateFlutterMessagesTask() =
    if(responseClassNames.isEmpty()) {
        GenerateFlutterMessagesTask(
            root = project.root,
            srcFolder = flutterSrcFolder,
            flutterFolder = flutterFolder,
            messages = messages,
            log = log)
    } else {
        object: GenerateCodeAction {
            override fun run() { }
        }
    }

/**
 * Generate the Flutter (dart) code in root/lib folder of the plugin project.
 */
class GenerateFlutterMessagesTask(
    private val root: Root,
    private val srcFolder: File,
    flutterFolder: FlutterDistributionFolderName,
    private val messages: List<SquintMessageSource>,
    private val log: (String) -> Unit = {  },
) : KlutterTask, GenerateCodeAction {

    private val flutter = flutterExecutable(flutterFolder).absolutePath

    override fun run() {
        messages.forEach { message ->
            squintJsonGenerate(message.source!!)
        }
        messages.forEach { message ->
            squintJsonGenerateSerializers(message.type)
        }
    }

    private fun squintJsonGenerate(sourceFile: File) {
        val command = "$flutter pub run squint_json:generate" +
                " --type dataclass" +
                " --input ${sourceFile.absolutePath}" +
                " --output ${srcFolder.absolutePath}" +
                " --overwrite true" +
                " --generateChildClasses false" +
                " --includeCustomTypeImports true"
        command.execute(root.folder).also { log(it) }
    }

    private fun squintJsonGenerateSerializers(type: AbstractType) {
        val command = "$flutter pub run squint_json:generate" +
                " --type serializer" +
                " --input ${srcFolder.resolve("${type.className.toSnakeCase()}_dataclass.dart")}" +
                " --output ${srcFolder.absolutePath}" +
                " --overwrite true"
        command.execute(root.folder).also { log(it) }
    }

}
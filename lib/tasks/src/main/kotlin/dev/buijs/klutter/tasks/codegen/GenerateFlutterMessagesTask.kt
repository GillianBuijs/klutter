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
package dev.buijs.klutter.tasks.codegen

import dev.buijs.klutter.kore.KlutterTask
import dev.buijs.klutter.kore.ast.*
import dev.buijs.klutter.kore.common.*
import dev.buijs.klutter.kore.project.*
import dev.buijs.klutter.tasks.execute
import java.io.File

fun GenerateCodeOptions.toGenerateFlutterMessagesTask() =
    GenerateFlutterMessagesTask(
        root = project.root,
        srcFolder = flutterSrcFolder,
        messages = messages,
        log = log)

/**
 * Generate the Flutter (dart) code in root/lib folder of the plugin project.
 */
class GenerateFlutterMessagesTask(
    private val root: Root,
    private val srcFolder: File,
    private val messages: List<SquintMessageSource>,
    private val log: (String) -> Unit = {  },
) : KlutterTask, GenerateCodeAction {

    override fun run() {
        val enums = messages.filter { it.type is EnumType  }
        val clazz = messages.filter { it.type !is EnumType }

        enums.forEach { message ->
            message.source?.squintJsonGenerate()
        }

        clazz.forEach { message ->
            message.source?.squintJsonGenerate()
            squintJsonGenerateSerializers(message.type)
        }
    }

    private fun File.squintJsonGenerate() {
        val command = "flutter pub run squint_json:generate" +
                " --type dataclass" +
                " --input $this" +
                " --output ${srcFolder.absolutePath}" +
                " --overwrite true" +
                " --generateChildClasses false" +
                " --includeCustomTypeImports true"
        command.execute(root.folder).also { log(it) }
    }

    private fun squintJsonGenerateSerializers(type: AbstractType) {
        val command = "flutter pub run squint_json:generate" +
                " --type serializer" +
                " --input ${srcFolder.resolve("${type.className.toSnakeCase()}_dataclass.dart")}" +
                " --output ${srcFolder.absolutePath}" +
                " --overwrite true"
        command.execute(root.folder).also { log(it) }
    }

}
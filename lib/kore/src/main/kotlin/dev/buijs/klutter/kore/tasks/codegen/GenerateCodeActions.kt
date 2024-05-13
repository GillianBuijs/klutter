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

import dev.buijs.klutter.kore.KlutterException
import dev.buijs.klutter.kore.common.ExcludeFromJacocoGeneratedReport

@ExcludeFromJacocoGeneratedReport(
    reason = """
        Most is actually covered 
        but can NOT test with Spock directly 
        due to it being a reified function.
    """)
inline fun <reified T: GenerateCodeAction> findGenerateCodeAction(
    options: GenerateCodeOptions
): GenerateCodeAction = when(T::class.java) {
    DartFormatTask::class.java ->
        options.toDartFormatTask()
    ExcludeArm64Task::class.java ->
        options.toExcludeArm64Task()
    GenerateAndroidLibTask::class.java ->
        options.toGenerateAndroidLibTask()
    GenerateCodeTask::class.java ->
        options.toGenerateCodeTask()
    GenerateFlutterControllersTask::class.java ->
        options.toGenerateControllersTask()
    GenerateFlutterLibTask::class.java ->
        options.toGenerateFlutterLibTask()
    GenerateIosLibTask::class.java ->
        options.toGenerateIosLibTask()
    GenerateFlutterMessagesTask::class.java ->
        options.toGenerateFlutterMessagesTask()
    PubGetTask::class.java ->
        options.toPubGetTask()
    GenerateProtoSchemasTask::class.java ->
        options.toGenerateProtoSchemaTask()
    CompileProtoSchemaTask::class.java ->
        options.toCompileProtoSchemaTask()
    GenerateProtoExtensionsTask::class.java ->
        options.toGenerateProtoExtensionsTask()
    else -> throw KlutterException("Unknown GenerateCodeAction: ${T::class.java}")
}

fun runGenerateCodeActions(vararg action: GenerateCodeAction) {
    action.toList().forEach { it.run() }
}

interface GenerateCodeAction {
    fun run()
}
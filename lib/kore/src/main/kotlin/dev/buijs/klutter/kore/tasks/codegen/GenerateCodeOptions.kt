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
@file:Suppress("MemberVisibilityCanBePrivate")
package dev.buijs.klutter.kore.tasks.codegen

import dev.buijs.klutter.kore.ast.*
import dev.buijs.klutter.kore.common.toSnakeCase
import dev.buijs.klutter.kore.common.verifyExists
import dev.buijs.klutter.kore.project.*
import java.io.File

data class GenerateCodeOptions(
    val project: Project,
    val pubspec: Pubspec,
    val flutterFolder: FlutterDistributionFolderName,
    val excludeArmArcFromPodspec: Boolean,
    val controllers: List<Controller>,
    val messages: List<SquintMessageSource>,

    /**
     * List of FQDN Responses for which protobuf schemas should be generated.
     */
    val responseClassNames: List<String>,
    val log: (String) -> Unit = {  },
) {

    val pluginName: String
        get() = pubspec.name ?: "klutter_library"

    val methodChannelName: String
        get() = pubspec.android?.pluginPackage ?: "$pluginName.klutter"

    val bindings: Map<Controller, List<FlutterChannel>>
        get() = controllers.associate { controller ->
            when {
                controller is SimpleController ->
                    controller to listOf(FlutterSyncChannel("$methodChannelName/channel/${controller.className.toSnakeCase()}"))

                controller.functions.isEmpty() ->
                    controller to listOf(FlutterAsyncChannel("$methodChannelName/channel/${controller.className.toSnakeCase()}"))

                else ->
                    controller to listOf(
                        FlutterAsyncChannel("$methodChannelName/channel/async/${controller.className.toSnakeCase()}"),
                        FlutterSyncChannel("$methodChannelName/channel/sync/${controller.className.toSnakeCase()}"))
            }
        }

    val flutterLibFolder: File
        get() = project.root.pathToLibFolder

    val flutterSrcFolder: File
        get() = flutterLibFolder.resolve("src")

}

internal fun GenerateCodeOptions.clearFlutterLibFolder() {
    flutterLibFolder.deleteRecursively()
    flutterLibFolder.mkdir()
}

internal fun GenerateCodeOptions.clearFlutterSrcFolder() {
    flutterSrcFolder.mkdir()
    flutterSrcFolder.verifyExists()
}
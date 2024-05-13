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
import dev.buijs.klutter.kore.templates.flutter.*
import java.io.File

fun GenerateCodeOptions.toGenerateControllersTask() =
    GenerateFlutterControllersTask(
        srcFolder = flutterSrcFolder,
        bindings = bindings,
        isProtobufEnabled = responseClassNames.isNotEmpty())

/**
 * Generate the Flutter (dart) code in root/lib folder of the plugin project.
 */
class GenerateFlutterControllersTask(
    private val srcFolder: File,
    private val bindings: Map<Controller, List<FlutterChannel>>,
    private val isProtobufEnabled: Boolean,
) : KlutterTask, GenerateCodeAction {

    override fun run() {
        bindings.forEach { (controller, channels) ->
            when (controller) {
                is SimpleController ->
                    srcFolder.writeSimpleController(
                        controller = controller,
                        channel = channels.firstSyncChannelOrNull()!!,
                        isProtobufEnabled = isProtobufEnabled)
                is BroadcastController ->
                    srcFolder.writeBroadcastController(
                        controller = controller,
                        asyncChannel = channels.firstAsyncChannelOrNull()!!,
                        syncChannel = channels.firstSyncChannelOrNull(),
                        isProtobufEnabled = isProtobufEnabled)
            }
        }
    }

}

private fun List<FlutterChannel>.firstSyncChannelOrNull() =
    filterIsInstance<FlutterSyncChannel>().firstOrNull()

private fun List<FlutterChannel>.firstAsyncChannelOrNull() =
    filterIsInstance<FlutterAsyncChannel>().firstOrNull()

private fun File.writeBroadcastController(
    controller: BroadcastController,
    asyncChannel: FlutterAsyncChannel,
    syncChannel: FlutterSyncChannel?,
    isProtobufEnabled: Boolean,
) {

    val parentFolder = this
        .resolve(controller.className.toSnakeCase())
        .maybeCreateFolder()

    parentFolder.resolve("controller.dart")
        .maybeCreate()
        .write(SubscriberWidget(
            topic = controller.className.toSnakeCase(),
            channel = asyncChannel,
            controllerName = controller.className,
            dataType = controller.response))

    controller.functions
        .forEach { it.writePublisherWidget(parentFolder, syncChannel!!, isProtobufEnabled) }
}

private fun File.writeSimpleController(
    controller: SimpleController,
    channel: FlutterSyncChannel,
    isProtobufEnabled: Boolean,
) {
    val parentFolder =
        resolve(controller.className.toSnakeCase())
            .maybeCreateFolder()

    controller.functions
        .forEach { it.writePublisherWidget(parentFolder, channel, isProtobufEnabled) }
}

private fun Method.writePublisherWidget(
    parentFolder: File,
    channel: FlutterSyncChannel,
    isProtobufEnabled: Boolean,
) =  parentFolder.resolve("${method.toSnakeCase()}.dart")
    .also { it.createNewFile() }
    .write(PublisherWidget(
        isProtobufEnabled = isProtobufEnabled,
        channel = channel,
        event = FlutterEvent(command),
        extension = FlutterExtension("${command.replaceFirstChar { it.uppercase() }}Event"),
        requestType = requestDataType?.let { FlutterMessageType(it) },
        responseType = FlutterMessageType(responseDataType),
        method = FlutterMethod(method)).createPrinter())
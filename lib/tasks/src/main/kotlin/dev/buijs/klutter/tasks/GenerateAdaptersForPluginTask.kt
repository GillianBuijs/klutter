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
package dev.buijs.klutter.tasks

import dev.buijs.klutter.kore.KlutterException
import dev.buijs.klutter.kore.KlutterTask
import dev.buijs.klutter.kore.ast.*
import dev.buijs.klutter.kore.common.*
import dev.buijs.klutter.kore.project.*
import dev.buijs.klutter.kore.templates.*
import dev.buijs.klutter.kore.templates.flutter.*
import java.io.File

/**
 * Task to generate the boilerplate code required to
 * let Kotlin Multiplatform and Flutter communicate.
 */
class GenerateAdaptersForPluginTask(
    private val android: Android,
    private val ios: IOS,
    private val root: Root,
    private val methodChannelName: String,
    private val pluginName: String,
    private val excludeArmArcFromPodspec: Boolean,
    private val controllers: List<Controller>,
    private val metadata: List<SquintMessageSource>,
    private val log: (String) -> Unit = {  },
) : KlutterTask {

    override fun run() {

        val folder = root.pathToLibFolder.also {
            it.deleteRecursively()
            it.mkdir()
        }

        val srcFolder = folder.resolve("src")
            .also { it.mkdir() }
            .verifyExists()

        "flutter pub get".execute(root.folder)

        metadata.forEach { message ->
            var command = "flutter pub run squint_json:generate" +
                    " --type dataclass" +
                    " --input ${message.source}" +
                    " --output ${srcFolder.absolutePath}" +
                    " --overwrite true" +
                    " --generateChildClasses false" +
                    " --includeCustomTypeImports true"
            command.execute(root.folder).also { log(it) }

            // Serializer code does not need to be generated for enumerations.
            if(message.type is CustomType) {
                command = "flutter pub run squint_json:generate" +
                        " --type serializer" +
                        " --input ${srcFolder.resolve("${message.type.className.toSnakeCase()}_dataclass.dart")}" +
                        " --output ${srcFolder.absolutePath}" +
                        " --overwrite true"
                command.execute(root.folder).also { log(it) }
            }
        }

        val eventChannelNames = mutableSetOf<String>()
        val methodChannelNames = mutableSetOf<String>()

        controllers.filterIsInstance<BroadcastController>().forEach { controller ->
            srcFolder
                .resolve(controller.className.toSnakeCase())
                .also { if(!it.exists()) it.mkdir() }
                .resolve("controller.dart")
                .maybeCreate()
                .write(
                    SubscriberWidget(
                        topic = controller.className.toSnakeCase(),
                        channel = "$methodChannelName/channel/${controller.className.toSnakeCase()}"
                            .also { eventChannelNames.add(it) },
                        controllerName = controller.className,
                        dataType = controller.response
                    )
                )
        }

        controllers.filterIsInstance<RequestScopedController>().forEach { controller ->
            val parentFolder = srcFolder.resolve(controller.className.toSnakeCase()).also {
                if(!it.exists()) it.mkdir()
            }
            controller.functions.forEach { function ->
                parentFolder.resolve("${function.method.toSnakeCase()}.dart")
                    .also { it.createNewFile() }
                    .write(
                        PublisherWidget(
                            channel = FlutterChannel("$methodChannelName/channel/${controller.className.toSnakeCase()}")
                                .also { methodChannelNames.add(it.name) },
                            event = FlutterEvent(function.command),
                            extension = FlutterExtension("${function.command.replaceFirstChar { it.uppercase()}}Event"),
                            requestType = function.requestDataType?.let { FlutterMessageType(it) },
                            responseType = FlutterMessageType(function.responseDataType),
                            method = FlutterMethod(function.method),
                        ).createPrinter()
                    )
            }
        }

        root.pathToLibFile.maybeCreate().write(
            PackageLib(
                name = pluginName,
                exports = srcFolder.walkTopDown()
                    .toList()
                    .filter { it.isFile }
                    .map { it.relativeTo(srcFolder) }
                    .map { it.path.prefixIfNot("src/") }
            )
        )

        "dart format .".execute(root.folder).also { log(it) }

        if(excludeArmArcFromPodspec) {
            ios.podspec().excludeArm64("dependency'Flutter'")
        }

        ios.pathToPlugin.maybeCreate().write(
            IosAdapter(
                pluginClassName = ios.pluginClassName,
                methodChannels = methodChannelNames,
                eventChannels = eventChannelNames,
                controllers = controllers.toSet(),
            )
        )

        android.pathToPlugin.maybeCreate().write(
            AndroidAdapter(
                pluginClassName = android.pluginClassName,
                pluginPackageName = android.pluginPackageName,
                methodChannels = methodChannelNames,
                eventChannels = eventChannelNames,
                controllers = controllers.toSet(),
            )
        )

    }

}


/**
 * Visitor which adds EXCLUDED_ARCHS for iphone simulator if not present.
 *
 * These exclusions are needed to be able to run the app on a simulator.
 */
internal fun File.excludeArm64(insertAfter: String) {

    var hasExcludedPod = false

    var hasExcludedUsr = false

    val pod = ".pod_target_xcconfig = { 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64' }"

    val usr = ".user_target_xcconfig = { 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64' }"

    val text = readText()

    if(text.contains(pod)) {
        hasExcludedPod = true
    }

    if(text.contains(usr)) {
        hasExcludedUsr = true
    }

    val regex = "Pod::Spec.new.+?do.+?.([^|]+).".toRegex()

    /** Check the prefix used in the podspec or default to 's'.
     *
     *  By default, the podspec file uses 's' as prefix.
     *  In case a podspec does not use this default,
     *  this regex will find the custom prefix.
     *
     *  If not found then 's' is used.
     */
    val fromRegex = regex.find(text)

    val prefix = if(fromRegex == null) "s" else fromRegex.groupValues[1]

    // INPUT
    val lines = readLines()

    // OUTPUT
    val newLines = mutableListOf<String>()

    for(line in lines){
        newLines.add(line)

        // Check if line contains Flutter dependency (which should always be present).
        // If so then add the vendored framework dependency.
        // This is done so the line is added at a fixed point in the podspec.
        if(line.filter { !it.isWhitespace() }.contains("$prefix.$insertAfter")) {

            if(!hasExcludedPod) {
                newLines.add("""  $prefix$pod""")
                hasExcludedPod = true
            }

            if(!hasExcludedUsr) {
                newLines.add("""  $prefix$usr""")
                hasExcludedUsr = true
            }

        }

    }

    if(hasExcludedPod && hasExcludedUsr) {
        // Write the editted line to the podspec file.
        writeText(newLines.joinToString("\n") { it })
    } else {
        throw KlutterException(
            """
          |Failed to add exclusions for arm64.
          |
          |Unable to find the following line in file $path:
          |- '$prefix.$insertAfter'
          |
          |""".trimMargin(),
        )
    }

}
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
package dev.buijs.klutter.compiler.processor

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import dev.buijs.klutter.kore.KlutterException
import dev.buijs.klutter.compiler.processor.ProcessorOption.*
import dev.buijs.klutter.kore.common.verifyExists
import dev.buijs.klutter.kore.project.*
import java.io.File

internal var dryRun = false

/**
 * Parsed ksp options used by [Processor].
 * </br>
 */
data class ProcessorOptions(
    val projectFolder: File,
    val outputFolder: File,
    val flutterVersion: String,
    val generateAdapters: Boolean,
    val isProtobufEnabled: Boolean,
)

/**
 * Possible options that can be set in the ksp DSL to configure Klutter code scanning/generation.
 */
internal enum class ProcessorOption(val value: String) {
    PROJECT_FOLDER("klutterProjectFolder"),
    OUTPUT_FOLDER("klutterOutputFolder"),
    GENERATE_ADAPTERS("klutterGenerateAdapters"),
    FLUTTER_SDK_VERSION("flutterVersion"),
    PROTOBUF_ENABLED("klutterProtobufEnabled")
}

/**
 * Parse options set in the ksp DSL and return a [ProcessorOptions] object
 * or throw a [KlutterException] if required options are missing/invalid.
 */
internal fun processorOptions(
    env: SymbolProcessorEnvironment,
    kradleEnv: File,
    kradleYaml: File,
): ProcessorOptions {

    val options = env.options

    val kradleEnvContent = if(kradleEnv.exists()) {
        kradleEnv.resolveProjectPropertiesOrThrow().resolveSystemPropertiesOrThrow().readText()
    } else {
        null
    }

    val kradleYamlContent = if(kradleYaml.exists()) {
        kradleYaml.resolveProjectPropertiesOrThrow().resolveSystemPropertiesOrThrow().readText()
    } else {
        null
    }

    val outputFolder = findOutputPathInKradleEnvOrNull(kradleEnvContent)
    val skipCodeGen = findSkipCodeGenInKradleEnvOrNull(kradleEnvContent)
    val flutterOrNull = findFlutterVersionInKradleYamlOrNull(kradleYamlContent)

    return ProcessorOptions(
        projectFolder = options.projectFolder(),
        outputFolder = outputFolder?.let { File(it) } ?: options.outputFolder(),
        generateAdapters = skipCodeGen?.let { !it } ?: options.boolean(GENERATE_ADAPTERS),
        flutterVersion = flutterOrNull ?: options.flutterVersion(),
        isProtobufEnabled = options.boolean(PROTOBUF_ENABLED, defaultValue = false),
    ).also { kcLogger?.info("Determined Processor Options: $it") }
}

/**
 * Parse required ksp option which contains path to the build directory.
 */
internal fun Map<String,String>.projectFolder(): File {
    val option = PROJECT_FOLDER.value
    val pathToScanFolder = this[option]
        ?: throw KlutterException("""Option $option not set! 
                |Add this option to the ksp DSL, example: 
                |```
                |ksp {
                |    arg("$option", project.buildDir.absolutePath)
                |}
                |```
                |""".trimMargin())

    return File(pathToScanFolder).also { it.verifyExists() }
}

/**
 * Parse required ksp option which contains path to the project output folder.
 */
private fun Map<String,String>.outputFolder(): File {
    val option = OUTPUT_FOLDER.value
    val pathToOutputFolder = this[option]
        ?: throw KlutterException("""Option $option not set!
                |Add this option to the ksp DSL, example:
                |```
                |ksp {
                |    arg("$option", project.buildDir.absolutePath)
                |}
                |```
                |""".trimMargin())
    return File(pathToOutputFolder).also { it.verifyExists() }
}

/**
 * Parse required ksp option which contains path to the flutter bin folder.
 */
private fun Map<String,String>.flutterVersion(): String {
    val option = FLUTTER_SDK_VERSION.value
    return this[option]
        ?: throw KlutterException("""Option $option not set!
                |Add this option to the ksp DSL, example:
                |```
                |ksp {
                |    arg("$option", <Flutter Version in format major.minor.patch, example: 3.0.5)
                |}
                |```
                |""".trimMargin())
}

/**
 * Return option argument as boolean value or default to true if not set.
 */
private fun Map<String,String>.boolean(option: ProcessorOption, defaultValue: Boolean = true) =
    this[option.value]?.let { it.trim().lowercase() == "true" } ?: defaultValue
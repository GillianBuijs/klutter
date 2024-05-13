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

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import dev.buijs.klutter.compiler.scanner.scanForControllers
import dev.buijs.klutter.compiler.scanner.scanForResponses
import dev.buijs.klutter.compiler.scanner.scanForResponsesProtobuf
import dev.buijs.klutter.compiler.validator.*
import dev.buijs.klutter.compiler.wrapper.KCLogger
import dev.buijs.klutter.kore.ast.Controller
import dev.buijs.klutter.kore.ast.CustomType
import dev.buijs.klutter.kore.ast.SquintMessageSource
import dev.buijs.klutter.kore.common.EitherNok
import dev.buijs.klutter.kore.common.EitherOk
import dev.buijs.klutter.kore.common.ExcludeFromJacocoGeneratedReport
import dev.buijs.klutter.kore.common.verifyExists
import dev.buijs.klutter.kore.project.*
import dev.buijs.klutter.kore.tasks.codegen.GenerateCodeOptions
import dev.buijs.klutter.kore.tasks.codegen.GenerateCodeTask
import dev.buijs.klutter.kore.tasks.codegen.findGenerateCodeAction
import dev.buijs.klutter.kore.tasks.project.DownloadFlutterTask

internal var kcLogger: KCLogger? = null

/**
 * The actual symbol processor which will scan all classes with Klutter annotations
 * and run code generation tasks as configured.
 */
@ExcludeFromJacocoGeneratedReport(reason = """
    The Processor requires an actual Resolver implementation to work properly.
    Stubbing and/or mocking is possible to some extent but is very maintenance intensive 
    without providing actual insight to the code quality. 
    
    It's better to NOT unit test the Processor but to run a System and/or Integration Test
    which uses a real Kotlin Multiplatform Project.
""")
class Processor(
    private val log: KSPLogger,
    env: SymbolProcessorEnvironment,
): SymbolProcessor {

    private val projectRoot = env.options.projectFolder()
    private val project = projectRoot.plugin()
    private val pubspec = project.root.toPubspec()
    internal val options = processorOptions(env, project.root.kradleEnvFile, project.root.kradleYaml)
    private var messages: List<SquintMessageSource>? = null
    private var controllers: List<Controller>? = null

    /**
     * List of FQDN names for each @Response annotated class.
     *
     * If protobuf is NOT enabled then this list stays empty.
     */
    private var responseClassNames: List<String>? = null

    private val flutterFolder: FlutterDistributionFolderName
        get() {
            val os = currentOperatingSystem
            val arch = currentArchitecture
            val version = options.flutterVersion
            val folder = FlutterDistributionFolderName("$version.$os.$arch".lowercase())
            val exe = flutterExecutable(folder)
            if(!exe.exists()) {
                DownloadFlutterTask(FlutterDistribution(
                    os = currentOperatingSystem,
                    arch = arch,
                    version = version
                        .split(".")
                        .let { Version(it[0].toInt(), it[1].toInt(), it[2].toInt()) }
                )).run()
            }
            exe.verifyExists()
            return folder
        }


    @ExcludeFromJacocoGeneratedReport
    override fun process(resolver: Resolver): List<KSAnnotated> {
        kcLogger = KCLogger(log, options.outputFolder)
        kcLogger!!.logCompilerOptions(options)

        responseClassNames = if(options.isProtobufEnabled) {
            resolver.findAndValidateResponsesForProtobuf()
        } else {
            emptyList()
        }

        messages = resolver.findAndValidateMessages()
        controllers = resolver.findAndValidateControllers()
        generateCodeAndInstall()
        return emptyList()
    }

    @ExcludeFromJacocoGeneratedReport
    private fun Resolver.findAndValidateMessages() =
        scanForResponses(resolver = this, outputFolder = options.outputFolder, isProtobufEnabled = options.isProtobufEnabled)
            .validateResponses().let {
                when (it) {
                    is ValidSquintMessages -> it.data
                    is InvalidSquintMessages -> {
                        kcLogger?.logAnnotationScanningWarnings(it.data, "@Response")
                        null
                    }
                }
            }

    /**
     * Returns the fully qualified class names of @Response annotated classes.
     */
    @ExcludeFromJacocoGeneratedReport
    private fun Resolver.findAndValidateResponsesForProtobuf() =
        scanForResponsesProtobuf(resolver = this, outputFolder = options.outputFolder)
            .let { results ->
                when {
                    results.any { result -> result is EitherNok } -> {
                        val warnings = results
                            .filterIsInstance<EitherNok<String, String>>()
                            .map { it.data }
                        kcLogger?.logAnnotationScanningWarnings(warnings, "@Response")
                        null
                    }

                    else -> results
                        .filterIsInstance<EitherOk<String, String>>()
                        .map { it.data }
                }
            }

    @ExcludeFromJacocoGeneratedReport
    private fun Resolver.findAndValidateControllers() =
        scanForControllers(
            resolver = this,
            outputFolder = options.outputFolder,
            responses = messages
                ?.map { it.type }
                ?.toSet()
                ?: responseClassNames
                    ?.map { CustomType(
                        className = it.substringAfterLast("."),
                        packageName = it.substringBeforeLast("."))
                    }
                    ?.toSet()
                ?: emptySet())
            .validateControllers(messages?.map { it.type } ?: emptyList()).let {
                when (it) {
                    is Valid -> it.data
                    is Invalid -> {
                        kcLogger?.logAnnotationScanningWarnings(it.data, "@Controller")
                        null
                    }
                }
            }


    @ExcludeFromJacocoGeneratedReport
    private fun generateCodeAndInstall() {
        // If null then there are validation errors and code generation should be skipped
        if (messages == null || controllers == null || responseClassNames == null) return

        val responsesCount = if(messages!!.isEmpty()) {
            responseClassNames!!.size
        } else {
            messages!!.size
        }

        kcLogger?.logDartCodeGenInfo(project, responsesCount, controllers!!.size)

        val codegenOptions = GenerateCodeOptions(
            project = project,
            pubspec = pubspec,
            excludeArmArcFromPodspec = currentArchitecture == Architecture.X64,
            controllers = controllers!!,
            messages = messages!!,
            responseClassNames = responseClassNames!!,
            flutterFolder = flutterFolder,
            log = { str -> kcLogger?.info("Running dart command:\n$str") })

        findGenerateCodeAction<GenerateCodeTask>(codegenOptions).run()
    }

}

@ExcludeFromJacocoGeneratedReport
private fun KCLogger.logAnnotationScanningWarnings(warnings: List<String>, annotation: String) {
    warn("Fatal error occured while processing $annotation classes")
    warn("=============================================================")
    warnings.forEach { warn(it) }
    warn("=============================================================")
}

@ExcludeFromJacocoGeneratedReport
private fun KCLogger.logDartCodeGenInfo(project: Project, responseCount: Int, controllerCount: Int) {
    info("=============================================================")
    info("Generating dart code")
    info("Root folder: ${project.root.folder.absolutePath}")
    info("Response count: $responseCount")
    info("Controller count: $controllerCount")
    info("=============================================================")
}

@ExcludeFromJacocoGeneratedReport
private fun KCLogger.logCompilerOptions(options: ProcessorOptions) {
    info("=============================================================")
    info("Compiler Options")
    info("Project folder: ${options.projectFolder}")
    info("Output folder: ${options.outputFolder}")
    info("Flutter version: ${options.flutterVersion}")
    info("Adapter codegen isEnabled: ${options.generateAdapters}")
    info("Protobuf isEnabled: ${options.isProtobufEnabled}")
    info("=============================================================")
}
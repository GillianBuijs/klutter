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
@file:Suppress("unused")
package dev.buijs.klutter.gradle.dsl

import com.google.devtools.ksp.gradle.KspExtension
import dev.buijs.klutter.kore.common.ExcludeFromJacocoGeneratedReport
import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import java.io.File

/**
 * Glue for the DSL used in a build.gradle(.kts) file and the Klutter tasks.
 */
@Suppress("MemberVisibilityCanBePrivate")
open class KlutterExtension(private val project: Project) {

    private val handler = KlutterDependencyHandler(project)

    private val defaultRoot = project.rootDir

    var root: File? = null

    @Internal
    internal var plugin: Dto? = null

    internal val features = mutableSetOf<KlutterFeature>()

    internal val isProtobufEnabled: Boolean
        get() = features.contains(KlutterFeature.PROTOBUF)

    /**
     * Configure the Gradle Plugin for a klutter plugin (consumer or producer).
     */
    fun plugin(lambda: KlutterPluginBuilder.() -> Unit) {
        plugin = KlutterPluginBuilder().apply(lambda).build()
    }

    /**
     * Enable or disable specific klutter features.
     *
     * See for feature list: [KlutterFeature].
     */
    fun feature(name: KlutterFeature, enable: Boolean = true) {
        if(enable) {
            features.add(name)
            project.extensions.getByType(KspExtension::class.java)
                .arg("klutterProtobufEnabled", "true")
        } else {
            features.remove(name)
            project.extensions.getByType(KspExtension::class.java)
                .arg("klutterProtobufEnabled", "false")
        }
    }

    /**
     * Add klutter implementation dependency to this project.
     */
    @JvmOverloads
    @ExcludeFromJacocoGeneratedReport
    fun include(simpleModuleName: String, version: String? = null, test: Boolean = false) {
        when {
            simpleModuleName == "bill-of-materials" || simpleModuleName == "bom"-> {
                handler.addKlutterImplementation("annotations", version)
                handler.addKlutterImplementation("kompose", version)
                handler.addKlutterImplementation("kore", version)
                handler.includeCompilerPlugin(version = version)
            }
            simpleModuleName == "embedded" -> {
                val configFile = (root ?: defaultRoot).resolve("kradle.yaml")
                configFile.embeddedDependenciesFromConfigFile().forEach {
                    handler.addImplementation(it)
                }
            }
            simpleModuleName == "compiler" ->
                handler.includeCompilerPlugin(version = version)
            test ->
                includeTest(simpleModuleName, version)
            else ->
                handler.addKlutterImplementation(simpleModuleName, version)
        }
    }

    @JvmOverloads
    @ExcludeFromJacocoGeneratedReport
    fun compiler(
        version: String? = null,
        dependsOnKore: String? = null
    ) {
        handler.includeCompilerPlugin(
            version = version,
            versionKore = dependsOnKore)
    }

    /**
     * Add klutter testImplementation dependency to this project.
     */
    @JvmOverloads
    @ExcludeFromJacocoGeneratedReport
    fun includeTest(simpleModuleName: String, version: String? = null) {
        handler.addKlutterTestImplementation(simpleModuleName, version)
    }

}

/**
 * A Jetbrains 'kotlin' inspired dependency handler which makes it easier
 * to add klutter dependencies in a build.gradle(.kts) file as follows:
 *
 * ```
 * klutter {
 *      implementation("annotations")
 *      implementation("annotations", version = "2022.r8")
 * }
 * ```
 *
 */
class KlutterDependencyHandler(private val project: Project) {

    @ExcludeFromJacocoGeneratedReport
    fun includeCompilerPlugin(
        version: String? = null,
        versionKore: String? = null
    ) {
        project.dependencies.add(
            "kspCommonMainMetadata",
            "dev.buijs.klutter:compiler:${version?:KlutterVersion.byName("compiler")}")
        project.dependencies.add(
            "kspCommonMainMetadata",
            "dev.buijs.klutter:kore:${versionKore ?: KlutterVersion.byName("kore")}")
    }

    @ExcludeFromJacocoGeneratedReport
    fun addImplementation(dependencyNotation: String) {
        project.dependencies.add(
            "implementation",
            project.createDependency(dependencyNotation))
    }

    @ExcludeFromJacocoGeneratedReport
    fun addKlutterImplementation(simpleModuleName: String, version: String? = null) {
        val multiplatform =
            project.findKotlinMultiplatformExtension()

        if(multiplatform == null) {
            // Not a Multiplatform Project so apply normally.
            project.dependencies.add(
                "implementation",
                project.createKlutterDependency(simpleModuleName = simpleModuleName, version = version))
        } else {
            // Multiplatform Project so apply for all sourcesets.
            multiplatform.sourceSets.forEach {
                project.addKotlinMultiplatformDependency(
                    sourceset = it,
                    simpleModuleName = simpleModuleName,
                    version = version ?: KlutterVersion.byName(simpleModuleName))
            }
        }
    }

    @ExcludeFromJacocoGeneratedReport
    fun addKlutterTestImplementation(simpleModuleName: String, version: String? = null) {
        val multiplatform =
            project.findKotlinMultiplatformExtension()

        if(multiplatform == null) {
            // Not a Multiplatform Project so apply normally.
            project.dependencies.add("testImplementation", project.createKlutterDependency(
                simpleModuleName = simpleModuleName,
                version = version))
        } else {
            // Multiplatform Project so apply for all sourcesets.
            multiplatform.sourceSets.forEach {
                project.addKotlinMultiplatformTestDependency(
                    sourceset = it,
                    simpleModuleName = simpleModuleName,
                    version = version ?: KlutterVersion.byName(simpleModuleName))
            }
        }
    }

}


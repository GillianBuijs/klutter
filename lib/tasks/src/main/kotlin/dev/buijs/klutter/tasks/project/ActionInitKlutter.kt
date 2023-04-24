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
package dev.buijs.klutter.tasks.project

import dev.buijs.klutter.kore.project.*
import dev.buijs.klutter.tasks.execute
import dev.buijs.klutter.tasks.input.PluginName
import dev.buijs.klutter.tasks.input.RootFolder
import dev.buijs.klutter.tasks.input.validPluginNameOrThrow
import dev.buijs.klutter.tasks.input.validRootFolderOrThrow
import java.io.File

internal fun ProjectBuilderOptions.toInitKlutterAction() =
    InitKlutter(rootFolder, pluginName, config)

internal class InitKlutter(
    rootFolder: RootFolder,
    pluginName: PluginName,
    configOrNull: Config? = null
): ProjectBuilderAction {

    private val name = pluginName.validPluginNameOrThrow()
    private val root = rootFolder.validRootFolderOrThrow().resolve(name)
    private val conf = configOrNull
    private val confContainsBom = conf?.bomVersion != null

    private val rootPubspecFile =
        root.resolve("pubspec.yaml")

    private val rootPubspec =
        rootPubspecFile.toPubspec()

    private val exampleFolder =
        root.resolve("example")

    private val examplePubspecFile =
        exampleFolder.resolve("pubspec.yaml")

    override fun doAction() {
        if(confContainsBom)
            root.writeKlutterYaml(conf!!.bomVersion!!)

        rootPubspecInit(
            pubspec = rootPubspec,
            pubspecFile = rootPubspecFile,
            config = conf)

        examplePubspecInit(
            rootPubspec = rootPubspec,
            examplePubspecFile = examplePubspecFile,
            config = conf)

        root.deleteTestFolder()
        root.clearLibFolder()
        root.overwriteReadmeFile()
        root.copyLocalProperties()
        "flutter pub get" execute root
        "flutter pub get" execute exampleFolder
        "flutter pub run klutter:producer init" execute root
        "flutter pub run klutter:consumer init" execute exampleFolder
    }

    /**
     * Delete test folder and all it's content.
     *
     * You should test, but we're going to do that with Spock/JUnit in the platform module,
     * not with Dart in the root/test folder.
     */
    private fun File.deleteTestFolder() {
        resolve("test").deleteRecursively()
    }

    /**
     * Delete all Files in the root/lib folder.
     */
    private fun File.clearLibFolder() {
        resolve("lib").listFiles()?.forEach { it.deleteRecursively() }
    }

    /**
     * Change the README content to explain Klutter plugin development.
     */
    private fun File.overwriteReadmeFile() {
        resolve("README.md").let { readme ->
            // Seems redundant to delete and then recreate the README.md file.
            // However, the project is created by invoking the Flutter version
            // installed by the end-user. Future versions of Flutter might not
            // Create a README.md file but a readme.md, PLEASE_README.md or not
            // a readme file at all!
            //
            // In that case this code won't try to delete a file that does not
            // exist and only create a new file. We do NOT want the task to fail
            // because of a README.md file. :-)
            if(readme.exists()) readme.delete()
            readme.createNewFile()
            readme.writeText("""
                |# $name
                |A new Klutter plugin project. 
                |Klutter is a framework which interconnects Flutter and Kotlin Multiplatform.
                |
                |## Getting Started
                |This project is a starting point for a Klutter
                |[plug-in package](https://github.com/buijs-dev/klutter),
                |a specialized package that includes platform-specific implementation code for
                |Android and/or iOS. 
                |
                |This platform-specific code is written in Kotlin programming language by using
                |Kotlin Multiplatform. 
            """.trimMargin())
        }
    }

    private fun File.copyLocalProperties() {
        val properties = resolve("local.properties")
        if(!properties.exists())
            resolve("android/local.properties").copyTo(properties)
    }

    private fun File.writeKlutterYaml(bomVersion: String) {
        val rootConfigFile = resolve("klutter.yaml")
        rootConfigFile.createNewFile()
        rootConfigFile.writeText("bom-version: '$bomVersion'")
    }
}


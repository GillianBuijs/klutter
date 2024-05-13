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
package dev.buijs.klutter.kore.templates.flutter

import dev.buijs.klutter.kore.KlutterPrinter
import dev.buijs.klutter.kore.project.*
import dev.buijs.klutter.kore.templates.appendTemplate

/**
 * Pattern used to get a pubspec dependency from Git.
 *
 * This pattern should be used in the kradle.yaml file.
 *
 * Example notation: 'https://github.com/your-repo.git@develop'.
 */
private val gitDependencyRegex = """^(https:..github.com.+?git)@(.+${'$'})""".toRegex()

/**
 * Pattern used to get a pubspec dependency from local path.
 *
 * This pattern should be used in the kradle.yaml file.
 *
 * Example notation: 'local@foo/bar/dependency-name'.
 */
private val pathDependencyRegex = """(^local)@(.+/)(.+${'$'})""".toRegex()

/**
 * Serialize a pubspec.yaml for a new plugin project.
 */
fun createRootPubspecYamlWriter(
    pubspec: Pubspec,
    config: Config?
): KlutterPrinter {

    return object: KlutterPrinter {
        override fun print(): String = buildString {
            appendLine("name: ${pubspec.name}")
            appendLine("description: A new klutter plugin project.")
            appendLine("version: 0.0.1")
            appendLine("")
            appendLine("environment:")
            appendLine("""  sdk: '>=2.17.6 <4.0.0'""")
            appendLine("""  flutter: ">=2.5.0"""")
            appendLine("")
            appendLine("dependencies:")
            appendLine("    flutter:")
            appendLine("        sdk: flutter")
            appendLine("")
            appendDependency(config?.dependencies?.squint ?: squintPubVersion, name = "squint_json")
            appendLine("")
            appendDependency(config?.dependencies?.klutterUI ?: klutterUIPubVersion, name = "klutter_ui")
            appendLine("")
            appendLine("")
            appendLine("    protobuf: ^3.1.0")
            appendLine("dev_dependencies:")
            appendDependency(config?.dependencies?.klutter ?: klutterPubVersion, name = "klutter")
            appendLine("")
            appendLine("flutter:")
            appendLine("  plugin:")
            appendLine("    platforms:")
            appendLine("      android:")
            appendLine("        package: ${pubspec.android?.pluginPackage}")
            appendLine("        pluginClass: ${pubspec.android?.pluginClass}")
            appendLine("      ios:")
            appendLine("        pluginClass: ${pubspec.ios?.pluginClass}")
        }
    }

}

/**
 * Serialize a pubspec.yaml for a new plugin project.
 */
fun createExamplePubspecYamlWriter(
    pubspec: Pubspec,
    config: Config?
): KlutterPrinter {

    return object: KlutterPrinter {
        override fun print(): String = buildString {
            appendLine("name: ${pubspec.name}_example")
            appendLine("description: Demonstrates how to use the ${pubspec.name} plugin")
            appendLine("publish_to: 'none' # Remove this line if you wish to publish to pub.dev")
            appendLine("")
            appendLine("environment:")
            appendLine("""  sdk: '>=2.17.6 <4.0.0'""")
            appendLine("")
            appendLine("dependencies:")
            appendLine("    flutter:")
            appendLine("        sdk: flutter")
            appendLine("")
            appendLine("    ${pubspec.name}:")
            appendLine("        path: ../")
            appendLine("")
            appendDependency(config?.dependencies?.klutterUI ?: klutterUIPubVersion, name = "klutter_ui")
            appendLine("")
            appendDependency(config?.dependencies?.squint ?: squintPubVersion, name = "squint_json")
            appendLine("")
            appendLine("dev_dependencies:")
            appendLine("    flutter_test:")
            appendLine("        sdk: flutter")
            appendLine("")
            appendDependency(config?.dependencies?.klutter ?: klutterPubVersion, name = "klutter")
            appendLine("")
            appendLine("flutter:")
            appendLine("    uses-material-design: true")
        }
    }

}

private fun StringBuilder.appendDependency(dependency: String, name: String) {
    when {
        dependency.matches(gitDependencyRegex) ->
            appendGitDependency(dependency, name = name)
        dependency.matches(pathDependencyRegex) ->
            appendPathDependency(dependency, name = name)
        else -> append("    $name: ^$dependency")
    }
}

private fun StringBuilder.appendGitDependency(dependency: String, name: String) {
    val splitted = dependency.split("@")
    appendTemplate("""
    $name:
      git:
        url: ${splitted[0]}
        ref: ${splitted[1]}
    """)
}

private fun StringBuilder.appendPathDependency(dependency: String, name: String) {
    val splitted = dependency.split("@")
    appendTemplate("""
    $name:
      path: ${splitted[1]}
    """)
}
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
package dev.buijs.klutter.kore.project

import dev.buijs.klutter.kore.KlutterException
import dev.buijs.klutter.kore.common.verifyExists
import java.io.File

/**
 * Pattern to be used to add properties to local/public configuration files (kradle.yaml, kradle.env).
 *
 * Example: "{{project.build}}" is resolved to the build folder of a Klutter project e.g. root/platform/build.
 */
val projectPropertyPattern = """(\{\{project[.]([^}]+?)\}\})""".toRegex()

/**
 * Pattern to be used to add system properties to local/public configuration files (kradle.yaml, kradle.env).
 *
 * Example: "{{system.user.home}}" is resolved to the "user.home" property.
 */
val systemPropertyPattern = """(\{\{system[.]([^}]+?)\}\})""".toRegex()

/**
 * Try to replace all project properties [projectPropertyPattern] and return File content.
 *
 * @return File with new content.
 * @throws [KlutterException] if File does not exist or propertyName is unknown.
 */
fun File.resolveProjectPropertiesOrThrow(): File {
    var content = verifyExists().readText()
    val projectProperties = projectPropertyPattern.findAll(content)
    for(result in projectProperties) {
        val propertyName = result.groupValues[2]
        val property = when(propertyName) {
            "build" ->  parentFile.resolve("platform").resolve("build").absolutePath
            "root" -> parentFile.absolutePath
            else -> throw KlutterException("Unknown project property: $propertyName")
        }
        content = content.replace("{{project.$propertyName}}", property)
    }
    return this.also { it.writeText(content) }
}

/**
 * Try to replace all system properties [systemPropertyPattern] and return File content.
 *
 * @return File with new content.
 * @throws [KlutterException] if File does not exist or propertyName is unknown.
 */
fun File.resolveSystemPropertiesOrThrow(): File {
    var content = verifyExists().readText()
    val systemProperties = systemPropertyPattern.findAll(content)
    for(result in systemProperties) {
        val propertyName = result.groupValues[2]
        val property = System.getProperty(propertyName)
        content = content.replace("{{system.$propertyName}}", property)
    }
    return this.also { it.writeText(content) }
}

fun findFlutterVersionInKradleYamlOrNull(content: String?) =
    content.findPropertyInYamlOrNull("flutter-version")

fun findOutputPathInKradleEnvOrNull(content: String?) =
    content.findPropertyOrNull("output.path")

fun findSkipCodeGenInKradleEnvOrNull(content: String?) =
    content.findPropertyOrNull("skip.codegen")?.uppercase()?.let { skip ->
        when(skip) {
            "TRUE" -> true
            "FALSE" -> false
            else -> null
        }
    }

fun findProtocDownloadURLInKradleEnvOrNull(content: String?) =
    content.findPropertyOrNull("protoc.url")

private fun String?.findPropertyInYamlOrNull(key: String) =
    this?.let { str ->
        """$key:\s*('|")\s*([^'"]+?)\s*('|")""".toRegex().find(str)?.let { match ->
            match.groupValues[2]
        }
    }

private fun String?.findBooleanPropertyInYamlOrNull(key: String) =
    this?.let { str ->
        """$key:\s*([Tt][Rr][Uu][Ee]|[Ff][Aa][Ll][Ss][Ee])\s*""".toRegex().find(str)?.let { match ->
            match.groupValues[1]
        }
    }

private fun String?.findPropertyOrNull(key: String) =
    this?.let { str ->
        """$key=([^\n\r]+)""".toRegex().find(str)?.let { match ->
            match.groupValues[1]
        }
    }
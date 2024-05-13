@file:Suppress("unused")
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
package dev.buijs.klutter

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.net.URI
import java.util.*

class KlutterInternalPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        // no-op
    }
}

object ProjectVersions {
    val kore = versions("kore.version")
    val bom = versions("bom.version")
    val annotations = versions("annotations.version")
    val gradle = versions("plugin.gradle.version")
    val jetbrains = versions("plugin.jetbrains.version")
    val kompose = versions("kompose.version")
    val compiler = versions("compiler.version")
    val flutterEngine = versions("flutter.engine.version")
}

object Repository {
    val endpoint: URI = URI(System.getenv("KLUTTER_PRIVATE_URL")
        ?: repository("repo.endpoint"))
    val username = System.getenv("KLUTTER_PRIVATE_USERNAME")
        ?: repository("repo.username")
    val password = System.getenv("KLUTTER_PRIVATE_PASSWORD")
        ?: repository("repo.password")
}

object Signing {
    val certificateChain = System.getenv("KLUTTER_JETBRAINS_CERTIFICATE_CHAINS")
        ?: signing("certificate_chain")
    val privateKey = System.getenv("KLUTTER_JETBRAINS_PRIVATE_KEY")
        ?: signing("private_key")
    val privateKeyPassword = System.getenv("KLUTTER_JETBRAINS_PRIVATE_KEY_PASSWORD")
        ?: signing("private_key_password")
}
fun versions(key: String): String = Properties().also {
    it.load(KlutterInternalPlugin::class.java.classLoader
        .getResourceAsStream("publish.properties"))
}.getProperty(key) ?: throw GradleException("missing $key")

fun repository(key: String): String = Properties().also {
    it.load(KlutterInternalPlugin::class.java.classLoader
        .getResourceAsStream("repository.properties"))
}.getProperty(key) ?: throw GradleException("missing $key")

fun signing(key: String): String = Properties().also {
    it.load(KlutterInternalPlugin::class.java.classLoader
        .getResourceAsStream("signing.properties"))
}.getProperty(key) ?: throw GradleException("missing $key")
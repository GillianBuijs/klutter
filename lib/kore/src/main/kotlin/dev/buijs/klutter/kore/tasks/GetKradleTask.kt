/* Copyright (c) 2021 - 2024 Buijs Software
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
package dev.buijs.klutter.kore.tasks

import dev.buijs.klutter.kore.KlutterException
import dev.buijs.klutter.kore.KlutterTask
import dev.buijs.klutter.kore.common.verifyExists
import dev.buijs.klutter.kore.project.*
import dev.buijs.klutter.kore.tasks.project.DownloadFlutterTask
import java.io.File
import java.nio.file.Files

class GetKradleTask(
    /**
     * Absolute path to the kradle executable file.
     *
     * Example: /foo/bar/my_project/kradle
     */
    private val pathToKradleOutput: String,
): KlutterTask {
    override fun run() {
        val repository = cloneKlutterDartRepositoryToTempOrThrow()
        val dart = findDartExecutableOrThrow()
        val kradle = repository.resolve("bin/kradle.dart").absolutePath
        println("$dart pub get" execute repository)
        println("$dart compile exe $kradle -o $pathToKradleOutput" execute repository)
    }
}

private fun cloneKlutterDartRepositoryToTempOrThrow(): File {
    val temp = createTempDirectory()
    cloneKlutterDartRepoTo(temp)
    return temp.resolve("klutter-dart").verifyExists()
}

private fun createTempDirectory(): File =
    Files.createTempDirectory("klutter").toFile().also { it.deleteOnExit() }

private fun cloneKlutterDartRepoTo(directory: File) {
    println("git clone https://github.com/buijs-dev/klutter-dart.git" execute directory)
}

/**
 * Find a dart executable to generate the kradle executable.
 *
 * @throws [KlutterException] if a dart executable is not found.
 */
private fun findDartExecutableOrThrow(): String {
    val cachedDartExecutableName = findDartExecutableInCacheOrNull()
    if (cachedDartExecutableName != null) {
        return dartExecutable(cachedDartExecutableName).absolutePath
    }

    val globalDart = findGlobalDartExecutableOrNull()
    if (globalDart != null) {
        return globalDart
    }

    return dartExecutable(downloadFlutterDistributionOrThrow()).absolutePath

}

private fun findDartExecutableInCacheOrNull(): String? {
    val versions = compatibleFlutterVersions.keys.map { it.folderNameString.toString() }
    val cache = kradleHome.resolve("cache").verifyExists()
    val cachedFlutterDistributions = cache.listFiles()?.map { it.name } ?:emptyList()
    return versions.firstOrNull { version ->
        cachedFlutterDistributions.any { it.contains(version) }
    }
}

private fun findGlobalDartExecutableOrNull(): String? {
    val dartOutput = "dart" execute currentWorkingDirectory
    val hasGlobalDart = dartOutput.contains("A command-line utility for Dart development.")
    return if(hasGlobalDart) "dart" else null
}

private fun downloadFlutterDistributionOrThrow(): String {
    val architecture = currentArchitecture
    val version = flutterVersionsDescending(currentOperatingSystem)
        .firstOrNull { it.arch == architecture }
        ?: throw KlutterException("Could not find compatible flutter distribution")
    DownloadFlutterTask(version).run()
    return version.folderNameString.toString()
}
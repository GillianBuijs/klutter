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
package dev.buijs.klutter.kore.tasks.project

import dev.buijs.klutter.kore.KlutterException
import dev.buijs.klutter.kore.common.verifyExists
import dev.buijs.klutter.kore.project.*
import dev.buijs.klutter.kore.tasks.Zippie
import java.io.*
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.Files

internal class DownloadProtoc(
    private val rootFolder: File,
    private val overwrite: Boolean = false,
    private val target: File,
) : ProjectBuilderAction {
    override fun doAction() {
        if(dryRun) return
        val path = findProtocDownloadURLInKradleEnvOrNull(rootFolder.resolve("kradle.env").verifyExists().readText())
        if(target.exists() && !overwrite) return
        val zip = tempZipFile

        try {
            Channels.newChannel(URL(path).openStream()).use { readableByteChannel ->
                val fileOutputStream = FileOutputStream(zip)
                val fileChannel = fileOutputStream.getChannel()
                fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
            }
        } catch (e: IOException) {
            throw KlutterException("Failed to download protoc", e)
        }

        // Make sure cache exists in the user home.
        initKradleCache()
        if(target.exists()) target.deleteRecursively()
        Zippie(zip).unzipTo(target.absolutePath)
        zip.deleteRecursively()
    }

}

private val tempZipFile: File
    get() = Files.createTempDirectory("klutter_download").toFile().resolve("protoc.zip")

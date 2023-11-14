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
package dev.buijs.klutter.kore.tasks

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipFile

class Zippie(
    /**
     * File to unzip.
     */
    private val zipFile: File,

    /**
     * Files which should be executable after unzipping.
     */
    private val executableFiles: Set<String>,

    /**
     * Size of the buffer to read/write data
     */
    private val bufferSize: Int = 4096
) {

    fun unzipTo(destDirectory: String) {
        File(destDirectory).run { if (!exists()) mkdirs() }
        ZipFile(zipFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    "$destDirectory${File.separator}${entry.name}".let { filePath ->
                        if (entry.isDirectory) {
                            File(filePath).mkdir()
                        } else {
                            input.extractTo(filePath)
                        }
                    }
                }
            }
        }
    }

    /**
     * Extracts a zip entry (file entry)
     */
    private fun InputStream.extractTo(destFilePath: String) {
        BufferedOutputStream(FileOutputStream(destFilePath)).use { bos ->
            val bytesIn = ByteArray(bufferSize)
            var bytesLength = read(bytesIn)
            while(bytesLength != -1) {
                bos.write(bytesIn, 0, bytesLength)
                bytesLength = read(bytesIn)
            }
        }

        File(destFilePath).setExecutableIfApplicable()
    }

    private fun File.setExecutableIfApplicable() {
        if(executableFiles.contains(nameWithoutExtension)) {
            setExecutable(true)
        }
    }
}

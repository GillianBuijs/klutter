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
package dev.buijs.klutter.compiler.scanner

import dev.buijs.klutter.kore.ast.*
import dev.buijs.klutter.kore.common.*
import kotlinx.serialization.encodeToString
import java.io.File

/**
 * Prefix for any File that is processed by the (dart) squint_json library.
 * </br>
 * If a File has this prefix then squint_json will analyse its content as Metadata and not as JSON.
 * </br>
 * For more information see: [squint_json](https://pub.dev/packages/squint_json).
 */
private const val squintJsonMetadataPrefix = "sqdb_"

/**
 * Write all AbstractTypes or error message to [outputFolder].
 */
internal fun Either<String,SquintMessageSource>.writeOutput(
    outputFolder: File, count: Int
): Either<String,SquintMessageSource> {

    val folder = outputFolder
        .resolve("response")
        .maybeCreateFolder()

    when (this) {
        is EitherNok -> {
            val file = folder.resolve("$squintJsonMetadataPrefix${count}_invalid.json")
            file.createNewFile()
            file.writeText(data)
            return this
        }

        is EitherOk -> {
            val file = folder.resolve("$squintJsonMetadataPrefix${data.type.className.lowercase()}.json")
            file.createNewFile()

            when (data.squintType) {
                is SquintCustomType ->
                    file.writeText(JSON.encodeToString(data.squintType))
                is SquintEnumType ->
                    file.writeText(JSON.encodeToString(data.squintType))
            }
            return ValidSquintType(data = data.copy(source = file))
        }
    }

}

/**
 * Write FQDN of Response class or error message to [outputFolder].
 */
internal fun Either<String,String>.writeResponseFQDN(
    outputFolder: File, count: Int
): Either<String,String> {

    val folder = outputFolder
        .resolve("response")
        .maybeCreateFolder()

    return when (this) {
        is EitherNok -> {
            val file = folder.resolve("${count}_invalid.txt")
            file.createNewFile()
            file.writeText(data)
            Either.nok(data)
        }

        is EitherOk -> {
            val file = folder.resolve("proto_${count}.txt")
            file.createNewFile()
            file.writeText(data)
            Either.ok(data)
        }
    }

}
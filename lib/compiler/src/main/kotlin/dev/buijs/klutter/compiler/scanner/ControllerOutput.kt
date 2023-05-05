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
package dev.buijs.klutter.compiler.scanner

import dev.buijs.klutter.kore.ast.Controller
import dev.buijs.klutter.kore.common.EitherNok
import dev.buijs.klutter.kore.common.EitherOk
import dev.buijs.klutter.kore.common.maybeCreate
import dev.buijs.klutter.kore.common.maybeCreateFolder
import java.io.File

/**
 * Write all AbstractTypes or error message to [outputFolder].
 * </br>
 * A folder named <b>controller</b> is created in [outputFolder].
 * A File is written For each [Controller] or error message.
 * Controller metadata is written using [Controller.toDebugString].
 */
internal fun List<ValidControllerOrError>.writeOutput(outputFolder: File) {
    val folder = outputFolder.resolve("controller").maybeCreateFolder(clearIfExists = true)
    for((count, controllerOrError) in this.withIndex()) {
        when(controllerOrError){
            is EitherOk -> {
                val name = "${count}_${controllerOrError.data.className.lowercase()}.txt"
                val text = controllerOrError.data.toDebugString()
                folder.resolve(name).maybeCreate().writeText(text)
            }
            is EitherNok -> {
                val name = "${count}_invalid.txt"
                val text = controllerOrError.data
                folder.resolve(name).maybeCreate().writeText(text)
            }
        }
    }
}
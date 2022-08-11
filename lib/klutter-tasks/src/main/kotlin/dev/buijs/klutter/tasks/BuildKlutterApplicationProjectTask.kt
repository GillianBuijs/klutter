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
package dev.buijs.klutter.tasks

import dev.buijs.klutter.kore.KlutterTask
import dev.buijs.klutter.kore.project.Project
import dev.buijs.klutter.kore.shared.execute
import java.io.File

/**
 * Task to build a Klutter application project.
 *
 * Executes the following steps:
 * - clean
 * - build
 * - klutterGenerateAdapters
 * - klutterCopyAarFile
 * - klutterCopyFramework
 * - klutterGenerateUI
 *
 */
open class BuildKlutterApplicationProjectTask(
    private val project: Project,
    private val pathToBuild: File,
    private val pathToOutput: File,
) : KlutterTask {

    override fun run() {
        """./gradlew clean build""".execute(project.root.folder)
        AdapterGeneratorTask.from(project).run()
        """./gradlew klutterCopyAarFile""".execute(project.root.folder)
        """./gradlew klutterCopyFramework""".execute(project.root.folder)
        UiGeneratorTask(pathToBuild, pathToOutput).run()
    }

}


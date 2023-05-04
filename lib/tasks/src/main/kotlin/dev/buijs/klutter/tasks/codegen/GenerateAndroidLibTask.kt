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
package dev.buijs.klutter.tasks.codegen

import dev.buijs.klutter.kore.KlutterTask
import dev.buijs.klutter.kore.ast.BroadcastController
import dev.buijs.klutter.kore.ast.Controller
import dev.buijs.klutter.kore.ast.SimpleController
import dev.buijs.klutter.kore.common.*
import dev.buijs.klutter.kore.project.*
import dev.buijs.klutter.kore.templates.AndroidAdapter

fun GenerateCodeOptions.toGenerateAndroidLibTask() =
    GenerateAndroidLibTask(android = project.android, bindings = bindings)

/**
 * Generate the Android code in root/android.
 */
class GenerateAndroidLibTask(
    private val android: Android,
    private val bindings: Map<String, Controller>,
) : KlutterTask, GenerateCodeAction {
    override fun run() {
        android.pathToPlugin.maybeCreate().write(
            AndroidAdapter(
                pluginClassName = android.pluginClassName,
                pluginPackageName = android.pluginPackageName,
                methodChannels = bindings.filterValues { it is SimpleController }.keys,
                eventChannels = bindings.filterValues { it is BroadcastController }.keys,
                controllers = bindings.values.toSet()))
    }
}
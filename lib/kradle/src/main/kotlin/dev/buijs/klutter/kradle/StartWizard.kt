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
package dev.buijs.klutter.kradle

import dev.buijs.klutter.kore.tasks.CleanCacheTask

internal enum class WizardAction(val prettyPrinted: String, val action: () -> Unit) {
    NEW_PROJECT(
        prettyPrinted = "New Project",
        action = { getNewProjectOptionsByUserInput().createNewProject() }),
    GET_FLUTTER_SDK(
        prettyPrinted = "Download Flutter SDK",
        action = { getFlutterWizard() }),
    CLEAR_CACHE(
        prettyPrinted = "Clear Klutter Cache",
        action = { CleanCacheTask().run() }),
    EXIT(
        prettyPrinted = "Exit",
        action = {
            println("Farewell!")
        })
}

internal fun startWizard() {
    val chosen = mrWizard.promptList(
        hint = "press Enter to pick",
        message = "What do you want to do?",
        choices = WizardAction.values().map { it.prettyPrinted })

    WizardAction.values().first { it.prettyPrinted == chosen }.action()
}
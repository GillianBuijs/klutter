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
package dev.buijs.klutter.jetbrains

import com.intellij.openapi.util.IconLoader
import dev.buijs.klutter.kore.common.ExcludeFromJacocoGeneratedReport
import javax.swing.Icon

/**
 * Common text messages.
 */
@ExcludeFromJacocoGeneratedReport
object KlutterBundle {

    const val moduleId: String = "KLUTTER_MODULE"

    const val bundleId: String = "buijs_software_klutter"

    const val presentableName: String = "Klutter"

    const val groupName: String = "Klutter Framework"

    const val descriptionShort: String = "Add support for the Klutter Framework"

    const val descriptionLong: String = "" +
            "Klutter is a framework which interconnects Flutter and Kotlin Multiplatform. " +
            "It can be used to create Flutter plugins or standalone apps."

}

/**
 * Klutter Logo's in Icon format.
 */
@ExcludeFromJacocoGeneratedReport
object KlutterIcons {
    val logo16x16: Icon = IconLoader.getIcon("/pluginIcon16x16.svg", KlutterIcons::class.java)
    val logo20x20: Icon = IconLoader.getIcon("/pluginIcon20x20.svg", KlutterIcons::class.java)
}
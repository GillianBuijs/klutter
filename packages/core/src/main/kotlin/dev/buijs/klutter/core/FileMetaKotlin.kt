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

package dev.buijs.klutter.core

import dev.buijs.klutter.core.annotations.ReturnTypeLanguage
import org.jetbrains.kotlin.util.removeSuffixIfPresent
import java.io.File

/**
 * @author Gillian Buijs
 */
internal data class MethodData(
    val getter: String,
    val import: String,
    val call: String,
    val async: Boolean = false,
    val returns: String,
)

internal fun File.toMethodData(language: ReturnTypeLanguage = ReturnTypeLanguage.KOTLIN): List<MethodData> {

    val content = readText()
    val className = name.removeSuffixIfPresent(".kt")
    val packagename = """package(.*)""".toRegex()
        .find(content)
        ?.groupValues
        ?.get(1)
        ?.filter { !it.isWhitespace() }
        ?:""

    val trimmedBody = content.filter { !it.isWhitespace() }

    return """@KlutterAdaptee\(("|[^"]+?")([^"]+?)".+?(suspend|)fun([^(]+?\([^:]+?):([^{]+?)\{""".toRegex()
        .findAll(trimmedBody).map { match ->
            val getter = match.groups[2]?.value?:throw KlutterException("""
                   Unable to process KlutterAdaptee annotation.
                   Please make sure the annotation is as follows: 'klutterAdaptee(name = "foo")'
                   """.trim())

            val caller = match.groups[4]?.value?:throw KlutterException("""
                   Unable to process KlutterAdaptee annotation.
                   Please make sure the annotation is as follows: 'klutterAdaptee(name = "foo")'
                   """.trim())

            val returns = match.groups[5]?.value?.trim()?:throw KlutterException("""
                    Unable to determine return type of function annotated with @KlutterAdaptee.
                    The method signature should be as follows: fun foo(): Bar { //your implementation }
                   """.trim())

            val returnType = DartKotlinMap.toMapOrNull(returns)?.let {
                                if(language == ReturnTypeLanguage.DART) it.dartType else it.kotlinType
                            } ?: returns

            MethodData(
                import = "$packagename.$className",
                getter = getter,
                call = "$className().$caller",
                async = match.groups[3]?.value?.trim()?.isNotBlank()?:false,
                returns = returnType)

        }.toList()
}
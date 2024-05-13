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
package dev.buijs.klutter.kore.test

class TestUtil {

    def static warningColor = "\u001B[1;31m"

    static Boolean verify(String actual, String expected) {

        if(actual == expected) {
            println("============== Test OK: IDENTICAL CONTENT ==============")
            return true
        }

        def actualNoSpaces = removeSpaces(actual)
        def expectedNoSpaces = removeSpaces(expected)

        if(actualNoSpaces == expectedNoSpaces) {
            println("============== Test OK: SPACES VARY ==============")
            return true
        }

        def actualNoLinebreaks = removeLinebreaks(actualNoSpaces)
        def expectedNoLinebreaks = removeLinebreaks(expectedNoSpaces)

        if(actualNoLinebreaks == expectedNoLinebreaks) {
            println("============== Test OK: SPACES AND LINE BREAKS VARY ==============")
            return true
        }

        println("============== Test NOK! ==============")
        println("")
        def actualLines = actual.readLines()
        def expectedLines = expected.readLines()

        for(int i = 0; i < actualLines.size(); i++){
            if(expectedLines.size() > i) {
                def expectedLine = expectedLines[i].trim()
                def actualLine = actualLines[i].trim()
                def colour = expectedLine != actualLine ? warningColor : ""
                println("$colour============== EXPECTED: '${expectedLine}'")
                println("$colour============== ACTUAL:   '${actualLine}'")
            } else {
                println("$warningColor============== ACTUAL:   '${actualLines[i].trim()}'")
            }
        }

        println("Actual String:")
        println("")
        println(actual)
        return false
    }

    def static removeSpaces(String input) {
        return input.replaceAll(" ", "")
    }

    def static removeLinebreaks(String input) {
        return input.replaceAll("\n", "")
    }
}
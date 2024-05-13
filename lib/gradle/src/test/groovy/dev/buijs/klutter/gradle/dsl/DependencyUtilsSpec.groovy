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
package dev.buijs.klutter.gradle.dsl

import spock.lang.Specification

import java.nio.file.Files

class DependencyUtilsSpec extends Specification {

    def "Verify embedded dependencies are retrieved successfully"() {

        given:
        def configFile = Files.createTempFile("", "").toFile()

        and:
        configFile.write(content)

        when:
        def embedded = DependencyUtilsKt.embeddedDependenciesFromConfigFile(configFile)

        then:
        embedded.size() == 1
        embedded[0] == "org.jetbrains.kotlinx:kotlinx-datetime:0.4.0"

        where:
        content = '''bom-version: "2023.1.2-SNAPSHOT"
                     dependencies:
                       klutter: "0.3.0"
                       klutter_ui: "0.0.1"
                       squint_json: "0.1.2"
                       embedded:
                         - "org.jetbrains.kotlinx:kotlinx-datetime:0.4.0"'''

    }


    def "Verify embedded dependencyNotation is bumped and not duplicated"() {

        given:
        def embedded = Set.of("foo:bar:001")

        when:
        def bumped = DependencyUtilsKt.bumpDependencyVersion(embedded,"foo:bar:002")

        then:
        bumped.size() == 1
        bumped[0] == "foo:bar:002"
    }


}
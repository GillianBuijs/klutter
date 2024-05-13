/* Copyright (c) 2021 - 2024 Buijs Software
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
package dev.buijs.klutter.gradle.tasks


import dev.buijs.klutter.kore.KlutterException
import dev.buijs.klutter.kore.tasks.DownloadProtocTask
import dev.buijs.klutter.kore.tasks.GetDartProtocExeTask
import dev.buijs.klutter.kore.tasks.codegen.CompileProtoSchemaTask
import spock.lang.Specification

import static dev.buijs.klutter.gradle.tasks.TaskTestUtil.getTask
import static dev.buijs.klutter.gradle.tasks.TaskTestUtil.verifyTask

class ProtoTasksSpec extends Specification {

    def "Verify GetProtocGradleTask returns DownloadProtocTask"() {
        expect:
        verifyTask(GetProtocGradleTask, DownloadProtocTask)
    }

    def "Verify GetProtocDartGradleTask returns GetDartProtocExeTask"() {
        expect:
        verifyTask(GetProtocDartGradleTask, GetDartProtocExeTask)
    }

    def "Verify CompileProtoSchemasGradleTask returns GetDartProtocExeTask"() {
        expect:
        verifyTask(CompileProtoSchemasGradleTask, CompileProtoSchemaTask)
    }

    def "Verify GenerateProtoSchemasGradleTask throws KlutterException if classLoader variable is not set"() {
        given:
        def task = getTask(GenerateProtoSchemasGradleTask)

        when:
        task.execute()

        then:
        KlutterException e = thrown()
        e.message == "GenerateProtoSchemaGradleTask is missing property value 'classLoader'"

        when:
        task.setClassLoader(this.class.classLoader)

        and:
        task.execute()

        then: "No exception is thrown"
        1 == 1
    }

}

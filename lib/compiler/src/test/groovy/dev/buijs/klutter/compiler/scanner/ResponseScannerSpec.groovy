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

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import dev.buijs.klutter.compiler.wrapper.KCEnumeration
import dev.buijs.klutter.compiler.wrapper.KCMessage
import dev.buijs.klutter.compiler.wrapper.KCResponse
import dev.buijs.klutter.kore.ast.StringType
import dev.buijs.klutter.kore.ast.TypeMember
import dev.buijs.klutter.kore.common.Either
import dev.buijs.klutter.kore.common.EitherNok
import dev.buijs.klutter.kore.common.EitherOk
import kotlin.sequences.Sequence
import kotlin.sequences.SequencesKt
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files

class ResponseScannerSpec extends Specification {

    @Shared
    File outputFolder = Files.createTempDirectory("").toFile()

    @Shared
    Resolver resolver = Stub(Resolver)

    @Shared
    String isSerializableAnnotated = "isSerializableAnnotated"

    @Shared
    String extendsKlutterJSON = "extendsKlutterJSON"

    @Shared
    String hasOneConstructor = "hasOneConstructor"

    @Shared
    String typeMembers = "typeMembers"

    @Shared
    String className = "className"

    @Shared
    String packageName = "packageName"

    @Shared
    String values = "values"

    @Shared
    String valuesJSON = "valuesJSON"

    @Shared
    TypeMember dummyTypeMember = new TypeMember("foo", new StringType())

    def "When a response has no Serializable annotation, then scanning returns an error" () {
        given:
        def classes = [buildKCMessage([isSerializableAnnotated: false])]

        when:
        def result = ResponseScannerKt
                .scanForResponses(outputFolder, resolver, false, {classes })

        then:
        with(result[0] as EitherNok) {
            it.data == "Class is missing @Serializable annotation: com.foo.example.MyCustomClass"
        }
    }

    def "When a response does NOT extend JSON, then scanning returns an error" () {
        given:
        def classes = [buildKCMessage([extendsKlutterJSON: false])]

        when:
        def result = ResponseScannerKt
                .scanForResponses(outputFolder, resolver, false, {classes })

        then:
        with(result[0] as EitherNok) {
            it.data == "Class does not extend JSON: com.foo.example.MyCustomClass"
        }
    }

    def "When a response has invalid TypeMembers, then scanning returns an error" () {
        given:
        def classes = [buildKCMessage([typeMembers: [Either.nok("Faulty Type")]])]

        when:
        def result = ResponseScannerKt
                .scanForResponses(outputFolder, resolver, false, {classes })

        then:
        with(result[0] as EitherNok) {
            it.data == "Response com.foo.example.MyCustomClass is invalid: Faulty Type"
        }
    }

    def "When a response has multiple constructors, then scanning returns an error" () {
        given:
        def classes = [buildKCMessage([hasOneConstructor: false])]

        when:
        def result = ResponseScannerKt
                .scanForResponses(outputFolder, resolver, false, {classes })

        then:
        with(result[0] as EitherNok) {
            it.data == "Response com.foo.example.MyCustomClass has multiple constructors but only 1 is allowed."
        }
    }

    def "When a response has multiple constructors, then scanning returns an error" () {
        given:
        def classes = [buildKCMessage(["":""])]

        when:
        def result = ResponseScannerKt
                .scanForResponses(outputFolder, resolver, false, {classes })

        then:
        with(result[0] as EitherNok) {
            it.data == "Response com.foo.example.MyCustomClass constructor has no fields but at least 1 is expected."
        }
    }

    def "Verify SquintMessageSource is returned for a valid Response" () {
        given:
        def classes = [buildKCMessage([typeMembers:[Either.ok(dummyTypeMember)]])]

        when:
        def result = ResponseScannerKt
                .scanForResponses(outputFolder, resolver, false, {classes })

        then:
        with(result[0] as EitherOk) {
            it.data.type.className == "MyCustomClass"
            it.data.type.packageName == "com.foo.example"
            //noinspection GroovyAssignabilityCheck
            it.data.type.members[0] == dummyTypeMember
            it.data.squintType.className == "MyCustomClass"
            //noinspection GroovyAssignabilityCheck
            with(it.data.squintType.members[0]) {
                it.name == "foo"
                it.type == "String"
                !it.nullable
            }

        }
    }

    def "When an enumeration has no Serializable annotation, then scanning returns an error" () {
        given:
        def classes = [buildKCEnumeration([isSerializableAnnotated: false])]

        when:
        def result = ResponseScannerKt
                .scanForResponses(outputFolder, resolver, false, {classes })

        then:
        with(result[0] as EitherNok) {
            it.data == "Class is missing @Serializable annotation: com.foo.example.MyCustomClass"
        }
    }


    def "Verify SquintMessageSource is returned for a valid Response" () {
        given:
        def classes = [buildKCEnumeration([
                values: ["foo", "bar"],
                valuesJSON: ["FOO", "BAR"]])]

        when:
        def result = ResponseScannerKt
                .scanForResponses(outputFolder, resolver, false, {classes })

        then:
        with(result[0] as EitherOk) {
            it.data.type.className == "MyCustomClass"
            it.data.type.packageName == "com.foo.example"
            it.data.type.values == ["foo", "bar"]
            it.data.type.valuesJSON == ["FOO", "BAR"]
            it.data.squintType.className == "MyCustomClass"
            it.data.squintType.values == ["foo", "bar"]
            it.data.squintType.valuesJSON == ["FOO", "BAR"]
        }
    }

    // TODO cannot mock a sealed interface
    @Ignore
    def "When no callback is given, then default symbol-processor callback is used" () {
        given:
        Resolver resolver = Mock()
        KSClassDeclaration annotated = Stub() {
            it.toKCResponse() >> Mock(KCResponse)
        }
        Sequence<KSAnnotated> sequence = SequencesKt.sequenceOf(annotated)
        resolver.getSymbolsWithAnnotation("dev.buijs.klutter.annotations.Response", false) >> sequence

        when:
        def result = ResponseScannerKt.scanForResponses(outputFolder, resolver)

        then:
        !result.isEmpty()
    }
    def buildKCMessage(Map<String, Object> config) {
        return new KCMessage(
                config.getOrDefault(isSerializableAnnotated, true) as boolean,
                config.getOrDefault(className, "MyCustomClass") as String,
                config.getOrDefault(packageName, "com.foo.example") as String,
                config.getOrDefault(extendsKlutterJSON, true) as boolean,
                config.getOrDefault(hasOneConstructor, true) as boolean,
                config.getOrDefault(typeMembers, []) as List<Either<String, TypeMember>>)
    }

    def buildKCEnumeration(Map<String, Object> config) {
        return new KCEnumeration(
                config.getOrDefault(isSerializableAnnotated, true) as boolean,
                config.getOrDefault(className, "MyCustomClass") as String,
                config.getOrDefault(packageName, "com.foo.example") as String,
                config.getOrDefault(values, []) as List<String>,
                config.getOrDefault(valuesJSON, []) as List<String>)
    }
}

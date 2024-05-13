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
import dev.buijs.klutter.compiler.wrapper.KCController
import dev.buijs.klutter.kore.ast.Method
import dev.buijs.klutter.kore.ast.StringType
import dev.buijs.klutter.kore.common.EitherNok
import dev.buijs.klutter.kore.common.EitherOk
import kotlin.sequences.Sequence
import kotlin.sequences.SequencesKt
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files

class ControllerScannerSpec extends Specification {

    @Shared
    File outputFolder = Files.createTempDirectory("").toFile()

    @Shared
    Resolver resolver = Stub(Resolver)

    @Shared
    String hasOneConstructor = "hasOneConstructor"

    @Shared
    String firstConstructorHasNoParameters = "firstConstructorHasNoParameters"

    @Shared
    String isBroadcastController = "isBroadcastController"

    @Shared
    String eventErrors = "eventErrors"

    @Shared
    String methods = "methods"

    @Shared
    String controllerType = "controllerType"

    @Shared
    String broadcastTypeParameterOrBlank = "broadcastTypeParameterOrBlank"

    @Shared
    String className = "className"

    @Shared
    String packageName = "packageName"

    @Shared
    Method dummyMethod = new Method("", "", "", false, new StringType(), null, null)

    def "When a controller has multiple constructors, scanning returns an error" () {
        given:
        def classes = [buildKotlinClassWrapper([hasOneConstructor: false])]

        when:
        def result = ControllerScannerKt
                .scanForControllers(outputFolder, resolver, Set.of(),{classes })

        then:
        with(result[0] as EitherNok) {
            it.data == "Controller com.foo.example.MyCustomClass has multiple constructors but only 1 is allowed."
        }
    }

    def "When a controller has a constructor with parameters, scanning returns an error" () {
        given:
        def classes = [buildKotlinClassWrapper([firstConstructorHasNoParameters: false])]

        when:
        def result = ControllerScannerKt
                .scanForControllers(outputFolder, resolver, Set.of(),{classes })

        then:
        with(result[0] as EitherNok) {
            it.data == "Controller com.foo.example.MyCustomClass is only allowed to have a no-arg constructor"
        }
    }

    def "When a controller has invalid events, scanning returns an error" () {
        given:
        def classes = [buildKotlinClassWrapper([eventErrors: ["Invalid Event!"]])]

        when:
        def result = ControllerScannerKt
                .scanForControllers(outputFolder, resolver, Set.of(),{classes })

        then:
        with(result[0] as EitherNok) {
            it.data == "Controller com.foo.example.MyCustomClass has invalid events: Invalid Event!"
        }
    }

    def "When a broadcast controller has an invalid TypeParameter, scanning returns an error" () {
        given:
        def classes = [buildKotlinClassWrapper([
                isBroadcastController: true,
                broadcastTypeParameterOrBlank: "!SMH"])]

        when:
        def result = ControllerScannerKt
                .scanForControllers(outputFolder, resolver, Set.of(),{classes })

        then:
        with(result[0] as EitherNok) {
            it.data == "BroadcastController has invalid TypeParameter: Type name is invalid (should match ^[A-Z][a-zA-Z0-9]+\$): !SMH (invalid class name)"
        }
    }

    def "Verify a valid SimpleController" () {
        given:
        def config = [
                isBroadcastController: false,
                controllerType: type,
                className: "SomeClass",
                packageName: "some.pack.age",
                methods: [dummyMethod]
        ]

        and:
        def classes = [buildKotlinClassWrapper(config)]

        when:
        def result = ControllerScannerKt
                .scanForControllers(outputFolder, resolver, Set.of(),{classes })

        then:
        with(result[0] as EitherOk) {
            it.data.class.simpleName == classType
            it.data.className == "SomeClass"
            it.data.packageName == "some.pack.age"
            it.data.functions.size() == 1
            //noinspection GroovyAssignabilityCheck
            it.data.functions[0] == dummyMethod
        }

        where:
        type            | classType
        "Singleton"     | "SingletonSimpleController"
        "Default"       | "RequestScopedSimpleController"
        "RequestScoped" | "RequestScopedSimpleController"
    }

    def "Verify a valid BroadcastController" () {
        given:
        def config = [
                isBroadcastController: true,
                broadcastTypeParameterOrBlank: "String",
                controllerType: type,
                className: "SomeClass",
                packageName: "some.pack.age",
                methods: [dummyMethod]
        ]

        and:
        def classes = [buildKotlinClassWrapper(config)]

        when:
        def result = ControllerScannerKt
                .scanForControllers(outputFolder, resolver, Set.of(),{classes })

        then:
        with(result[0] as EitherOk) {
            it.data.class.simpleName == classType
            it.data.className == "SomeClass"
            it.data.packageName == "some.pack.age"
            it.data.functions.size() == 1
            //noinspection GroovyAssignabilityCheck
            it.data.functions[0] == dummyMethod
        }

        where:
        type            | classType
        "Singleton"     | "SingletonBroadcastController"
        "Default"       | "SingletonBroadcastController"
        "RequestScoped" | "RequestScopedBroadcastController"
    }

    def "When no callback is given, then default symbol-processor callback is used" () {
        given:
        Resolver resolver = Mock()
        KSClassDeclaration annotated = Stub()
        Sequence<KSAnnotated> sequence = SequencesKt.sequenceOf(annotated)
        resolver.getSymbolsWithAnnotation("dev.buijs.klutter.annotations.Controller", false) >> sequence

        when:
        def result = ControllerScannerKt.scanForControllers(outputFolder, resolver, Set.of())

        then:
        !result.isEmpty()
    }

    def buildKotlinClassWrapper(Map<String, Object> config) {
        return new KCController(
                config.getOrDefault(hasOneConstructor, true) as boolean,
                config.getOrDefault(firstConstructorHasNoParameters, true) as boolean,
                config.getOrDefault(methods, []) as List<Method>,
                config.getOrDefault(eventErrors, []) as List<String>,
                config.getOrDefault(controllerType, "Broadcast") as String,
                config.getOrDefault(isBroadcastController, true) as boolean,
                config.getOrDefault(broadcastTypeParameterOrBlank, "") as String,
                config.getOrDefault(className, "MyCustomClass") as String,
                config.getOrDefault(packageName, "com.foo.example") as String)
    }
}

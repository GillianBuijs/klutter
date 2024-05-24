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
package dev.buijs.klutter.kore.tasks.project

import dev.buijs.klutter.kore.ast.*
import dev.buijs.klutter.kore.common.Either
import dev.buijs.klutter.kore.project.ProjectKt
import dev.buijs.klutter.kore.project.PubspecBuilder
import dev.buijs.klutter.kore.tasks.ExecutorKt
import dev.buijs.klutter.kore.tasks.codegen.GenerateCodeOptions
import dev.buijs.klutter.kore.tasks.codegen.GenerateCodeTaskKt
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class GenerateCodeTaskSpec extends Specification {

    @Shared
    def executor = new Exeggutor()

    @Shared
    def pluginName = "my_awesome_plugin"

    @Shared
    def groupName = "com.example.awesomeness"

    @Shared
    def root = Files.createTempDirectory("").toFile()

    @Shared
    def pathToRoot = root.absolutePath

    @Shared
    def plugin = new File("${pathToRoot}/$pluginName")

    @Shared
    def pathToPlugin = plugin.absolutePath

    @Shared
    def example = new File("${pathToPlugin}/example")

    @Shared
    def pathToExampleIos = "${example.absolutePath}/ios"

    @Shared
    def pathToExample = example.absolutePath

    @Shared
    def flutterDistribution = "3.0.5.macos.arm64"

    @Shared
    def flutterExe = ProjectKt.flutterExecutable(flutterDistribution).absolutePath

    @Shared
    def dartExe = ProjectKt.dartExecutable(flutterDistribution).absolutePath

    @Shared
    def sut = new ProjectBuilderTask(
            new ProjectBuilderOptions(
                    Either.ok(new File(pathToRoot)),
                    Either.ok(pluginName),
                    Either.ok(groupName),
                    flutterDistribution,
                    null))

    @Shared
    def createFlutterPlugin = flutterExe + " create my_awesome_plugin --org com.example.awesomeness --template=plugin --platforms=android,ios -a kotlin -i swift"

    @Shared
    def flutterPubGet = flutterExe + " pub get"

    @Shared
    def klutterProducerInit = flutterExe + " pub run klutter:kradle init bom=2024.1.3.beta flutter=3.0.5.macos.arm64"

    @Shared
    def klutterConsumerInit = flutterExe + " pub run klutter:kradle init"

    @Shared
    def klutterConsumerInitIOS = flutterExe + " pub run klutter:kradle init ios=13"

    @Shared
    def klutterConsumerAdd = flutterExe + " pub run klutter:kradle add lib=my_awesome_plugin"

    @Shared
    def iosPodUpdate = "pod update"

    @Shared
    def iosPodInstall = "pod install"

    @Shared
    String packageName = "foo.dot.com"

    @Shared
    TypeMember fooType = new TypeMember("foo", new StringType())

    @Shared
    CustomType myCustomType = new CustomType("MyCustomType", packageName, [fooType])

    @Shared
    Method methodReturningMyCustomType = new Method(
            "getFoo",
            "foo.dot.com",
            "foo",
            false,
            myCustomType,
            null,
            null)

    @Shared
    Method methodWithStringRequest = new Method(
            "setFoo",
            "foo.dot.com",
            "foo",
            false,
            new UnitType(),
            new StringType(),
            "data")
    @Shared
    List<Controller> controllers = [
            new RequestScopedBroadcastController(packageName, "MyRequestScopedBroadcaster", [], new StringType()),
            new RequestScopedSimpleController(packageName, "MyRequestScoped", [methodReturningMyCustomType]),
            new SingletonSimpleController(packageName, "MySingleton", [methodWithStringRequest]),
            new SingletonBroadcastController(packageName, "MySingletonBroadcaster", [], myCustomType)
    ]

    @Shared
    List<SquintMessageSource> messages
    def setupSpec() {
        ActionDownloadFlutterKt.dryRun = true
        plugin.mkdirs()
        example.mkdirs()
        ExecutorKt.executor = executor

        def pubspecInRoot = new File("${pathToPlugin}/pubspec.yaml")
        pubspecInRoot.createNewFile()
        pubspecInRoot.write(rootPubspecYaml)

        def pubspecInExample = new File("${pathToExample}/pubspec.yaml")
        pubspecInExample.createNewFile()
        pubspecInExample.write(examplePubspecYaml)

        new File("${pathToPlugin}/android").mkdirs()
        def localProperties = new File("${pathToPlugin}/android/local.properties")
        localProperties.createNewFile()
        localProperties.write("hello=true")
        executor.putExpectation(pathToRoot, createFlutterPlugin)
        executor.putExpectation(pathToPlugin, flutterPubGet)
        executor.putExpectation(pathToExample, flutterPubGet)
        executor.putExpectation(pathToPlugin, klutterProducerInit)
        executor.putExpectation(pathToExample, klutterConsumerInit)
        executor.putExpectation(pathToExample, klutterConsumerAdd)
        executor.putExpectation(pathToExample, klutterConsumerInitIOS)
        executor.putExpectation(pathToExampleIos, iosPodUpdate)
        executor.putExpectation(pathToExampleIos, iosPodInstall)
        sut.run()

        def androidMain = plugin.toPath().resolve("android/src/main")
        androidMain.toFile().mkdirs()
        def androidSource = androidMain.resolve("kotlin")
        androidSource.toFile().mkdir()

        def ios = plugin.toPath().resolve("ios")
        ios.toFile().mkdirs()
        def iosPodspec = ios.resolve("my_awesome_plugin.podspec")
        iosPodspec.toFile().createNewFile()
        def iosClasses = ios.resolve("Classes")
        iosClasses.toFile().mkdir()
    }

    def "Generate code in new project"() {
        given:
        def myCustomTypeSrcFile = Files.createTempDirectory("")
                .resolve("sqdb_my_custom_type.json")
                .toFile()
        myCustomTypeSrcFile.createNewFile()
        myCustomTypeSrcFile.write("""
            {"className":"${myCustomType.className}","members":[{"name":"foo","type":"String","nullable":false}]}
        """)
        messages = [
                new SquintMessageSource(
                        myCustomType,
                        new SquintCustomType(
                                myCustomType.className,
                                [new SquintCustomTypeMember(fooType.name, fooType.type.className, false)]),
                        myCustomTypeSrcFile)]

        when:
        def rootPubspecYamlFile = Path.of(pathToPlugin).resolve("pubspec.yaml").toFile()
        rootPubspecYamlFile.createNewFile()
        rootPubspecYamlFile.write(rootPubspecYaml)
        def project = ProjectKt.plugin(pathToPlugin)
        def pubspec = PubspecBuilder.toPubspec(rootPubspecYamlFile)
        def srcFolder = project.root.pathToLibFolder.toPath().resolve("src")
        Path.of(pathToPlugin).resolve("platform/src/commonMain").toFile().mkdirs()

        and:
        executor.putExpectation(pathToPlugin, flutterExe + " pub get")
        executor.putExpectation(project.root.pathToLibFolder.absolutePath, dartExe + " format .")
        executor.putExpectationWithAction(pathToPlugin, flutterExe + " pub run squint_json:generate" +
                                    " --type dataclass" +
                                    " --input ${myCustomTypeSrcFile.path}" +
                                    " --output ${srcFolder.toFile().path}" +
                                    " --overwrite true" +
                                    " --generateChildClasses false" +
                                    " --includeCustomTypeImports true",
                { srcFolder.resolve("my_custom_type_dataclass.dart").toFile().createNewFile() })
        executor.putExpectation(pathToPlugin, flutterExe + " pub run squint_json:generate" +
                                        " --type serializer" +
                                        " --input ${srcFolder.resolve("my_custom_type_dataclass.dart").toFile().path}" +
                                        " --output ${srcFolder.toFile().path}" +
                                        " --overwrite true")

        and:
        GenerateCodeTaskKt.toGenerateCodeTask(new GenerateCodeOptions(
                project, pubspec, "3.0.5.macos.arm64", false, controllers, messages, [],{ println("$it") })).run()

        then:
        def flutterLib = project.root.pathToLibFile
        flutterLib.exists()
        flutterLib.text.contains("export 'src/my_custom_type_dataclass.dart';")
        flutterLib.text.contains("export 'src/my_request_scoped_broadcaster/controller.dart'")
        flutterLib.text.contains("export 'src/my_singleton/foo.dart'")
        flutterLib.text.contains("export 'src/my_singleton_broadcaster/controller.dart'")
        flutterLib.text.contains("export 'src/my_request_scoped/foo.dart'")

        and:
        def myRequestScopedControllerDartSource = srcFolder.resolve("my_request_scoped")
        myRequestScopedControllerDartSource.toFile().exists()

        def myRequestScopedBroadcastControllerDartSource = srcFolder.resolve("my_request_scoped_broadcaster")
        myRequestScopedBroadcastControllerDartSource.toFile().exists()

        def mySingletonControllerDartSource = srcFolder.resolve("my_singleton")
        mySingletonControllerDartSource.toFile().exists()

        def mySingletonBroadcastControllerDartSource = srcFolder.resolve("my_singleton_broadcaster")
        mySingletonBroadcastControllerDartSource.toFile().exists()
    }

    @Shared
    def rootPubspecYaml = """name: my_awesome_plugin
description: A new klutter plugin project.
version: 0.0.1

environment:
  sdk: '>=2.16.1 <3.0.0'
  flutter: ">=2.5.0"

dependencies:
    flutter:
        sdk: flutter

    squint_json: ^0.1.2
    klutter_ui: ^1.1.0
dev_dependencies:
    klutter: ^3.0.2
flutter:
  plugin:
    platforms:
      android:
        package: null
        pluginClass: null
      ios:
        pluginClass: null
            """

    @Shared
    def examplePubspecYaml = """name: my_awesome_plugin_example
description: Demonstrates how to use the my_awesome_plugin plugin
publish_to: 'none' # Remove this line if you wish to publish to pub.dev

environment:
  sdk: '>=2.16.1 <3.0.0'

dependencies:
    flutter:
        sdk: flutter

    my_awesome_plugin:
        path: ../

    klutter_ui: ^1.1.0
    squint_json: ^0.1.2
dev_dependencies:
    flutter_test:
        sdk: flutter

    klutter: ^3.0.2
flutter:
    uses-material-design: true
"""
}

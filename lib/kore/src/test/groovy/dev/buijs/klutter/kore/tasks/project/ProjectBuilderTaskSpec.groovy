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

import dev.buijs.klutter.kore.common.Either
import dev.buijs.klutter.kore.project.ProjectKt
import dev.buijs.klutter.kore.tasks.ExecutorKt
import dev.buijs.klutter.kore.test.TestUtil
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files

class ProjectBuilderTaskSpec extends Specification {

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
    def flutterVersion = "3.0.5.macos.arm64"

    @Shared
    def flutterExe = ProjectKt.flutterExecutable(flutterVersion).absolutePath

    @Shared
    def sut = new ProjectBuilderTask(
            new ProjectBuilderOptions(
                    Either.ok(new File(pathToRoot)),
                    Either.ok(pluginName),
                    Either.ok(groupName),
                    flutterVersion,
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

    def setupSpec() {
        plugin.mkdirs()
        example.mkdirs()
        ExecutorKt.executor = executor
        ActionDownloadFlutterKt.dryRun = true
    }

    def "Verify a new project is created"(){
        given:
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

        and:
        executor.putExpectation(pathToRoot, createFlutterPlugin)
        executor.putExpectation(pathToPlugin, flutterPubGet)
        executor.putExpectation(pathToExample, flutterPubGet)
        executor.putExpectation(pathToPlugin, klutterProducerInit)
        executor.putExpectation(pathToExample, klutterConsumerInit)
        executor.putExpectation(pathToExample, klutterConsumerInitIOS)
        executor.putExpectation(pathToExample, klutterConsumerAdd)
        executor.putExpectation(pathToExampleIos, iosPodUpdate)
        executor.putExpectation(pathToExampleIos, iosPodInstall)

        when:
        sut.run()

        then: "Klutter is added as dependency to pubspec.yaml"
        TestUtil.verify(pubspecInRoot.text, rootPubspecYamlWithKlutter)
        TestUtil.verify(pubspecInExample.text, examplePubspecYamlWithKlutter)

        and: "local.properties is copied to root"
        with(new File("$pathToPlugin/local.properties")) {
            it.exists()
            it.text.contains("hello=true")
        }

        and: "test folder is deleted"
        !new File("$pathToPlugin/test").exists()

        and: "a new README.md is created"
        with(new File("$pathToPlugin/README.md")) {
            it.exists()
            TestUtil.verify(it.text, readme)
        }
    }

    @Shared
    def readme = """
        # my_awesome_plugin
        A new Klutter plugin project. 
        Klutter is a framework which interconnects Flutter and Kotlin Multiplatform.
        
        ## Getting Started
        This project is a starting point for a Klutter
        [plug-in package](https://github.com/buijs-dev/klutter),
        a specialized package that includes platform-specific implementation code for
        Android and/or iOS. 
        
        This platform-specific code is written in Kotlin programming language by using
        Kotlin Multiplatform. 
    """

    @Shared
    def rootPubspecYaml = """
        name: my_awesome_plugin
        description: A new Flutter plugin project.
        version: 0.0.1
        homepage:
        
        environment:
          sdk: ">=2.17.6 <4.0.0"
          flutter: ">=2.5.0"
        
        dependencies:
          flutter:
            sdk: flutter
          plugin_platform_interface: ^2.0.2
        
        dev_dependencies:
          flutter_test:
            sdk: flutter
          flutter_lints: ^2.0.0
    """

    @Shared
    def rootPubspecYamlWithKlutter =
             """name: my_awesome_plugin
                description: A new klutter plugin project.
                version: 0.0.1
                
                environment:
                  sdk: '>=2.17.6 <4.0.0'
                  flutter: ">=2.5.0"
                
                dependencies:
                    flutter:
                        sdk: flutter
                
                    squint_json: ^0.1.2
                    klutter_ui: ^1.1.0
                    protobuf: ^3.1.0
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
    def examplePubspecYaml =
            """name: my_awesome_plugin_example
        description: Demonstrates how to use the my_plugin plugin.
        publish_to: 'none' # Remove this line if you wish to publish to pub.dev
        
        environment:
          sdk: '>=2.17.6 <4.0.0'
        
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
          flutter_lints: ^2.0.0
          klutter: ^3.0.2
        flutter:
          uses-material-design: true
    """

    @Shared
    def examplePubspecYamlWithKlutter =
            """name: my_awesome_plugin_example
                description: Demonstrates how to use the my_awesome_plugin plugin
                publish_to: 'none' # Remove this line if you wish to publish to pub.dev
                
                environment:
                  sdk: '>=2.17.6 <4.0.0'
                
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

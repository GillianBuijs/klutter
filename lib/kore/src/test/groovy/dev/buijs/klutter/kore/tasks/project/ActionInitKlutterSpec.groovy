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
package dev.buijs.klutter.kore.tasks.project

import dev.buijs.klutter.kore.common.Either
import dev.buijs.klutter.kore.common.EitherOk
import dev.buijs.klutter.kore.project.Config
import dev.buijs.klutter.kore.project.Dependencies
import dev.buijs.klutter.kore.tasks.project.InitKlutter
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import java.nio.file.Files

@Stepwise
class ActionInitKlutterSpec extends Specification {

    @Shared
    EitherOk validRootFolder

    @Shared
    EitherOk validPluginName

    @Shared
    EitherOk validFlutterPath

    @Shared
    Config config

    @Shared
    File klutterYaml

    @Shared
    def pluginName = "my_plugin"

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
    def pathToExample = example.absolutePath

    String flutterVersion = "3.0.5.macos.arm64"

    def setupSpec() {
        validRootFolder = Either.ok(root)
        validPluginName = Either.ok(pluginName)
        config = new Config(new Dependencies(), "2023.x.y", null, null)
        klutterYaml = root.toPath().resolve("my_plugin/kradle.yaml").toFile()
        plugin.mkdirs()
        example.mkdirs()
        def rootPubspec = new File("${pathToPlugin}/pubspec.yaml")
        rootPubspec.createNewFile()
        rootPubspec.write(rootPubspecYaml)
        new File("${pathToExample}/pubspec.yaml").createNewFile()
    }

    @Shared
    def rootPubspecYaml = """
        name: my_awesome_plugin
        description: A new Flutter plugin project.
        version: 0.0.1
        homepage:
        
        environment:
          sdk: ">=2.17.5 <3.0.0"
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
}

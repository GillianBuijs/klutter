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
package dev.buijs.klutter.jetbrains

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.openapi.Disposable
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.observable.util.whenItemSelected
import com.intellij.openapi.observable.util.whenTextChanged
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.ComponentValidator
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import dev.buijs.klutter.kore.KlutterException
import dev.buijs.klutter.kore.common.EitherNok
import dev.buijs.klutter.kore.project.*
import dev.buijs.klutter.kore.tasks.project.toGroupName
import dev.buijs.klutter.kore.tasks.project.toPluginName
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JPanel

private val knownKlutterBOMVersions = setOf(
    "2024.1.3.beta",
    "2024.1.2.beta",
    "2024.1.1.beta",
    "2023.3.1.beta",
    "2023.2.2.beta",
    "2023.2.1.beta",
    "2023.1.1.beta")

/**
 * Displays a new project screen for Klutter in the standard new project wizard.
 *
 * This wizard can be accessed by going to:
 * - Intellij: File - New - Project... and then in the left pane click on Klutter.
 * - Android Studio: File - New - New Klutter Project... and then in the left pane click on Klutter.
 */
class NewProjectWizardStep(
    private val builder: NewProjectBuilder,
    private val disposable: Disposable,
) : ModuleWizardStep() {

    /**
     * State to store user input.
     */
    private val data: NewProjectConfig = NewProjectConfig().also {
        it.appName = klutterPluginDefaultName
        it.groupName = klutterPluginDefaultGroup
        it.flutterDistribution = latestFlutterVersion(currentOperatingSystem)
    }

    /**
     * Show the panel to receive user input.
     */
    override fun getComponent() = newProjectPanel(data, disposable)

    /**
     * Validate the [NewProjectConfig] data and display an error pop-up if invalid.
     */
    override fun validate(): Boolean =
        data.validOrThrows().let { super.validate() }

    /**
     * Update the [NewProjectConfig] data.
     */
    override fun updateDataModel() {
        builder.config = data
    }
}

/**
 * Throws a [ConfigurationException] if the [NewProjectConfig] is not valid.
 *
 * Throwing an exception here enables the UI to display a pop-up showing the messages.
 */
private fun NewProjectConfig.validOrThrows() {
    this.validate().let { validation ->
        if (!validation.isValid)
            validation.messages
                .joinToString("\n") { "- $it" }
                .let { throw ConfigurationException(it) }
    }
}

/**
 * Big shiny banner! \0/
 */
private object KlutterBanner : JPanel() {

    private fun readResolve(): Any = KlutterBanner

    override fun getPreferredSize(): Dimension =
        Dimension(500, 150)

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        IconLoader.getIcon("/klutterBanner.svg", KlutterIcons::class.java).paintIcon(this, g, 0, 25)
    }

}

/**
 * The new project view.
 */
private fun newProjectPanel(data: NewProjectConfig, disposable: Disposable) = panel {
    indent {
        row {
            // KLUTTER BANNER
            cell(KlutterBanner).align(AlignY.CENTER)
        }

        val project = "Project"
        group(project) {
            // Name: [ my_plugin ]
            row("Name:") {
                textField()
                    .bindText(data.appNameObservable)
                    .widthGroup(project)
                    .align(AlignX.LEFT)
                    .bindValidator(disposable) { input ->
                        if(toPluginName(input) is EitherNok) "The Plugin Name is not valid." else null
                    }
            }

            // Group: [ com.example ]
            row("Group:") {
                textField()
                    .bindText(data.groupNameObservable)
                    .widthGroup(project)
                    .align(AlignX.LEFT)
                    .bindValidator(disposable) { input ->
                        if(toGroupName(input) is EitherNok) "The Group Name is not valid." else null
                    }
            }
        }

        val dependencies = "Dependencies"
        group(dependencies) {
            row("BOM Version:") {
                comboBox(items = knownKlutterBOMVersions)
                    .also { it.component.isEditable = true }
                    .widthGroup(dependencies)
                    .bindItem(data.gradleVersionObservable)
                    .align(AlignX.LEFT)
                    .bindValidator(disposable, isBlocking = false) { input ->
                        if(knownKlutterBOMVersions.none { it == input }) "The BOM Version is unknown." else null
                    }
            }
            row("Flutter Version:") {
                comboBox(items = flutterVersionsDescending(currentOperatingSystem).map { "${it.prettyPrintedString}" })
                    .widthGroup(dependencies)
                    .bindItem(data.flutterVersionObservable)
                    .align(AlignX.LEFT)
            }
            row {
                checkBox("Get flutter dependencies from git.")
                    .widthGroup(dependencies)
                    .bindSelected(data.useGitForPubDependenciesObservable)
                    .align(AlignX.LEFT)
            }

            row {}.comment("If enabled then all flutter dependencies are pulled from git and not pub.")
        }
    }
}

private fun Cell<*>.bindValidator(
    disposable: Disposable,
    isBlocking: Boolean = true,
    validation: (String) -> String?
) {
    ComponentValidator(disposable).let { validator ->
        validator.installOn(component)
        when (component) {
            is JBTextField -> {
                val c = component as JBTextField
                c.whenTextChanged(disposable) {
                    validator.updateInfo(
                        validation(c.text)?.let { errorMessage ->
                            val capitalized = errorMessage.replaceFirstChar { char -> char.uppercase() }
                            val info = ValidationInfo(capitalized, component)
                            if(!isBlocking) info.asWarning() else info
                        })
                }
            }

            is ComboBox<*> -> {
                val c = component as ComboBox<*>
                c.whenItemSelected { selected ->
                    validator.updateInfo(
                        validation("$selected")?.let { errorMessage ->
                            val capitalized = errorMessage.replaceFirstChar { char -> char.uppercase() }
                            val info = ValidationInfo(capitalized, component)
                            if(!isBlocking) info.asWarning() else info
                        })
                }
            }

            else -> throw KlutterException("Unsupported component for validator: ${component::javaClass}")
        }

    }
}

private val NewProjectConfig.flutterVersionObservable
    get() = observable(
        get = { this.flutterDistribution?.prettyPrintedString?.toString() ?: "" },
        set = { this.flutterDistribution = PrettyPrintedFlutterDistribution(it).flutterDistribution })

/**
 * Getter/Setter for the AppName.
 */
private val NewProjectConfig.appNameObservable
    get() = observable(
        get = { this.appName ?: "" },
        set = { this.appName = it })

/**
 * Getter/Setter for the GroupName (package name).
 */
private val NewProjectConfig.groupNameObservable
    get() = observable(
        get = { this.groupName ?: "" },
        set = { this.groupName = it })

private val NewProjectConfig.gradleVersionObservable
    get() = observable(
        get = { this.bomVersion ?: klutterBomVersion },
        set = { this.bomVersion = it })

private val NewProjectConfig.useGitForPubDependenciesObservable
    get() = observable(
        get = { this.useGitForPubDependencies ?: false },
        set = { this.useGitForPubDependencies = it })

private fun <T> observable(
    get: () -> T,
    set: (T) -> Unit,
) = object : ObservableMutableProperty<T> {

    override fun set(value: T) {
        set.invoke(value)
    }

    override fun get() = get.invoke()

    override fun afterChange(listener: (T) -> Unit) {
        // nothing
    }

}
package dev.buijs.klutter.plugins.gradle

import dev.buijs.klutter.core.*
import dev.buijs.klutter.plugins.gradle.utils.GradleLoggingWrapper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.text.StyledTextOutputFactory
import java.io.File
import javax.inject.Inject

/**
 * Parent of all Gradle Tasks.
 * This class groups all implementing class under the <b>klutter</> group and gives access to the klutter configuration.
 * The implementing class shall return a KlutterLogging object which is here outputted to the console using the GradleLoggingWrapper.
 *
 * @author Gillian Buijs
 */
abstract class KlutterTask
@Inject constructor(
    private val styledTextOutputFactory: StyledTextOutputFactory,
)
    :DefaultTask()
{
    init { group = "klutter" }

    @Internal
    val ext = project.adapter()

    /**
     * The implementing class must describe what the task does by implementing this function.
     */
    abstract fun describe()

    @TaskAction
    fun execute() = describe()

    fun modules() = ext.getModulesDto()
        ?.modules
        ?.map { File(it) }
        ?.map{ source -> getFileSafely(source,"Klutter modules") }

    fun repositories() = ext.getRepositoriesDto()?.repositories ?: emptyList()

    fun iosVersion() = ext.getIosDto()?.version?:"13.0"

    fun appName() = ext.getAppName() ?: throw KlutterConfigException("App name not set. Supply a name for the app in the app DSL.")

    fun project() = KlutterProjectFactory().fromRoot(Root(ext.root))

}

internal fun getFileSafely(file: File?, name: String): File {
    if (file == null) {
        throw KlutterConfigException("File location for '$name' is not set in Klutter Plugin.")
    }

    if(!file.exists()){
        throw KlutterConfigException("File location for '$name' does not exist: '${file.absolutePath}'")
    }

    return file
}
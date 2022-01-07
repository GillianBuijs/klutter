package dev.buijs.klutter.gradle.tasks

import dev.buijs.klutter.core.KlutterGradleException
import dev.buijs.klutter.core.KlutterLogger
import dev.buijs.klutter.gradle.KlutterTask
import dev.buijs.klutter.gradle.utils.runCommand
import org.gradle.internal.logging.text.StyledTextOutputFactory
import javax.inject.Inject

/**
 * @author Gillian Buijs
 * @contact https://buijs.dev
 */
open class BuildDebugTask
@Inject constructor(styledTextOutputFactory: StyledTextOutputFactory):
    KlutterTask(styledTextOutputFactory)
{

    override fun describe() {
        logger.info("Clean project")

        val root = project().root.folder
        val aarFile = root.resolve("./klutter/kmp.aar").absoluteFile
        if(aarFile.exists()){
            logger.debug("Deleting file: $aarFile")
            aarFile.delete()
        } else logger.debug("File does not exist, continuing: $aarFile")

        logger.info("Build KMP module")
        val kmp = project().kmp.file.also { kmpRoot ->
            listOf(
                "gradlew",
                "gradlew.bat",
                "gradle",
                "gradle/wrapper/gradle-wrapper.jar",
                "gradle/wrapper/gradle-wrapper.properties"
            ).forEach {
                kmpRoot.resolve(it).also {  file ->
                    if(!file.exists()) {
                        throw KlutterGradleException("Missing Gradle wrapper file: '$file'")
                    }
                }
            }

            logger.debug("Running ./gradlew clean build in directory '$kmpRoot'")
        }

        logger.debug("./gradlew clean build".runCommand(kmp)
            ?:throw KlutterGradleException("Oops Gradle build/clean did not seem to do work..."))

        val artifact = kmp.resolve("${project().kmp.build()}/outputs/aar/${project().kmp.moduleName()}-debug.aar")

        if(!artifact.exists()) {
            throw KlutterGradleException(
                "Artifact not found in directory '$artifact'".withStacktrace(logger)
            )
        }

        val klutterBuildDir = root.resolve(".klutter").also {
            if(!it.exists()) { it.mkdir() }
        }

        val target =  klutterBuildDir.resolve("kmp.aar").also {
            if(it.exists()) { it.delete() }
        }

        logger.info("Copy Android KMP .aar file to root .klutter directory")
        artifact.copyTo(target)
        logger.info("Update Flutter dependencies")
        logger.debug("""flutter pub get""".runCommand(root)?:"")
        logger.info("Remove Flutter iOS pods")
        root.resolve("ios/Pods").also { if(it.exists()){ it.delete() } }
        root.resolve("ios/Podfile.lock").also { if(it.exists()){ it.delete() } }
        logger.info("Pod Update")
        logger.debug("""pod update""".runCommand(project().ios.file)?:"")
        logger.info("Klutter Build finished")
    }

    private fun String.withStacktrace(logger: KlutterLogger) =
        "$this\r\n\r\nCaused by:\r\n\r\n${logger.messages().joinToString{ "${it.message}\r\n" }}"

}


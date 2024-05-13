//file:noinspection ConfigurationAvoidance
package dev.buijs.klutter.gradle

import com.google.devtools.ksp.gradle.KspExtension
import dev.buijs.klutter.gradle.tasks.AbstractTask
import dev.buijs.klutter.gradle.tasks.AbstractTaskKt
import dev.buijs.klutter.gradle.tasks.KlutterGradleTaskName
import dev.buijs.klutter.kore.KlutterTask
import dev.buijs.klutter.kore.project.RootKt
import dev.buijs.klutter.kore.test.TestPlugin
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import spock.lang.Specification

class KlutterGradlePluginSpec extends Specification {

    def "Verify the plugin is applied"() {

        given:
        def project = getGradleProjectWithAppliedKlutterPlugin()

        expect:
        project.plugins.getPlugin(KlutterGradlePlugin.class) != null

    }

    def "Verify klutter tasks are created"() {
        given:
        def project = ProjectBuilder.builder().build()

        when:
        project.pluginManager.apply("dev.buijs.klutter")

        then:
        def taskContainer = project.getTasks()

        and:
        taskContainer != null

        and:
        def arr = taskContainer.toArray()

        and:
        arr.size() != 0
    }

    def "Verify that ksp plugin is applied and configured"() {

        given:
        def project = getGradleProjectWithAppliedKlutterPlugin()

        expect:
        def ksp = project.extensions.getByType(KspExtension.class)
        def args = ksp.getArguments()
        args.containsKey(RootKt.kspArgumentKlutterProjectFolder)
    }

    def "Verify that ksp generated code is added to KMP sourceSets and KMP dependent tasks are added"() {

        given:
        def project = ProjectBuilder.builder().build()

        and:
        project.extensions.add("kmp", Stub(KotlinMultiplatformExtension) {
            it.sourceSets >> Stub(NamedDomainObjectContainer<KotlinSourceSet>) {
                it.getByName("commonMain") >> Stub(KotlinSourceSet) {
                    it.kotlin >> Stub(SourceDirectorySet) {
                        it.srcDir(_)
                    }
                }
            }
        })

        and:
        project.tasks.register("build", DummyGradleTask.class)
        project.tasks.register("assemblePlatformReleaseXCFramework", DummyGradleTask.class)
        project.tasks.register(KlutterGradleTaskName.GenerateProtoSchemas.taskName, DummyGradleTask.class)

        when:
        project.pluginManager.apply("dev.buijs.klutter")

        and: "afterEvaluate is triggered"
        project.getTasksByName(KlutterGradleTaskName.CopyAndroidAarFile.taskName, false)

        then:
        with(project.tasks.getByName("build") as DummyGradleTask) {
            it.finalizers.contains("assemblePlatformReleaseXCFramework")
            it.finalizers.contains(KlutterGradleTaskName.GenerateProtoSchemas.taskName)
            it.finalizers.contains(KlutterGradleTaskName.CopyAndroidAarFile.taskName)
        }

        with(project.tasks.getByName("assemblePlatformReleaseXCFramework") as DummyGradleTask) {
            it.finalizers.contains(KlutterGradleTaskName.CopyIosFramework.taskName)
        }

        with(project.tasks.getByName(KlutterGradleTaskName.GenerateProtoSchemas.taskName) as DummyGradleTask) {
            it.finalizers.contains(KlutterGradleTaskName.CompileProtoSchemas.taskName)
        }
    }

    def "Verify ISE is thrown if wrong class is found for klutter extension"() {
        given:
        def container = Stub(ExtensionContainer) {
            it.getByName("klutter") >> "Not an extension"
        }

        def project = Stub(Project) {
            it.extensions >> container
        }

        when:
        AbstractTaskKt.klutterExtension(project)

        then:
        IllegalStateException ise = thrown()
        ise.message == "klutter extension is not of the correct type"
    }

    static getGradleProjectWithAppliedKlutterPlugin() {
        def klutterProject = new TestPlugin()
        def project = ProjectBuilder.builder()
                .withProjectDir(klutterProject.root)
                .build()
        project.pluginManager.apply("dev.buijs.klutter")
        project.getTasks()
        project.getTasksByName(KlutterGradleTaskName.CopyAndroidAarFile.taskName, false)
        project
    }

    static class DummyGradleTask extends AbstractTask {

        def finalizers = []

        @Override
        KlutterTask klutterTask$gradle() {
            return new KlutterTask() {
                @Override
                void run() { }
            }
        }

        @Override
        void setFinalizedBy(Iterable<?> finalizedByTasks) {
            finalizers.addAll(finalizedByTasks)
        }

        @Override
        KlutterGradleTaskName getGradleTaskName$gradle() {
            return null
        }
    }

}
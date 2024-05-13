package dev.buijs.klutter.kore.tasks.codegen

import spock.lang.Specification

import java.nio.file.Files

class CompileProtoSchemaTaskSpec extends Specification {

    def "Verify generated proto files are renamed correctly"() {
        given:
        def destination = Files.createTempDirectory("").toFile()
        def schemaFile = destination.toPath().resolve("foo.bar.someclass.proto").toFile()
        schemaFile.createNewFile()
        def classInput = destination.toPath().resolve("foo.bar.someclass.pb.dart").toFile()
        classInput.createNewFile()
        classInput.write("class SomeClass")
        def enumInput = destination.toPath().resolve("foo.bar.someclass.pbenum.dart").toFile()
        enumInput.createNewFile()
        enumInput.write("class SomeClass")

        expect:
        with(CompileProtoSchemaTaskKt.renameProtoGeneratedFiles(schemaFile, destination)) {
            it.size() == 2
            it.any { it.name.contains("someclass.pb.dart")}
            it.any { it.name.contains("someclass.pbenum.dart")}
        }
    }

    def "Verify generated files that have no class definition are deleted"() {
        given:
        def destination = Files.createTempDirectory("").toFile()
        def schemaFile = destination.toPath().resolve("foo.bar.someclass.proto").toFile()
        schemaFile.createNewFile()
        def classInput = destination.toPath().resolve("foo.bar.someclass.pb.dart").toFile()
        classInput.createNewFile()
        classInput.write("class SomeClass")
        def enumInput = destination.toPath().resolve("foo.bar.someclass.pbenum.dart").toFile()
        enumInput.createNewFile()
        enumInput.write("// Comments BLA BLA")

        expect:
        with(CompileProtoSchemaTaskKt.renameProtoGeneratedFiles(schemaFile, destination)) {
            it.size() == 1
            it[0].name.contains("someclass.pb.dart")
        }

    }

    def "Verify generated files that are obsolete are deleted"() {
        given:
        def destination = Files.createTempDirectory("").toFile()
        def schemaFile = destination.toPath().resolve("foo.bar.someclass.proto").toFile()
        schemaFile.createNewFile()
        def pbjsonFile = destination.toPath().resolve("foo.bar.someclass.pbjson.dart").toFile()
        pbjsonFile.createNewFile()
        def pbserverFile = destination.toPath().resolve("foo.bar.someclass.pbserver.dart").toFile()
        pbserverFile.createNewFile()

        and:
        pbjsonFile.exists()
        pbserverFile.exists()

        when:
        CompileProtoSchemaTaskKt.deleteObsoleteGeneratedFiles(schemaFile, destination)

        then:
        !pbjsonFile.exists()
        !pbserverFile.exists()

    }

}

package dev.buijs.klutter.gradle.dsl

import spock.lang.Specification

class KlutterPluginDSLSpec extends Specification {

    def "Verify a KlutterPluginDTO is created by using the KlutterPluginDSL" () {

        when:
        def dto = new KlutterPluginBuilder()
        dto.name = "awesome_plugin"

        then:
        dto.build().name == "awesome_plugin"
    }

}

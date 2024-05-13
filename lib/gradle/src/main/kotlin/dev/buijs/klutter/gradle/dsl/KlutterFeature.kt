package dev.buijs.klutter.gradle.dsl

enum class KlutterFeature {
    /**
     * Enable the protocol buffer feature.
     *
     * This replaces JSON (de)serialization as communication method between Kotlin and Dart.
     */
    PROTOBUF
}
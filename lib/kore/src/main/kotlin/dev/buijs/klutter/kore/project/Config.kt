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
package dev.buijs.klutter.kore.project

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import dev.buijs.klutter.kore.common.verifyExists
import mu.KotlinLogging
import java.io.File

private val log = KotlinLogging.logger { }

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "bom-version",
    "flutter-version",
    "feature-protobuf-enabled",
    "dependencies")
data class Config(
    @JsonProperty("dependencies")
    val dependencies: Dependencies = Dependencies(),

    @JsonProperty("bom-version")
    val bomVersion: String = klutterBomVersion,

    @JsonProperty("flutter-version")
    val flutterVersion: String? = null,

    @JsonProperty("feature-protobuf-enabled")
    val featureProtobufEnabled: Boolean? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "klutter",
    "klutter_ui",
    "squint",
    "embedded")
data class Dependencies(
    @JsonProperty("klutter")
    val klutter: String? = null,

    @JsonProperty("klutter_ui")
    val klutterUI: String? = null,

    @JsonProperty("squint_json")
    val squint: String? = null,

    @JsonProperty("embedded")
    val embedded: Set<String> = emptySet()
)

/**
 * [Dependencies] instance which uses GIT for all dependencies.
 */
val gitDependencies = Dependencies(
    klutter = "https://github.com/buijs-dev/klutter-dart.git@develop",
    klutterUI = "https://github.com/buijs-dev/klutter-dart-ui.git@develop",
    squint = "https://github.com/buijs-dev/squint.git@develop",
    embedded = emptySet())

fun File.toConfigOrNull(): Config? = try {
    mapper.readValue(verifyExists().readText(), Config::class.java)
} catch(e: Exception) { log.error { e }; null }
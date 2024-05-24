@file:JvmName("PubspecBuilder")
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

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dev.buijs.klutter.kore.KlutterException
import dev.buijs.klutter.kore.common.verifyExists
import dev.buijs.klutter.kore.common.write
import dev.buijs.klutter.kore.templates.flutter.createExamplePubspecYamlWriter
import dev.buijs.klutter.kore.templates.flutter.createRootPubspecYamlWriter
import java.io.File

/**
 * The version of Klutter Gradle Executable Tool.
 */
const val klutterBomVersion = "2024.1.3.beta"

/**
 * The version of the klutter Pub Plugin.
 */
const val klutterPubVersion = "3.0.2"

/**
 * The version of the klutter-ui Pub Plugin.
 */
const val klutterUIPubVersion = "1.1.0"

/**
 * The version of the squint_json Pub Plugin.
 */
const val squintPubVersion = "0.1.2"

/**
 * Mapper to read/write YAML.
 */
val mapper = YAMLFactory.builder()
    .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
    .build()
    .let { factory ->
        ObjectMapper(factory)
            .also {
                it.registerKotlinModule()
                it.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
                it.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
                it.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                it.setVisibility(it.serializationConfig.defaultVisibilityChecker
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                        .withGetterVisibility(JsonAutoDetect.Visibility.ANY)
                        .withSetterVisibility(JsonAutoDetect.Visibility.ANY)
                        .withCreatorVisibility(JsonAutoDetect.Visibility.ANY))
            }
    }

fun Root.toPubspec(): Pubspec =
    folder.resolve("pubspec.yaml").toPubspec()

fun File.toPubspec(): Pubspec {
    try {
        return mapper.readValue(verifyExists().readText(), Pubspec::class.java)
    } catch(e: Exception) {
        throw KlutterException("Failed to parse pubspec.yaml: ${e.message}")
    }
}

@JvmOverloads
fun rootPubspecInit(
    pubspecFile: File,
    pubspec: Pubspec,
    config: Config? = null,
): File {
    pubspecFile.write(createRootPubspecYamlWriter(pubspec, config))
    return pubspecFile
}

@JvmOverloads
fun examplePubspecInit(
    examplePubspecFile: File,
    rootPubspec: Pubspec,
    config: Config? = null,
): File {
    examplePubspecFile.write(createExamplePubspecYamlWriter(rootPubspec, config))
    return examplePubspecFile
}

fun Pubspec.iosClassName(orElse: String): String {
    return when {
        ios == null -> {
            orElse
        }
        ios!!.pluginClass.isNullOrBlank() -> {
            orElse
        }
        else -> {
            ios!!.pluginClass!!
        }
    }
}

fun Pubspec.androidClassName(orElse: String): String {
    return when {
        android == null -> {
            orElse
        }
        android!!.pluginClass.isNullOrBlank() -> {
            orElse
        }
        else -> {
            android!!.pluginClass!!
        }
    }
}

fun Pubspec.androidPackageName(): String {
    return when {
        android == null -> {
            ""
        }
        android!!.pluginPackage == null -> {
            ""
        }
        else -> {
            android!!.pluginPackage!!
        }
    }
}
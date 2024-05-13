package dev.buijs.klutter.kore.templates

import dev.buijs.klutter.kore.KlutterPrinter

class ProtobufExtensions(
    private val packageName: String,
    private val className: String,
): KlutterPrinter {

    override fun print(): String = """|package $packageName
|
|import kotlinx.serialization.ExperimentalSerializationApi
|import kotlinx.serialization.decodeFromByteArray
|import kotlinx.serialization.encodeToByteArray
|import kotlinx.serialization.protobuf.ProtoBuf
|
|@OptIn(ExperimentalSerializationApi::class)
|fun $className.encode${className}ToByteArray(): ByteArray =
|    ProtoBuf.encodeToByteArray(this)
|
|@OptIn(ExperimentalSerializationApi::class)
|fun decodeByteArrayTo$className(byteArray: ByteArray): $className =
|    ProtoBuf.decodeFromByteArray(byteArray)""".trimMargin()

}
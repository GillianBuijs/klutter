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

package dev.buijs.klutter.kore.templates

import dev.buijs.klutter.kore.*
import dev.buijs.klutter.kore.ast.*
import dev.buijs.klutter.kore.ast.Method
import dev.buijs.klutter.kore.common.toSnakeCase

class IosAdapter(
    private val pluginClassName: String,
    private val isProtobufEnabled: Boolean,
    methodChannels: Set<String>,
    eventChannels: Set<String>,
    controllers: Set<Controller>,
): KlutterPrinter {

    private val imports = setOf(
        "import Flutter",
        "import UIKit",
        "import Platform")

    private val methodChannelNames =
        methodChannels.map { """      "$it", """ }

    private val eventChannelNames =
        eventChannels.map { """      "$it", """ }

    private val methodChannelHandlerSwitchClauses = controllers
        .flatMap { it.functions }
        .map { listOf(
            """        case "${it.command}":""",
            """            self.${it.command}(data: data, result: result)""")
        }
        .flatten()

    private val methodChannelHandlerFunctions = controllers
        .flatMap { it.functions.flatMap { func -> func.methodHandlerString(it.instanceOrConstructor()) } }

    private val singletonControllerVariables = controllers
        .filter { it is Singleton }
        .map { it.className }
        .map { """    private let ${it.replaceFirstChar { char -> char.lowercase() }}: $it = $it()""" }
        .toSet()

    private val broadcastControllerReceivers = controllers
        .filterIsInstance<BroadcastController>()
        .flatMap { listOf(
            """     case "${it.className.toSnakeCase()}":""",
            "           ${it.instanceOrConstructor()}.receiveBroadcastIOS().collect(",
            "                  onEach: { value in",
            "                            eventSink(${it.response.responseEncoder()})",
            "                        },",
            "                  onCompletion: { error in",
            """                             eventSink("ERROR: \("error")")""",
            "                        }",
            "                  )",
            "         return nil"
           )
         }

    override fun print(): String = buildString {
        appendLines(imports)
        appendLine()
        appendLine("public class $pluginClassName: NSObject, FlutterPlugin, FlutterStreamHandler {")
        appendLine()
        appendLine("    static let mcs: [String] = [")
        appendLines(methodChannelNames)
        appendLine("    ]")
        appendLine()
        appendLine("    static let ecs: [String] = [")
        appendLines(eventChannelNames)
        appendLine("    ]")
        appendLine()
        appendLine("    var methodChannels: Set<FlutterMethodChannel> = []")
        appendLine("    var eventChannels: Set<FlutterEventChannel> = []")
        appendLine()
        appendLines(singletonControllerVariables)
        appendLine()
        appendTemplate(
            """
                |    public static func register(with registrar: FlutterPluginRegistrar) {
                |        let messenger = registrar.messenger()
                |        let instance = $pluginClassName()
                |        
                |        for name in mcs {
                |           let channel = FlutterMethodChannel(name: name, binaryMessenger: messenger)
                |            instance.methodChannels.insert(channel)
                |            registrar.addMethodCallDelegate(instance, channel: channel)
                |        }
                |        
                |        for name in ecs {
                |            let channel = FlutterEventChannel(name: name, binaryMessenger: messenger)
                |            channel.setStreamHandler(instance)
                |        }
                |    }     
                |""")
        appendLine("    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {")
        appendLine("        let data = call.arguments")
        appendLine("        switch call.method {")
        appendLines(methodChannelHandlerSwitchClauses)
        appendLine("         default:")
        appendLine("            result(FlutterMethodNotImplemented)")
        appendLine("         }")
        appendLine("     }")
        appendLine()
        appendLines(methodChannelHandlerFunctions)
        appendTemplate("""
            |    public func onListen(withArguments: Any?, eventSink: @escaping FlutterEventSink) -> FlutterError? {
            |        let topic = withArguments ?? "none"
            |        switch "\(topic)" {
           """)
        appendLines(broadcastControllerReceivers)
        appendTemplate("""|          case "none":
            |           eventSink(FlutterError(code: "ERROR_CODE",
            |                                         message: "Topic not provided!",
            |                                         details: ""))
            |        default:
            |           eventSink(FlutterError(code: "ERROR_CODE",
            |                               message: "Unknown topic",
            |                               details: "\(topic)"))
            |        }
            |        return nil
            |     }
            |
            |    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
            |        for channel in eventChannels {
            |            channel.setStreamHandler(nil)
            |        }
            |        return nil
            |    }
            |
            |}
        """)
    }

    private fun Method.methodHandlerString(instanceOrConstuctor: String): List<String> {
        val method = method.replace("(context)", """(context: "")""")

        val responseEncoder =
            responseDataType.responseEncoder()

        if(this.requestDataType != null) {
            return methodHandlerWithArgument(instanceOrConstuctor, responseEncoder)
        }

        if(this.responseDataType is UnitType) {
            return methodHandlerNoArgumentNoResponse(instanceOrConstuctor)
        }

        return if(async) {
            listOf(
                """|    func ${command}(data: Any?, result: @escaping FlutterResult) {
                   |        $instanceOrConstuctor.${method.removeSuffix("()")} { maybeData, error in
                   |            if let value = maybeData { 
                   |                $responseEncoder 
                   |            }
                   |
                   |            if let failure = error { result(failure) }
                   |        }
                   |    }
                   |    
                """.trimMargin())
        } else {
            listOf(
                """|    func ${command}(data: Any?, result: @escaping FlutterResult) {
                    |       let value = $instanceOrConstuctor.$method()
                   |        $responseEncoder
                   |    }
                   |    
                """.trimMargin())
        }
    }

    private fun Method.methodHandlerNoArgumentNoResponse(instanceOrConstuctor: String): List<String> {
        val method = method.replace("(context)", """(context: "")""")

        return if(async) {
            listOf(
                """|    func ${command}(data: Any?, result: @escaping FlutterResult) {
                   |        $instanceOrConstuctor.${method.removeSuffix("()")} { error in
                   |            result("")
                   |        }
                   |    }
                   |    
                """.trimMargin())
        } else {
            listOf(
                """|    func ${command}(data: Any?, result: @escaping FlutterResult) {
                   |        $instanceOrConstuctor.$method()
                   |        result("")
                   |    }
                   |    
                """.trimMargin())
        }
    }

    private fun Method.methodHandlerWithArgument(instanceOrConstuctor: String, responseEncoder: String): List<String> {

        var requiresBytesDecoding = false

        val requestDecoder = if(isProtobufEnabled) {
            when(requestDataType) {
                is StringType -> "TypeHandlerKt.stringOrNull(data: data)"
                is IntType -> "TypeHandlerKt.intOrNull(data: data)"
                is DoubleType -> "TypeHandlerKt.intOrNull(data: data)"
                is BooleanType -> "TypeHandlerKt.booleanOrNull(data: data)"
                is LongType -> "TypeHandlerKt.intOrNull(data: data)"
                is IntArrayType -> "TypeHandlerKt.intArrayOrNull(data: data)"
                is LongArrayType -> "TypeHandlerKt.longArrayOrNull(data: data)"
                is FloatArrayType -> "TypeHandlerKt.floatArrayOrNull(data: data)"
                is DoubleArrayType -> "TypeHandlerKt.doubleArrayOrNull(data: data)"
                else -> {
                    requiresBytesDecoding = true
                    "TypeHandlerKt.byteArrayOrNull(data: data)"
                }
            }
        } else {
            when(requestDataType) {
                is StringType -> "TypeHandlerKt.stringOrNull(data: data)"
                is IntType -> "TypeHandlerKt.intOrNull(data: data)"
                is DoubleType -> "TypeHandlerKt.intOrNull(data: data)"
                is BooleanType -> "TypeHandlerKt.booleanOrNull(data: data)"
                is LongType -> "TypeHandlerKt.intOrNull(data: data)"
                is ByteArrayType -> "TypeHandlerKt.byteArrayOrNull(data: data)"
                is IntArrayType -> "TypeHandlerKt.intArrayOrNull(data: data)"
                is LongArrayType -> "TypeHandlerKt.longArrayOrNull(data: data)"
                is FloatArrayType -> "TypeHandlerKt.floatArrayOrNull(data: data)"
                is DoubleArrayType -> "TypeHandlerKt.doubleArrayOrNull(data: data)"
                is ListType -> "TypeHandlerKt.listOrNull(data: data)"
                is MapType -> "TypeHandlerKt.mapOrNull(data: data)"
                is CustomType -> "TypeHandlerKt.deserialize(data)"
                is EnumType -> "TypeHandlerKt.deserialize(data)"
                else -> throw KlutterException("Found unsupported request Type: $requestDataType")
            }
        }

        val swiftRequestDataType = when(requestDataType) {
            is StringType -> "as! String"
            is IntType -> "as! Int32"
            is DoubleType -> "as! Double"
            is LongType -> "as! Int64"
            is BooleanType -> "as! Bool"
            is ByteArrayType -> "as! Data"
            is IntArrayType -> "as! Data"
            is LongArrayType -> "as! Data"
            is FloatArrayType -> "as! Data"
            is DoubleArrayType -> "as! Data"
            is ListType -> {
                when(requestDataType.child) {
                    is BooleanType -> "as! Array<KotlinBoolean>"

                    is DoubleType -> "as! Array<KotlinDouble>"

                    is IntType -> "as! Array<KotlinInt>"

                    is LongType -> "as! Array<KotlinLong>"

                    else -> {
                        throw KlutterException("Found unsupported List child Type: ${requestDataType.child}")
                    }
                }
            }
            is CustomType, is EnumType -> "as! ${requestDataType.typeSimplename()}"
            else -> throw KlutterException("Found unsupported request Type: $requestDataType")
        }

        val lines = mutableListOf(
            "    func ${command}(data: Any?, result: @escaping FlutterResult) {")

        // Cast to Array (for supported types)
        if(requestDataType is ListType) {
            when(requestDataType.child) {
                is BooleanType -> {
                    lines.add("        let dataOrNull = data as? Array<KotlinBoolean>")
                }
                is DoubleType -> {
                    lines.add("        let dataOrNull = data as? Array<KotlinDouble>")
                }
                is IntType -> {
                    lines.add("        let dataOrNull = data as? Array<KotlinInt>")
                }
                is LongType -> {
                    lines.add("        let dataOrNull = data as? Array<KotlinLong>")
                }
                else -> {
                   throw KlutterException("Found unsupported List child Type: ${requestDataType.child}")
                }
            }
        } else {
            lines.add("        let dataOrNull = $requestDecoder")
        }

        lines.add("        if(dataOrNull == nil) {")
        lines.add("           result(FlutterError(code: \"ERROR_CODE\",")
        lines.add("                           message: \"Unexpected data type: \\(dataOrNull)\",")
        lines.add("                           details: nil))")
        lines.add("        } else {")


        if(requiresBytesDecoding)
            lines.add("let data = ProtocolBufferGenerated${requestDataType.className}Kt.decodeByteArrayTo${requestDataType.className}(byteArray: dataOrNull!)")

        val dataOrNull = if(requiresBytesDecoding) {
            "(data $swiftRequestDataType)"
        } else {
            "(dataOrNull $swiftRequestDataType)"
        }

        if(async) {
            if(responseDataType is UnitType) {
                lines.add("           $instanceOrConstuctor.$method($requestParameterName: $dataOrNull) { error in")
                lines.add("                if let failure = error { result(failure) }")
                lines.add("                result(dataOrNull!)")
                lines.add("            }")
            } else {
                lines.add("           $instanceOrConstuctor.$method($requestParameterName: $dataOrNull) { maybeData, error in")
                lines.add("                if let value = maybeData { $responseEncoder }")
                lines.add("                if let failure = error { result(failure) }")
                lines.add("            }")
            }
        } else {
            if(responseDataType is UnitType) {
                lines.add("           $instanceOrConstuctor.$method($requestParameterName: $dataOrNull)")
                lines.add("           result($dataOrNull)")
            } else {
                lines.add("           result($instanceOrConstuctor.$method($requestParameterName: $dataOrNull))")
            }
        }

        lines.add("         }")
        lines.add("     }")
        return lines
    }

    private fun Controller.instanceOrConstructor() = when(this) {
        is Singleton -> className.replaceFirstChar { char -> char.lowercase() }
        else -> "${className}()"
    }

    private fun AbstractType.responseEncoder(): String {
        return when {
            this is StandardType -> "result(value)"
            isProtobufEnabled ->
                """|let bytes = value.encode${className}ToByteArray()
                    |       var array = [UInt8]()
                    |       var iterator = bytes.iterator()
                    |       while iterator.hasNext() {
                    |           array.append(UInt8(iterator.nextByte()))
                    |       }
                    |       result(FlutterStandardTypedData(bytes: Data(array)))
                """.trimMargin()
            else -> "result(TypeHandlerKt.encode(value))"
        }
    }


}
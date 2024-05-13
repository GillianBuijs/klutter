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
package dev.buijs.klutter.kore.templates.flutter

import dev.buijs.klutter.kore.KlutterPrinter
import dev.buijs.klutter.kore.ast.*
import dev.buijs.klutter.kore.common.toSnakeCase
import dev.buijs.klutter.kore.templates.dartType

internal fun PublisherWidget.print() = createPrinter().print()

/**
 * Create a [KlutterPrinter] instance which generates a Flutter Producer Widget.
 *
 * Example
 * </br>
 * Given input:
 * - channel.name: com.example.awesome.my_awesome_app
 * - event.name: greeting
 * - extension.className: GreetingEvent
 * - request.type: String
 * - response.type: String
 * - method.name: getGreeting
 *
 * Will generate:
 * ```
 * import 'package:flutter/services.dart';
 * import 'package:flutter/widgets.dart';
 * import 'package:klutter_ui/klutter_ui.dart';
 *
 * const MethodChannel _channel =
 *   MethodChannel('com.example.awesome.my_awesome_app');
 *
 * extension GreetingEvent on State {
 *   void getGreeting({
 *     required String message,
 *     void Function(String)? onSuccess,
 *     void Function(Exception)? onFailure,
 *     void Function()? onNullValue,
 *     void Function(AdapterResponse<String>)? onComplete,
 *   }) => doEvent<String>(
 *     state: this,
 *     event: "greeting",
 *     message: message,
 *     channel: _channel,
 *     onSuccess: onSuccess,
 *     onFailure: onFailure,
 *     onNullValue: onNullValue,
 *     onComplete: onComplete,
 *   );
 * }
 *
 * void getGreeting({
 *   required String message,
 *   State? state,
 *   void Function(String)? onSuccess,
 *   void Function(Exception)? onFailure,
 *   void Function()? onNullValue,
 *   void Function(AdapterResponse<String>)? onComplete,
 * }) => doEvent<String>(
 *   state: state,
 *   event: "greeting",
 *   message: message,
 *   channel: _channel,
 *   onSuccess: onSuccess,
 *   onFailure: onFailure,
 *   onNullValue: onNullValue,
 *   onComplete: onComplete,
 * );
 * ```
 */
fun PublisherWidget.createPrinter(): KlutterPrinter {

    val requiresMessage =
        requestType != null

    val requiresRequestEncoder =
        requiresMessage && requestType!!.dataType !is StandardType

    val requiresResponseDecoder =
        responseType.dataType !is StandardType

    val requestEncoderOrBlank = if(isProtobufEnabled) {
        when(requestType?.dataType) {
            is CustomType -> {
                "encodeBuffer: (dynamic data) => (data as ${requestType.dataType.className}).writeToBuffer(),"
            }

            is EnumType -> {
                "encodeBuffer: (dynamic data) => Uint8List.fromList([(data as ${responseType.dataType.className}).value]),"
            }

            else -> ""
        }
    } else {
        when(requestType?.dataType) {
            is CustomType -> {
                "encode: (dynamic data) => (data as ${requestType.dataType.className}).toJson,"
            }

            is EnumType -> {
                "encode: (dynamic data) => (data as ${requestType.dataType.className}).toJsonValue,"
            }

            else -> ""
        }
    }

    val template = buildString {
        append(
            """
            |import 'package:flutter/services.dart';
            |import 'package:flutter/widgets.dart';
            |import 'package:klutter_ui/klutter_ui.dart';
            """
        )

        if(requiresRequestEncoder) {
            if(isProtobufEnabled) {
                if(requestType?.dataType is EnumType) {
                    append("import '../${requestType.dartType.toSnakeCase().replace("_", "")}.pbenum.dart';")
                } else {
                    append("import '../${requestType?.dartType?.toSnakeCase()?.replace("_", "")}.pb.dart';")
                }
            } else {
                append(
                    """
                |import '../${requestType?.dartType?.toSnakeCase()}_dataclass.dart';
                |import '../${requestType?.dartType?.toSnakeCase()}_extensions.dart';
                """
                )
            }
        }

        if(requiresResponseDecoder) {
            if(isProtobufEnabled) {
                if(responseType.dataType is EnumType) {
                    append("import '../${responseType.dartType.toSnakeCase().replace("_", "")}.pbenum.dart';")
                } else {
                    append("import '../${responseType.dartType.toSnakeCase().replace("_", "")}.pb.dart';")
                }
            } else {
                append(
                    """
                |import '../${responseType.dartType.toSnakeCase()}_dataclass.dart';
                |import '../${responseType.dartType.toSnakeCase()}_extensions.dart';
                """
                )
            }
        }

        append(
            """
            |
            |const MethodChannel _channel =
            |  MethodChannel('${channel.name}');
            |
            |extension ${extension.className} on State {
            |  void ${method.name}({"""
        )

        if(requiresMessage) {
            append("    required ${requestType?.dartType} message,")
        }

        append("""
            |    void Function(${responseType.dartType})? onSuccess,
            |    void Function(Exception)? onFailure,
            |    void Function()? onNullValue,
            |    void Function(AdapterResponse<${responseType.dartType}>)? onComplete,
            |}) => doEvent<${responseType.dartType}>(
            |    state: this,
            |    event: "${event.name}",
            |    channel: _channel,
            |    onSuccess: onSuccess,
            |    onFailure: onFailure,
            |    onNullValue: onNullValue,
            |    onComplete: onComplete,
            """)

        if(requiresRequestEncoder) {
            append(requestEncoderOrBlank)
        }

        if(requiresResponseDecoder) {
            if(isProtobufEnabled) {
                if(responseType.dataType is EnumType) {
                    append("decodeBuffer: (List<int> buffer) => ${responseType.dataType.className}.valueOf(buffer[0])!,")
                } else {
                    append("decodeBuffer: (List<int> buffer) => ${responseType.dataType.className}.fromBuffer(buffer),")
                }
            } else {
                append("decode: (String json) => json.to${responseType.dataType.className},")
            }
        }

        append("""
            |  );
            |}
            |
            |void ${method.name}({
        """)

        if(requiresMessage) {
            append("|    required ${requestType?.dartType} message,")
        }

        append("""
            |    State? state, 
            |    void Function(${responseType.dartType})? onSuccess,
            |    void Function(Exception)? onFailure,
            |    void Function()? onNullValue,
            |    void Function(AdapterResponse<${responseType.dartType}>)? onComplete,
            |}) => doEvent<${responseType.dartType}>(
            |    state: state,
            |    event: "${event.name}",
            |    channel: _channel,
            |    onSuccess: onSuccess,
            |    onFailure: onFailure,
            |    onNullValue: onNullValue,
            |    onComplete: onComplete,
            """)

        if(requiresMessage) {
            append("|    message: message,")
        }

        if(requiresRequestEncoder) {
            append(requestEncoderOrBlank)
        }

        if(requiresResponseDecoder) {
            if(isProtobufEnabled) {
                if(responseType.dataType is EnumType) {
                    append("decodeBuffer: (List<int> buffer) => ${responseType.dataType.className}.valueOf(buffer[0])!,")
                } else {
                    append("decodeBuffer: (List<int> buffer) => ${responseType.dataType.className}.fromBuffer(buffer),")
                }
            } else {
                append("decode: (String json) => json.to${responseType.dataType.className},")
            }
        }

        append("""
            |);
            |
        """)

    }

    return object: KlutterPrinter {
        override fun print(): String = template.trimMargin()
    }

}

/**
 * Wrapper for all fields to configure code generation for a Flutter Widget.
 */
data class PublisherWidget(
    /**
     * The MethodChannel value used to communicate.
     */
    val channel: FlutterSyncChannel,

    /**
     * Name of the event to execute.
     */
    val event: FlutterEvent,

    /**
     * Name of the extension class to be generated.
     *
     * Example:
     * </br>
     * Given the value <b>GreetingEvent</b>, will generate the following:
     *
     * ```
     * extension GreetingEvent on State {
     *      // Code omitted for brevity...
     * }
     * ```
     */
    val extension: FlutterExtension,

    /**
     * Type of data (if any) returned after finishing the event.
     */
    val requestType: FlutterMessageType? = null,

    /**
     * Type of data (if any) returned after finishing the event.
     */
    val responseType: FlutterMessageType,

    /**
     * Name of the method to be generated.
     *
     * Example:
     * </br>
     * Given the value <b>getGreeting</b>, will generate the following:
     *
     * ```
     * void getGreeting({
     *      // Code omitted for brevity...
     * }) => doEvent<String>(
     *      // Code omitted for brevity...
     * );
     * ```
     */
    val method: FlutterMethod,

    val isProtobufEnabled: Boolean,
)

/**
 * Name of the event to execute.
 */
data class FlutterEvent(
    val name: String
)

/**
 * Name of the extension class to be generated.
 *
 * Example:
 * </br>
 * Given the value <b>GreetingEvent</b>, will generate the following:
 *
 * ```
 * extension GreetingEvent on State {
 *      // Code omitted for brevity...
 * }
 * ```
 */
data class FlutterExtension(
    val className: String
)

/**
 * Data Type used to communicate between Flutter and Kotlin.
 */
data class FlutterMessageType(
    val dataType: AbstractType
) {
    val dartType
        get() = dataType.dartType()
}

/**
 * Name of the method to be generated.
 *
 * Example:
 * </br>
 * Given the value <b>getGreeting</b>, will generate the following:
 *
 * ```
 * void getGreeting({
 *      // Code omitted for brevity...
 * }) => doEvent<String>(
 *      // Code omitted for brevity...
 * );
 * ```
 */
data class FlutterMethod(
    val name: String
)
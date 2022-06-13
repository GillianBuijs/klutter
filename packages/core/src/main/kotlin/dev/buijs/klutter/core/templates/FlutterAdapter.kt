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

package dev.buijs.klutter.core.templates

import dev.buijs.klutter.core.*
import dev.buijs.klutter.core.Method

internal class FlutterAdapter(
    private val pluginClassName: String = "Adapter",
    private val methodChannelName: String = "KLUTTER",
    private val methods: List<Method>,
    private val messages: List<DartMessage>,
    private val enumerations: List<DartEnum>,
): KlutterPrinter {

    override fun print(): String {

        val messages = messages.joinToString("\n" + "\n") {
            MessagePrinter(it).print()
        }

        val enumerations = enumerations.joinToString("\n" + "\n") {
            EnumerationPrinter(it).print()
        }

        val block = methods.joinToString("\r\n\r\n") { it.printFun() }

        return """
            |import 'dart:async';
            |import 'package:flutter/services.dart';
            |
            |/// Autogenerated by Klutter Framework. 
            |/// 
            |/// Do net edit directly, but recommended to store in VCS.
            |/// 
            |/// Adapter class which handles communication with the KMP library.
            |class $pluginClassName {
            |  static const MethodChannel _channel = MethodChannel('$methodChannelName');
            |  
            $block
            |
            |}
            |
            |/// Autogenerated by Klutter Framework. 
            |/// 
            |/// Do net edit directly, but recommended to store in VCS.
            |/// 
            |/// Wraps an [exception] if calling the platform method has failed to be logged by the consumer.
            |/// Or wraps an [object] of type T when platform method has returned a response and
            |/// deserialization was successful.
            |class AdapterResponse<T> {
            |
            |  AdapterResponse(this._object, this._exception);
            |
            |  factory AdapterResponse.success(T t) => AdapterResponse(t, null);
            |
            |  factory AdapterResponse.failure(Exception e) => AdapterResponse(null, e);
            |  
            |  ///The actual object to returned
            |  T? _object;
            |  set object(T object) => _object = object;
            |  T get object => _object!;
            |
            |  ///Exception which occurred when calling a platform method failed.
            |  Exception? _exception;
            |  set exception(Exception e) => _exception = e;
            |  Exception get exception => _exception!;
            |
            |  bool isSuccess() {
            |    return _object != null;
            |  }
            |   
            |}
            |
            |${messages}
            |
            |${enumerations}
            |
            """.trimMargin()
    }

    private fun Method.printFun() =
        if(DartKotlinMap.toMapOrNull(dataType) == null) {
            """|  static Future<AdapterResponse<${dataType}>> get $command async {
           |    try {
           |      final response = await _channel.invokeMethod('${command}');
           |      final json = jsonDecode(response);
           |      return AdapterResponse.success(${serializer()});
           |    } catch (e) {
           |      return AdapterResponse.failure(
           |          e is Error ? Exception(e.stackTrace) : e as Exception
           |      );
           |    }
           |  }"""
        } else {
            """|  static Future<AdapterResponse<${dataType}>> get $command async {
           |    try {
           |      final json = await _channel.invokeMethod('${command}');
           |      return AdapterResponse.success(${serializer()});
           |    } catch (e) {
           |      return AdapterResponse.failure(
           |          e is Error ? Exception(e.stackTrace) : e as Exception
           |      );
           |    }
           |  }"""
        }


    private fun Method.serializer(): String {

        val listRegex = """List<([^>]+?)>""".toRegex()

        var isList = false
        var type = dataType
        val q = if(type.contains("?")) "?" else ""

        listRegex.find(type)?.let {
            isList = true
            type = it.groups[1]?.value ?: type
        }

        val dartType = DartKotlinMap.toMapOrNull(type)?.dartType

        //Standard DART datatype
        if(dartType != null) {
            return if(isList) {
                "List<$type>.from(json.map((o) => o$q${getCastMethod(dartType)}))"
            } else "json${getCastMethod(dartType)}"
        }

        //Custom DTO or enum
        return if(isList) {
            "List<$type>.from(json.map((o) => $type.fromJson(o)))"
        } else "$type.fromJson(json)"

    }

}

private const val BR = "\n"

/**
 * Prints all members of a class.
 */
internal class EnumerationPrinter(private val message: DartEnum): KlutterPrinter {

    override fun print() = "" +
            "class ${message.name} {$BR" +
            "final String string;$BR$BR" +
            "const ${message.name}._(this.string);" +
            printValues(message) +
            "  static const none = ${message.name}._('none');$BR" +
            BR + BR +
            "static const values = [${message.values.joinToString(",") { it.toCamelCase()}}];$BR$BR" +
            "  @override$BR" +
            "  String toString() {$BR" +
            "    return '${message.name}.\$string';\n" +
            "  }$BR" +
            EnumExtensionPrinter(message).print() +
            "}$BR"

    private fun printValues(message: DartEnum): String {

        val sb = StringBuilder()

        val jsonValues = if(message.valuesJSON.size == message.values.size) message.valuesJSON else message.values

        message.values.forEachIndexed { index, s ->
            sb.append("  static const ${s.toCamelCase()} = ${message.name}._('${jsonValues[index]}');$BR")
        }

        return sb.toString()

    }

}

/**
 * Prints all members of a class.
 */
internal class EnumExtensionPrinter(private val message: DartEnum): KlutterPrinter {

    override fun print() = """
        |
        |  static ${message.name} fromJson(String value) {
        |    switch(value) {${cases()}
        |      default: return ${message.name}.none;
        |    }
        | }
        |
        |  String? toJson() {
        |    switch(this) { ${serializers()}
        |      default: return null;
        |    }
        |  }
        |
    """.trimMargin()

    private fun cases(): String {
        if(message.valuesJSON.isEmpty()) {
            return message.values.joinToString(";") {
                "$BR      case \"$it\": return ${message.name}.${it.toCamelCase()}"
            } + ";"
        }

        var print = ""
        message.valuesJSON.forEachIndexed { index, json ->
            print += "$BR      case \"$json\": return ${message.name}.${message.values[index].toCamelCase()};"
        }

        return print
    }

    private fun serializers(): String {
        if(message.valuesJSON.isEmpty()) {
            return message.values.joinToString(";") {
                "$BR      case ${message.name}.${it.toCamelCase()}: return \"$it\""
            }  + ";"
        }

        var print = ""
        message.valuesJSON.forEachIndexed { index, json ->
            print += "$BR      case ${message.name}.${message.values[index].toCamelCase()}: return \"$json\";"
        }

        return print
    }

}



/**
 * Prints all members of a class.
 */
internal class MessagePrinter(private val message: DartMessage): KlutterPrinter {

    override fun print() = """
        |
        |class ${message.name} {
        |  
        |${ConstructorPrinter(message).print()}
        |  
        |${FactoryPrinter(message).print()}
        |
        |${MemberPrinter(message.fields).print()}
        |
        |${SerializerPrinter(message.fields).print()}  
        |}
    """.trimMargin()

}

/**
 * Prints the class constructor
 */
internal class ConstructorPrinter(
    private val message: DartMessage
): KlutterPrinter {

    override fun print() =
        "" +
                "  ${message.name}({$BR" +
                "${message.fields.sortedBy { it.isOptional }.joinToString(BR){ printField(it) }}$BR" +
                "  });"

    private fun printField(field: DartField) =
        StringBuilder().also { sb ->
            sb.append("    ")

            if(!field.isOptional) {
                sb.append("required ")
            }

            sb.append("this.${field.name},")

        }.toString()

}

/**
 * Prints the factory method.
 */
internal class FactoryPrinter(
    private val message: DartMessage
): KlutterPrinter {

    override fun print() = "" +
            "factory ${message.name}.fromJson(dynamic json) {$BR" +
            "   return ${message.name} ($BR" +
            "${message.fields.joinToString(BR) { printField(it) }}$BR" +
            "   );$BR" +
            " }"


    private fun printField(field: DartField) =
        StringBuilder().also { sb ->

            val q = if(field.isOptional) "?" else ""

            sb.append("     ")
            sb.append("${field.name}: ")

            if(field.isList) {
                procesAsList(
                    sb = sb,
                    isNullable = field.isOptional,
                    field = field,
                )
            } else {
                val dataType = field.type
                if(field.isCustomType){
                    sb.append("$dataType.fromJson(json['${field.name}'])")
                } else sb.append("json['${field.name}']$q${getCastMethod(dataType)}")
            }
            sb.append(",")

        }.toString()

    private fun procesAsList(
        sb: StringBuilder,
        isNullable: Boolean,
        field: DartField,
    ) {

        if(isNullable) sb.append("json['${field.name}'] == null ? [] : ")

        val dataType = field.type
        sb.append("List<${dataType}>")
        sb.append(".from(json['${field.name}']")

        val q = if(isNullable) "?" else ""

        if(field.isCustomType){
            sb.append("$q.map((o) => ${dataType}.fromJson(o)))")
        } else sb.append("$q.map((o) => o${getCastMethod(dataType)}))")

    }
}

internal fun getCastMethod(dataType: String) = when(DartKotlinMap.toMap(dataType)) {
    DartKotlinMap.BOOLEAN -> ""
    DartKotlinMap.DOUBLE -> ".toDouble()"
    DartKotlinMap.INTEGER -> ".toInt()"
    DartKotlinMap.STRING -> ".toString()"
}

/**
 * Prints the fields of a class.
 */
internal class MemberPrinter(
    private val fields: List<DartField>
): KlutterPrinter {

    override fun print() = fields.sortedBy { it.isOptional }.joinToString(BR) {

        var datatype = it.type

        if(it.isList){
            datatype = "List<${it.type}>"
        }

        if(it.isOptional) {
            " $datatype? ${it.name};"
        } else " final $datatype ${it.name};"
    }

}

/**
 * Prints the toJson method.
 */
internal class SerializerPrinter(
    private val fields: List<DartField>
): KlutterPrinter {

    override fun print() =
        " Map<String, dynamic> toJson() {$BR" +
                "   return {$BR" +
                "|${fields.joinToString(",$BR") { "     '${it.name}': ${serializer(it)}" }}$BR" +
                "   };$BR" +
                " }"

    private fun serializer(field: DartField): String {
        val q = if(field.isOptional) "?" else ""

        var out = field.name

        if(field.isList) {
            out += if(field.isCustomType) {
                "$q.map((o) => o.toJson()).toList()"
            } else {
                "$q.toList()"
            }
        } else if(field.isCustomType) {
            out = "$out$q".postfixJson()
        }

        return out
    }
}
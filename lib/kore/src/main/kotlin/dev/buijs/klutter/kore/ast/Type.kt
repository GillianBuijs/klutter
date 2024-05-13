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
package dev.buijs.klutter.kore.ast

/**
 * A type that is nullable.
 *
 * Example: String?.
 */
interface Nullable

/**
 * A type that has type parameters.
 *
 * Example: List<Boolean>.
 */
interface Nested

/**
 * Parent for all types in the ast package.
 */
sealed class AbstractType {
    override fun toString(): String =
        this.javaClass.simpleName

    open val className: String =
        javaClass.simpleName

    open val packageName: String =
        javaClass.packageName

    val fqdn: String
        get() = "$packageName.$className"
}

/**
 * A user defined class.
 *
 * Example:
 *
 * ```
 * package com.example
 *
 * class Response(
 *    val foo: Foo
 * )
 * ```
 *
 * is CustomType:
 * - name: "Response"
 * - package: "com.example"
 * - members: []
 *
 */
open class CustomType(
    override val className: String,
    override val packageName: String,
    val members: List<TypeMember> = emptyList()
): AbstractType() {

    open fun copy(fields: List<TypeMember>) =
        CustomType(
            className = className,
            packageName = packageName,
            members = fields
        )

    override fun toString() = print()

    override fun equals(other: Any?): Boolean = when {
        other !is CustomType -> false
        other.className != className -> false
        other.packageName != packageName -> false
        other.members.size != members.size -> false
        !other.members.containsAll(members) -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = className.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + members.hashCode()
        return result
    }

}

/**
 * A user defined enum class.
 *
 * Example:
 *
 * ```
 * package com.example
 *
 * enum class GreetingType(
 *    GREETING, HUG, HIGH_FIVE
 * )
 * ```
 *
 * is EnumType:
 * - name: "GreetingType"
 * - package: "com.example"
 * - values: [GREETING, HUG, HIGH_FIVE]
 *
 */
open class EnumType(
    override val className: String,
    override val packageName: String ,
    val values: List<String> = emptyList(),
    val valuesJSON: List<String> = emptyList(),
): AbstractType() {

    open fun copy(values: List<String>) =
        EnumType(
            className = className,
            packageName = packageName,
            values = values)

    override fun toString() = print()

}

data class UndeterminedType(
    override val className: String,
): AbstractType()

/**
 * A nullable variant of [CustomType].
 */
class NullableCustomType(
    className: String,
    packageName: String,
    fields: List<TypeMember> = emptyList()
): CustomType(
    className = className,
    packageName = packageName,
    members = fields
), Nullable {

    override fun copy(fields: List<TypeMember>) =
        NullableCustomType(
            className = className,
            packageName = packageName,
            fields = fields
        )

    override fun toString() = print()

}

/**
 * A standard Kotlin/Dart type, as defined in [StandardTypeMap].
 */
sealed class StandardType(
    private val type: StandardTypeMap,
): AbstractType() {

    val kotlinType
        get() = type.kotlinType

    val dartType
        get() = type.dartType

    override val className: String
        get() = kotlinType
}

/**
 * Representation of a class field member.
 */
data class TypeMember(
    val name: String,
    val type: AbstractType,
)

private fun CustomType.print() = this.javaClass.simpleName +
    "(className = $className, " +
    "packageName = $packageName, " +
    "fields = ${members.map { "${it.name}: ${it.type.typeSimplename()}" }}, " +
    "nullable = ${this.javaClass.interfaces.map { i -> i.simpleName }.contains(object: Nullable {}.javaClass.simpleName)})"

private fun EnumType.print() = this.javaClass.simpleName +
        "(className = $className, " +
        "packageName = $packageName, " +
        "values = $values, " +
        "valuesJSON = $valuesJSON, " +
        "nullable = ${this.javaClass.interfaces.map { i -> i.simpleName }.contains(object: Nullable {}.javaClass.simpleName)})"


fun AbstractType?.typeSimplename(asKotlinType: Boolean = true): String {
    if(this == null) return "#null#"

    val nullable =
        if(this is Nullable) "?" else ""

    return when (this) {

        is CustomType ->
            this.className + nullable

        is EnumType ->
            this.className + nullable

        is UndeterminedType ->
            this.className + nullable

        is MapType -> {
            val type =
                if(asKotlinType) kotlinType else dartType

            val keyType =
                this.key.typeSimplename(asKotlinType)

            val valueType =
                this.value.typeSimplename(asKotlinType)

            "$type<$keyType,$valueType>$nullable"
        }

        is ListType -> {
            val type =
                if(asKotlinType) kotlinType else dartType

            val childType =
                this.child.typeSimplename(asKotlinType)

            "$type<$childType>$nullable"
        }

        is StandardType ->
            (if(asKotlinType) kotlinType else dartType) + nullable
    }

}
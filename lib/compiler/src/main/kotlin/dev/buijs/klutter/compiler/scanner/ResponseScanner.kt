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
package dev.buijs.klutter.compiler.scanner

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import dev.buijs.klutter.compiler.wrapper.*
import dev.buijs.klutter.kore.ast.*
import dev.buijs.klutter.kore.common.Either
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File


/**
 * FQDN for classes annotated with @Response.
 */
private const val RESPONSE_ANNOTATION =
    "dev.buijs.klutter.annotations.Response"

/**
 * Get all classes with @Response annotation and convert them to [KCController].
 */
private fun getSymbolsWithResponseAnnotation(resolver: Resolver): List<KSClassDeclaration> =
    resolver.getSymbolsWithAnnotation(RESPONSE_ANNOTATION)
        .filterIsInstance<KSClassDeclaration>()
        .toList()

/**
 * Find all class annotated with @Response.
 * </br>
 * Preliminary checks are done which result in either a [SquintMessageSource] instance
 * or an error message describing the issue.
 * </br>
 * Full validation is done by [validateResponses()] which takes the
 * full context (other Responses and/or Controllers) into consideration.
 */
@JvmOverloads
internal fun scanForResponses(
    outputFolder: File,
    resolver: Resolver,
    isProtobufEnabled: Boolean,
    scanner: (resolver: Resolver) -> List<KCResponse> = {
        getSymbolsWithResponseAnnotation(it).map { clazz -> clazz.toKCResponse() } },
): List<Either<String, SquintMessageSource>> =
    scanner.invoke(resolver)
        .map { it.toSquintMessageSourceOrFail(isProtobufEnabled) }
        .mapIndexed { index, data -> data.writeOutput(outputFolder, index) }
        .toList()

@JvmOverloads
internal fun scanForResponsesProtobuf(
    outputFolder: File,
    resolver: Resolver,
    scanner: (resolver: Resolver) -> List<KSClassDeclaration> = { getSymbolsWithResponseAnnotation(it) },
): List<Either<String, String>> =
    scanner.invoke(resolver)
        .map { it.toSchemaSourceOrFail() }
        .mapIndexed { index, data -> data.writeResponseFQDN(outputFolder, index) }
        .toList()

private fun KCResponse.toSquintMessageSourceOrFail(isProtobufEnabled: Boolean)
: Either<String, SquintMessageSource> = when(this) {
    is KCEnumeration -> enumeration()
    is KCMessage -> message(isProtobufEnabled)
}

private fun KSClassDeclaration.toSchemaSourceOrFail()
: Either<String, String> {

    val annotationNames = annotations
        .map{ it.shortName.getShortName() }
        .toList()

    val isSerializableAnnotated =
        annotationNames.contains("Serializable")

    val className = "$this"
    val packageName = packageName.asString()

    if(!isSerializableAnnotated)
        return Either.nok("Class is missing @Serializable annotation: $packageName.$className")

    if(superTypes.map { it.toString() }.toList().contains("JSON"))
        return Either.nok("Class extends JSON which is incompatible with Protobuf (remove JSON interface to fix): $packageName.$className")

    return Either.ok("$packageName.$className")
}

private fun KCMessage.message(isProtobufEnabled: Boolean): Either<String, SquintMessageSource> {
    if(!isSerializableAnnotated)
        return missingSerializableAnnotation()

    if(!extendsJSON && !isProtobufEnabled)
        return doesNotExtendKlutterJSON()

    val validTypeMembers = typeMembers
        .filterIsInstance<ValidTypeMember>()

    val invalidTypeMembers = typeMembers
        .filterIsInstance<InvalidTypeMember>()
        .map { it.data }

    if(invalidTypeMembers.isNotEmpty())
        return invalidTypeMembers(invalidTypeMembers)

    if(!hasOneConstructor)
        return responseHasTooManyConstructors()

    if(validTypeMembers.isEmpty())
        return emptyConstructor()

    val type = CustomType(
        className = className,
        packageName = packageName,
        members = validTypeMembers.map { it.data })

    val squintType = SquintCustomType(
        className = className,
        members = validTypeMembers.map {
            SquintCustomTypeMember(
                name = it.data.name,
                type = it.data.type.let { t -> t.typeSimplename(asKotlinType = false) },
                nullable = it.data.type is Nullable) })

    return ValidSquintType(SquintMessageSource(type = type, squintType = squintType))
}

private fun KCEnumeration.enumeration(): Either<String, SquintMessageSource> {
    if(!isSerializableAnnotated)
        return missingSerializableAnnotation()

    val enumType = EnumType(
        className = className,
        packageName = packageName,
        values = values,
        valuesJSON = valuesJSON.ifEmpty { values })

    val squintType = SquintEnumType(
        className = className,
        values = values,
        valuesJSON = valuesJSON.ifEmpty { values })

    return Either.ok(SquintMessageSource(type = enumType, squintType = squintType))
}
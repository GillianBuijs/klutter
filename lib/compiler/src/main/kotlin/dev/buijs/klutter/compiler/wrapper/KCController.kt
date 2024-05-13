package dev.buijs.klutter.compiler.wrapper

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.buijs.klutter.compiler.scanner.InvalidEvent
import dev.buijs.klutter.compiler.scanner.ValidEvent
import dev.buijs.klutter.compiler.scanner.getEvents
import dev.buijs.klutter.kore.ast.AbstractType
import dev.buijs.klutter.kore.ast.Method
import dev.buijs.klutter.kore.common.ExcludeFromJacocoGeneratedReport

/**
 * Wrapper for KSP KSClassDeclaration to encapsulate KSP specific classes.
 */
internal data class KCController(
    val hasOneConstructor: Boolean,
    val firstConstructorHasNoParameters: Boolean,
    val events: List<Method>,
    val eventErrors: List<String>,
    val controllerType: String,
    val isBroadcastController: Boolean,
    val broadcastTypeParameterOrBlank: String,
    val className: String,
    val packageName: String)

@ExcludeFromJacocoGeneratedReport(
    reason = "Requires way too much mocking/stubbing. Is test through module test-ksp.")
internal fun KSClassDeclaration.toKotlinClassWrapper(responses: Set<AbstractType>): KCController {

    val constructors: List<KSFunctionDeclaration> = getConstructors().toList()

    val controllerType = annotations
        .filter { it.shortName.asString() == "Controller" }
        .filter { it.arguments.isNotEmpty() }
        .map { it.arguments.firstOrNull { arg -> arg.name?.getShortName() == "type" } }
        .filterNotNull()
        .map { it.value.toString() }
        .firstOrNull { it.startsWith("dev.buijs.klutter.annotations.ControllerType.") }
        ?.substringAfterLast("dev.buijs.klutter.annotations.ControllerType.")
        ?: "Default"

    val publisherOrNull =
        superTypes.firstOrNull { it.toString() == "Publisher" }

    val events = getAllFunctions().map { it.toKAWrapper(responses) }.getEvents()

    return KCController(
        hasOneConstructor = constructors.size == 1,
        firstConstructorHasNoParameters = constructors.firstOrNull()?.parameters?.isEmpty() ?: false,
        events = events.filterIsInstance<ValidEvent>().map { it.data }.toList(),
        eventErrors = events.filterIsInstance<InvalidEvent>().map { it.data }.toList(),
        controllerType = controllerType,
        isBroadcastController = publisherOrNull != null,
        broadcastTypeParameterOrBlank = publisherOrNull?.resolve()?.arguments?.first()?.type?.toString() ?: "",
        className ="$this",
        packageName = packageName.asString())
}

@ExcludeFromJacocoGeneratedReport(
    reason = "Requires way too much mocking/stubbing. Is test through module test-ksp.")
internal fun KSClassDeclaration.toKotlinClassWrapper(): KCController {

    val constructors: List<KSFunctionDeclaration> = getConstructors().toList()

    val controllerType = annotations
        .filter { it.shortName.asString() == "Controller" }
        .filter { it.arguments.isNotEmpty() }
        .map { it.arguments.firstOrNull { arg -> arg.name?.getShortName() == "type" } }
        .filterNotNull()
        .map { it.value.toString() }
        .firstOrNull { it.startsWith("dev.buijs.klutter.annotations.ControllerType.") }
        ?.substringAfterLast("dev.buijs.klutter.annotations.ControllerType.")
        ?: "Default"

    val publisherOrNull =
        superTypes.firstOrNull { it.toString() == "Publisher" }

    return KCController(
        hasOneConstructor = constructors.size == 1,
        firstConstructorHasNoParameters = constructors.firstOrNull()?.parameters?.isEmpty() ?: false,
        events = emptyList(),
        eventErrors =  emptyList(),
        controllerType = controllerType,
        isBroadcastController = publisherOrNull != null,
        broadcastTypeParameterOrBlank = publisherOrNull?.resolve()?.arguments?.first()?.type?.toString() ?: "",
        className ="$this",
        packageName = packageName.asString())
}
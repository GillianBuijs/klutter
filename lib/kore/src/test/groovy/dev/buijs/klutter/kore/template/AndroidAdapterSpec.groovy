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

package dev.buijs.klutter.kore.template

import dev.buijs.klutter.kore.ast.AbstractType
import dev.buijs.klutter.kore.ast.BooleanType
import dev.buijs.klutter.kore.ast.Controller
import dev.buijs.klutter.kore.ast.CustomType
import dev.buijs.klutter.kore.ast.IntType
import dev.buijs.klutter.kore.ast.NullableCustomType
import dev.buijs.klutter.kore.ast.NullableStringType
import dev.buijs.klutter.kore.ast.RequestScopedBroadcastController
import dev.buijs.klutter.kore.ast.RequestScopedSimpleController
import dev.buijs.klutter.kore.ast.SingletonBroadcastController
import dev.buijs.klutter.kore.ast.SingletonSimpleController
import dev.buijs.klutter.kore.ast.StringType
import dev.buijs.klutter.kore.ast.TypeMember
import dev.buijs.klutter.kore.ast.UnitType
import dev.buijs.klutter.kore.ast.Method
import dev.buijs.klutter.kore.templates.AndroidAdapter
import dev.buijs.klutter.kore.test.TestUtil
import spock.lang.Specification

class AndroidAdapterSpec extends Specification {

    def "AndroidAdapter should create a valid Kotlin class"() {
        given:
        def packageName = "dev.buijs.platform.controller"
        def sut = createSut([
                new RequestScopedSimpleController(packageName, "MySimpleContollerImpl", [
                        new Method(
                                "sayHi",
                                "dev.buijs.platform.contoller.MySimpleControllerImpl",
                                "sayHiPlease",
                                false,
                                new StringType(),
                                new StringType(),
                                "message"
                        )
                ]),
                new SingletonBroadcastController(packageName, "MyEmittingController", [
                        new Method(
                                "start",
                                "dev.buijs.platform.contoller.MyEmittingController",
                                "startBroadcast",
                                false,
                                new BooleanType(),
                                null,
                                null
                        )
                ],
                        new CustomType("MyResponse", "dev.buijs.platform.controller",
                                [new TypeMember("foo", new StringType())])
        )].toSet())

        expect:
        TestUtil.verify(sut.print(), classBody)

        where:
        classBody = """package super_plugin

import android.app.Activity
import android.content.Context
import dev.buijs.klutter.*
import dev.buijs.klutter.EventChannelFacade
import dev.buijs.klutter.MethodChannelFacade
import dev.buijs.klutter.registerEventSink
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.EventChannel.StreamHandler
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import dev.buijs.platform.controller.*

private val methodChannelNames = setOf(
        "dev.company.plugins/channels/controller",
)

private val eventChannelNames = setOf(
        "dev.buijs.plugins/streams/controller",
)

private val myEmittingController: MyEmittingController = MyEmittingController()

class SuperPlugin: FlutterPlugin, MethodCallHandler, StreamHandler, ActivityAware {

    private val mainScope = CoroutineScope(Dispatchers.Main)
    private lateinit var activity: Activity
    private lateinit var mcFacade: MethodChannelFacade
    private lateinit var ecFacade: EventChannelFacade

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        this.mcFacade = MethodChannelFacade(this, binding.binaryMessenger, methodChannelNames)
        this.ecFacade = EventChannelFacade(this, binding.binaryMessenger, eventChannelNames)
    }        

    override fun onMethodCall(call: MethodCall, result: Result) {
        mainScope.launch {
            onEvent(
                event = call.method,
                data = call.arguments,
                result = result
            )
        }
    }


    override fun onListen(arguments: Any?, eventSink: EventChannel.EventSink) {
        when (arguments) {
            "my_emitting_controller" ->
                 registerEventSink(myEmittingController, eventSink)
            else -> {
                eventSink.error("Unknown topic", "\$arguments", null)
            }
        }
    }

    override fun onCancel(arguments: Any?) {
        myEmittingController.cancel()
    }

    override fun onDetachedFromEngine(
        binding: FlutterPlugin.FlutterPluginBinding
    ) {
    }

    override fun onAttachedToActivity(
        binding: ActivityPluginBinding
    ) {
        activity = binding.activity
    }

    override fun onReattachedToActivityForConfigChanges(
        binding: ActivityPluginBinding
    ) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        // nothing
    }

    override fun onDetachedFromActivityForConfigChanges() {
        // nothing
    }

    suspend fun <T> onEvent(event: String, data: T?, result: Result) { 
           try {
               when(event) {
                "sayHi" ->
                    result.success(MySimpleContollerImpl().sayHiPlease(data as String))
                "start" ->
                    result.success(myEmittingController.startBroadcast())
                   else -> result.notImplemented()
               }
           } catch(e: Exception) {
               result.error("10101", e.message, e.stackTrace)
           }
       }
}
"""
    }

    def "Verify broadcast - singleton - standard-type"() {
        expect:
        with(createSut(Set.of(createSingletonBroadcastController([],stringType)))) {
            with(it.print()) { content ->
                content.contains("registerEventSink(mySingletonBroadcastController, eventSink)")
            }
        }
    }

    def "Verify broadcast - singleton - nullable standard-type"() {
        expect:
        with(createSut(Set.of(createSingletonBroadcastController([],nullableStringType)))) {
            with(it.print()) { content ->
                content.contains("registerEventSink(mySingletonBroadcastController, eventSink)")
                content.contains("mySingletonBroadcastController.cancel()")
            }
        }
    }

    def "Verify broadcast - singleton - custom-type"() {
        expect:
        with(createSut(Set.of(createSingletonBroadcastController([],myCustomType)))) {
            with(it.print()) { content ->
                content.contains("registerEventSink(mySingletonBroadcastController, eventSink)")
                content.contains("mySingletonBroadcastController.cancel()")
            }
        }
    }

    def "Verify broadcast - request-scoped - nullable custom-type"() {
        expect:
        with(createSut(Set.of(createRequestScopedBroadcastController([],nullableMyCustomType)))) {
            with(it.print()) { content ->
                content.contains("registerEventSink(MyRequestScopedBroadcastController(), eventSink)")
                content.contains("MyRequestScopedBroadcastController().cancel()")
            }
        }
    }

    def "Verify simple - singleton - standard-type not-null request, standard-type not-null response"() {
        expect:
        with(createSut(Set.of(createSingletonController([createMethod(stringType, stringType)])))) {
            with(it.print()) { content ->
                content.contains('''"sayHi" ->''')
                content.contains("result.success(mySingletonController.sayHiPlease(data as String))")
            }
        }
    }

    def "Verify simple - singleton - standard-type nullable request, standard-type not-null response"() {
        expect:
        with(createSut(Set.of(createSingletonController([createMethod(nullableStringType, stringType)])))) {
            with(it.print()) { content ->
                content.contains('''"sayHi" ->''')
                content.contains("result.success(mySingletonController.sayHiPlease(data as String?))")
            }
        }
    }

    def "Verify simple - singleton - standard-type nullable request, standard-type nullable response"() {
        expect:
        with(createSut(Set.of(createSingletonController([createMethod(nullableStringType, nullableStringType)])))) {
            with(it.print()) { content ->
                content.contains('''"sayHi" ->''')
                content.contains("result.success(mySingletonController.sayHiPlease(data as String?))")
            }
        }
    }

    def "Verify simple - request-scoped - no request, custom-type not-null response"() {
        expect:
        with(createSut(Set.of(createRequestScopedController([createMethod(null, myCustomType)])))) {
            with(it.print()) { content ->
                content.contains('''"sayHi" ->''')
                content.contains("result.success(MyRequestScopedController().sayHiPlease().encode())")
            }
        }
    }

    def "Verify simple - request-scoped - custom-type not-null request, custom-type not-null response"() {
        expect:
        with(createSut(Set.of(createRequestScopedController([createMethod(myCustomType, myCustomType)])))) {
            with(it.print()) { content ->
                content.contains('''"sayHi" ->''')
                content.contains("val kJson: MyCustomType? = data.decode() as MyCustomType?")
                content.contains("if(kJson != null) {")
                content.contains("result.success(MyRequestScopedController().sayHiPlease(kJson).encode())")
            }
        }
    }

    def "Verify simple - request-scoped - custom-type nullable request, custom-type not-null response"() {
        expect:
        with(createSut(Set.of(createRequestScopedController([createMethod(nullableMyCustomType, myCustomType)])))) {
            with(it.print()) { content ->
                content.contains('''"sayHi" ->''')
                content.contains("result.success(MyRequestScopedController().sayHiPlease(kJson).encode())")
            }
        }
    }

    def "Verify simple - request-scoped - custom-type nullable request, custom-type nullable response"() {
        expect:
        with(createSut(Set.of(createRequestScopedController([createMethod(nullableMyCustomType, nullableMyCustomType)])))) {
            with(it.print()) { content ->
                content.contains('''"sayHi" ->''')
                content.contains("result.success(MyRequestScopedController().sayHiPlease(kJson)?.encode())")
            }
        }
    }

    def "Verify emptySet is used when there are no method channels"() {
        given:
        def adapter = new AndroidAdapter("", "", false, Set.of(), Set.of(), Set.of())

        expect:
        with(adapter.print()) { content ->
            content.contains("private val methodChannelNames = emptySet<String>()")
            content.contains("private val eventChannelNames = emptySet<String>()")
        }
    }

    def "Verify request data is returned when invoked method has void return type"() {
        given:
        def methodReturningVoid = createMethod(new StringType(), new UnitType())
        def controllerWithMethod = new SingletonSimpleController("foo.bar", "MySingleton", [methodReturningVoid])
        def adapter = new AndroidAdapter("", "", false,Set.of("foo/bar"), Set.of(), Set.of(controllerWithMethod))

        expect:
        with(adapter.print()) { content ->
            content.contains("mySingleton.sayHiPlease(data as String)")
            content.contains("result.success(data as String)")
            !content.contains("result.success(mySingleton.sayHiPlease(data as String))")
        }
    }

    def static stringType = new StringType()

    def static nullableStringType = new NullableStringType()

    def static integerType = new IntType()

    def static myCustomType = new CustomType(
            "MyCustomType",
            "dev.buijs.platform.models",
            [new TypeMember("foo", integerType)]
    )

    def static nullableMyCustomType = new NullableCustomType(
            "MyCustomType",
            "dev.buijs.platform.models",
            [new TypeMember("foo", integerType)]
    )

    def static createMethod(
            AbstractType request,
            AbstractType response
    ) {
        new Method(
                "sayHi",
                "dev.buijs.platform.controller.MySimpleControllerImpl",
                "sayHiPlease",
                false,
                response,
                request,
                request == null ? null : "data"
        )
    }

    def static createRequestScopedController(List<Method> methods) {
        new RequestScopedSimpleController(
                "dev.buijs.platform.controller",
                "MyRequestScopedController",
                methods
        )
    }

    def static createRequestScopedBroadcastController(List<Method> methods, AbstractType response) {
        new RequestScopedBroadcastController(
                "dev.buijs.platform.controller",
                "MyRequestScopedBroadcastController",
                methods,
                response
        )
    }

    def static createSingletonController(List<Method> methods) {
        new SingletonSimpleController(
                "dev.buijs.platform.controller",
                "MySingletonController",
                methods
        )
    }

    def static createSingletonBroadcastController(List<Method> methods, AbstractType response) {
        new SingletonBroadcastController(
                "dev.buijs.platform.controller",
                "MySingletonBroadcastController",
                methods,
                response
        )
    }

    def static createSut(
            Set<Controller> controllers
    ) {
        def packageName = "super_plugin"
        def pluginName = "SuperPlugin"
        def channels = ["dev.company.plugins/channels/controller"].toSet()
        def streams = ["dev.buijs.plugins/streams/controller"].toSet()
        new AndroidAdapter(
                packageName,
                pluginName,
                false,
                channels,
                streams,
                controllers
        )
    }

}
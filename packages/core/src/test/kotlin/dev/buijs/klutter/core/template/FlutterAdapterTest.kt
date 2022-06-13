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

package dev.buijs.klutter.core.template

import dev.buijs.klutter.core.Method
import dev.buijs.klutter.core.fooBarMethods
import dev.buijs.klutter.core.templates.FlutterAdapter
import dev.buijs.klutter.core.verify
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec

class FlutterAdapterTest: WordSpec({

    "Using the FlutterAdapter" should {

        "fallback to defaults" {
            //given:
            val methods: List<Method> = fooBarMethods()
            val adapter = FlutterAdapter(
                methods = methods,
                messages = emptyList(),
                enumerations = emptyList(),
            )

            //expect:
            verify(adapter, expected1) shouldBe true
        }

        "override defaults" {
            //given:
            val methods: List<Method> = fooBarMethods()
            val adapter = FlutterAdapter(
                pluginClassName = "MyPlugin",
                methodChannelName = "channeling",
                methods = methods,
                messages = emptyList(),
                enumerations = emptyList(),
            )

            //expect:
            verify(adapter, expected2) shouldBe true
        }

    }
})

private const val expected1 = """import 'dart:async';
    import 'package:flutter/services.dart';

    /// Autogenerated by Klutter Framework.
    ///
    /// Do net edit directly, but recommended to store in VCS.
    ///
    /// Adapter class which handles communication with the KMP library.
    class Adapter {
      static const MethodChannel _channel = MethodChannel('KLUTTER');

      static Future<AdapterResponse<String>> get doFooBar async {
        try {
          final json = await _channel.invokeMethod('doFooBar');
          return AdapterResponse.success(json.toString());
        } catch (e) {
          return AdapterResponse.failure(
            e is Error ? Exception(e.stackTrace) : e as Exception
          );
        }
      }

      static Future<AdapterResponse<int>> get notDoFooBar async {
        try {
          final json = await _channel.invokeMethod('notDoFooBar');
          return AdapterResponse.success(json.toInt());
        } catch (e) {
          return AdapterResponse.failure(
            e is Error ? Exception(e.stackTrace) : e as Exception
          );
        }
      }

      static Future<AdapterResponse<List<Complex>>> get complexityGetter async {
        try {
          final response = await _channel.invokeMethod('complexityGetter');
          final json = jsonDecode(response);
          return AdapterResponse.success(List<Complex>.from(json.map((o) => Complex.fromJson(o))));
        } catch (e) {
          return AdapterResponse.failure(
            e is Error ? Exception(e.stackTrace) : e as Exception
          );
        }
      }

    }

    /// Autogenerated by Klutter Framework.
    ///
    /// Do net edit directly, but recommended to store in VCS.
    ///
    /// Wraps an [exception] if calling the platform method has failed to be logged by the consumer.
    /// Or wraps an [object] of type T when platform method has returned a response and
    /// deserialization was successful.
    class AdapterResponse<T> {

      AdapterResponse(this._object, this._exception);

      factory AdapterResponse.success(T t) => AdapterResponse(t, null);

      factory AdapterResponse.failure(Exception e) => AdapterResponse(null, e);

      ///The actual object to returned
      T? _object;
        set object(T object) => _object = object;
        T get object => _object!;

      ///Exception which occurred when calling a platform method failed.
      Exception? _exception;
        set exception(Exception e) => _exception = e;
        Exception get exception => _exception!;

      bool isSuccess() {
        return _object != null;
      }

    }
   """


private const val expected2 = """import 'dart:async';
    import 'package:flutter/services.dart';

    /// Autogenerated by Klutter Framework.
    ///
    /// Do net edit directly, but recommended to store in VCS.
    ///
    /// Adapter class which handles communication with the KMP library.
    class MyPlugin {
      static const MethodChannel _channel = MethodChannel('channeling');

      static Future<AdapterResponse<String>> get doFooBar async {
        try {
          final json = await _channel.invokeMethod('doFooBar');
          return AdapterResponse.success(json.toString());
        } catch (e) {
          return AdapterResponse.failure(
            e is Error ? Exception(e.stackTrace) : e as Exception
          );
        }
      }

      static Future<AdapterResponse<int>> get notDoFooBar async {
        try {
          final json = await _channel.invokeMethod('notDoFooBar');
          return AdapterResponse.success(json.toInt());
        } catch (e) {
          return AdapterResponse.failure(
            e is Error ? Exception(e.stackTrace) : e as Exception
          );
        }
      }

      static Future<AdapterResponse<List<Complex>>> get complexityGetter async {
        try {
          final response = await _channel.invokeMethod('complexityGetter');
          final json = jsonDecode(response);
          return AdapterResponse.success(List<Complex>.from(json.map((o) => Complex.fromJson(o))));
        } catch (e) {
          return AdapterResponse.failure(
            e is Error ? Exception(e.stackTrace) : e as Exception
          );
        }
      }

    }

    /// Autogenerated by Klutter Framework.
    ///
    /// Do net edit directly, but recommended to store in VCS.
    ///
    /// Wraps an [exception] if calling the platform method has failed to be logged by the consumer.
    /// Or wraps an [object] of type T when platform method has returned a response and
    /// deserialization was successful.
    class AdapterResponse<T> {

      AdapterResponse(this._object, this._exception);

      factory AdapterResponse.success(T t) => AdapterResponse(t, null);

      factory AdapterResponse.failure(Exception e) => AdapterResponse(null, e);

      ///The actual object to returned
      T? _object;
        set object(T object) => _object = object;
        T get object => _object!;

      ///Exception which occurred when calling a platform method failed.
      Exception? _exception;
        set exception(Exception e) => _exception = e;
        Exception get exception => _exception!;

      bool isSuccess() {
        return _object != null;
      }

    }
   """



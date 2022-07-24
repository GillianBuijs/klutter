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

import dev.buijs.klutter.core.templates.KomposeFlutterAdapter
import dev.buijs.klutter.core.verify
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec

class KomposeFlutterAdapterTest: WordSpec({

    "Using the KomposeFlutterAdapter" should {

        "fallback to defaults" {
            //given:
            val adapter = KomposeFlutterAdapter(
                messages = emptyList(),
                enumerations = emptyList(),
            )

            //expect:
            verify(adapter, expected1) shouldBe true
        }

        "override defaults" {
            //given:
            val adapter = KomposeFlutterAdapter(
                pluginClassName = "MyPlugin",
                methodChannelName = "channeling",
                messages = emptyList(),
                enumerations = emptyList(),
            )

            //expect:
            verify(adapter, expected2) shouldBe true
        }

    }
})

private const val expected1 = """import 'dart:async';
import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:klutter/klutter.dart';

/// Autogenerated by Klutter Framework. 
/// 
/// Do net edit directly, but recommended to store in VCS.
/// 
/// Adapter class which handles communication with the KMP library.
class Adapter {
  static const MethodChannel _channel = MethodChannel('KLUTTER');
  
    static Future<dynamic> fireEvent({
       required String widget,
       required String event,
       required String data,
       required String controller,
   }) async {
       return await _channel.invokeMethod("kompose_event_trigger",
        {
          "widget" : widget,
          "event" : event,
          "controller": controller,
          "data": data,
        }
    );
  }
  
    static Future<dynamic> initController({
       required String widget,
       required String event,
       required String data,
       required String controller,
   }) async {
       return await _channel.invokeMethod("kompose_init_controller",
        {
          "widget" : widget,
          "event" : event,
          "controller": controller,
          "data": data,
        }
    );
  }
  
  static Future<void> disposeController({
    required String widget,
    required String event,
    required String data,
    required String controller,
  }) async {
    await _channel.invokeMethod("kompose_dispose_controller",
        {
          "widget" : widget,
          "event" : event,
          "controller": controller,
          "data": data,
        }
    );
  }
}"""


private const val expected2 = """import 'dart:async';
import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:klutter/klutter.dart';

/// Autogenerated by Klutter Framework. 
/// 
/// Do net edit directly, but recommended to store in VCS.
/// 
/// Adapter class which handles communication with the KMP library.
class MyPlugin {
  static const MethodChannel _channel = MethodChannel('channeling');
  
    static Future<dynamic> fireEvent({
       required String widget,
       required String event,
       required String data,
       required String controller,
   }) async {
       return await _channel.invokeMethod("kompose_event_trigger",
        {
          "widget" : widget,
          "event" : event,
          "controller": controller,
          "data": data,
        }
    );
  }
  
    static Future<dynamic> initController({
       required String widget,
       required String event,
       required String data,
       required String controller,
   }) async {
       return await _channel.invokeMethod("kompose_init_controller",
        {
          "widget" : widget,
          "event" : event,
          "controller": controller,
          "data": data,
        }
    );
  }
  
  static Future<void> disposeController({
    required String widget,
    required String event,
    required String data,
    required String controller,
  }) async {
    await _channel.invokeMethod("kompose_dispose_controller",
        {
          "widget" : widget,
          "event" : event,
          "controller": controller,
          "data": data,
        }
    );
  }
  
}"""



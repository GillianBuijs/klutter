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

import dev.buijs.klutter.core.CoreTestUtil
import dev.buijs.klutter.core.shared.DartEnum
import dev.buijs.klutter.core.shared.DartMessage
import dev.buijs.klutter.core.TestData
import dev.buijs.klutter.core.templates.FlutterAdapter
import spock.lang.Specification

class FlutterAdapterSpec extends Specification {

    def "FlutterAdapter should create a valid Dart class"() {
        given:
        def methods = TestData.complexityMethods

        and: "The printer as SUT"
        def adapter = new FlutterAdapter("Adapter", "KLUTTER", methods, [], [])

        expect:
        CoreTestUtil.verify(adapter, expected1)
    }

    def "FlutterAdapter should convert enumerations and messages"() {
        given:
        def methods = TestData.fooBarMethods

        and: "The printer as SUT"
        def adapter = new FlutterAdapter(
                "Adapter",
                "KLUTTER",
                methods,
                [
                        TestData.emptyMessageFoo,
                        new DartMessage("FooMessage", [
                                TestData.fieldOptionalFoo,
                                TestData.fieldOptionalFooList,
                                TestData.fieldOptionalString,
                                TestData.fieldOptionalStringList,
                                TestData.fieldRequiredFoo,
                                TestData.fieldRequiredFooList,
                                TestData.fieldRequiredString,
                                TestData.fieldRequiredStringList,
                        ])
                ],
                [
                        new DartEnum("FooEnum", ["FOO1", "FOO2", "FOO3"], []),
                        new DartEnum("BarEnum", ["BAR1", "BAR2", "BAR3"], ["bar", "barry", "bardon"]),
                ],
        )

        expect:
        CoreTestUtil.verify(adapter, expected2)
    }

    private static def expected1 = """import 'dart:async';
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
  
            
  static Future<AdapterResponse<String>> doFooBar(State caller, {
    void Function(String)? onSuccess,
    void Function(Exception)? onFailure,
    void Function()? onNullValue,
    void Function(AdapterResponse<String>)? onComplete,
  }) async {

    try {
    final json = await _channel.invokeMethod('doFooBar');
      final value = json.toString();
      final AdapterResponse<String> response = 
          AdapterResponse.success(value);

      if(caller.mounted) {
        onComplete?.call(response);      
          onSuccess?.call(value);
      }

      return response;
      
    } catch (e) {
      
      final exception = e is Error 
          ? Exception(e.stackTrace) 
          : e as Exception;
      
      final AdapterResponse<String> response = 
          AdapterResponse.failure(exception);

      if(caller.mounted) {
        onComplete?.call(response);
        onFailure?.call(exception);
      }

      return response;
    }
  }

  static Future<AdapterResponse<int>> notDoFooBar(State caller, {
    void Function(int)? onSuccess,
    void Function(Exception)? onFailure,
    void Function()? onNullValue,
    void Function(AdapterResponse<int>)? onComplete,
  }) async {

    try {
    final json = await _channel.invokeMethod('notDoFooBar');
      final value = json.toInt();
      final AdapterResponse<int> response = 
          AdapterResponse.success(value);

      if(caller.mounted) {
        onComplete?.call(response);      
          onSuccess?.call(value);
      }

      return response;
      
    } catch (e) {
      
      final exception = e is Error 
          ? Exception(e.stackTrace) 
          : e as Exception;
      
      final AdapterResponse<int> response = 
          AdapterResponse.failure(exception);

      if(caller.mounted) {
        onComplete?.call(response);
        onFailure?.call(exception);
      }

      return response;
    }
  }

  static Future<AdapterResponse<List<Complex>>> complexityGetter(State caller, {
    void Function(List<Complex>)? onSuccess,
    void Function(Exception)? onFailure,
    void Function()? onNullValue,
    void Function(AdapterResponse<List<Complex>>)? onComplete,
  }) async {

    try {
         
               final jsonResponse = await _channel.invokeMethod('complexityGetter');
      final json = jsonDecode(jsonResponse);
      final value = List<Complex>.from(json.map((o) => Complex.fromJson(o)));
      final AdapterResponse<List<Complex>> response = 
          AdapterResponse.success(value);

      if(caller.mounted) {
        onComplete?.call(response);      
          onSuccess?.call(value);
      }

      return response;
      
    } catch (e) {
      
      final exception = e is Error 
          ? Exception(e.stackTrace) 
          : e as Exception;
      
      final AdapterResponse<List<Complex>> response = 
          AdapterResponse.failure(exception);

      if(caller.mounted) {
        onComplete?.call(response);
        onFailure?.call(exception);
      }

      return response;
    }
  }

}
  """

    private static def expected2 = '''import 'dart:async';
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
  
            
  static Future<AdapterResponse<String>> doFooBar(State caller, {
    void Function(String)? onSuccess,
    void Function(Exception)? onFailure,
    void Function()? onNullValue,
    void Function(AdapterResponse<String>)? onComplete,
  }) async {

    try {
    final json = await _channel.invokeMethod('doFooBar');
      final value = json.toString();
      final AdapterResponse<String> response = 
          AdapterResponse.success(value);

      if(caller.mounted) {
        onComplete?.call(response);      
          onSuccess?.call(value);
      }

      return response;
      
    } catch (e) {
      
      final exception = e is Error 
          ? Exception(e.stackTrace) 
          : e as Exception;
      
      final AdapterResponse<String> response = 
          AdapterResponse.failure(exception);

      if(caller.mounted) {
        onComplete?.call(response);
        onFailure?.call(exception);
      }

      return response;
    }
  }

  static Future<AdapterResponse<int>> notDoFooBar(State caller, {
    void Function(int)? onSuccess,
    void Function(Exception)? onFailure,
    void Function()? onNullValue,
    void Function(AdapterResponse<int>)? onComplete,
  }) async {

    try {
    final json = await _channel.invokeMethod('notDoFooBar');
      final value = json.toInt();
      final AdapterResponse<int> response = 
          AdapterResponse.success(value);

      if(caller.mounted) {
        onComplete?.call(response);      
          onSuccess?.call(value);
      }

      return response;
      
    } catch (e) {
      
      final exception = e is Error 
          ? Exception(e.stackTrace) 
          : e as Exception;
      
      final AdapterResponse<int> response = 
          AdapterResponse.failure(exception);

      if(caller.mounted) {
        onComplete?.call(response);
        onFailure?.call(exception);
      }

      return response;
    }
  }

  static Future<AdapterResponse<bool>> fooBarBinary(State caller, {
    void Function(bool)? onSuccess,
    void Function(Exception)? onFailure,
    void Function()? onNullValue,
    void Function(AdapterResponse<bool>)? onComplete,
  }) async {

    try {
    final json = await _channel.invokeMethod('fooBarBinary');
      final value = json;
      final AdapterResponse<bool> response = 
          AdapterResponse.success(value);

      if(caller.mounted) {
        onComplete?.call(response);      
          onSuccess?.call(value);
      }

      return response;
      
    } catch (e) {
      
      final exception = e is Error 
          ? Exception(e.stackTrace) 
          : e as Exception;
      
      final AdapterResponse<bool> response = 
          AdapterResponse.failure(exception);

      if(caller.mounted) {
        onComplete?.call(response);
        onFailure?.call(exception);
      }

      return response;
    }
  }

  static Future<AdapterResponse<double>> twoFoo4You(State caller, {
    void Function(double)? onSuccess,
    void Function(Exception)? onFailure,
    void Function()? onNullValue,
    void Function(AdapterResponse<double>)? onComplete,
  }) async {

    try {
    final json = await _channel.invokeMethod('twoFoo4You');
      final value = json.toDouble();
      final AdapterResponse<double> response = 
          AdapterResponse.success(value);

      if(caller.mounted) {
        onComplete?.call(response);      
          onSuccess?.call(value);
      }

      return response;
      
    } catch (e) {
      
      final exception = e is Error 
          ? Exception(e.stackTrace) 
          : e as Exception;
      
      final AdapterResponse<double> response = 
          AdapterResponse.failure(exception);

      if(caller.mounted) {
        onComplete?.call(response);
        onFailure?.call(exception);
      }

      return response;
    }
  }

  static Future<AdapterResponse<ExoticFoo>> getExoticFoo(State caller, {
    void Function(ExoticFoo)? onSuccess,
    void Function(Exception)? onFailure,
    void Function()? onNullValue,
    void Function(AdapterResponse<ExoticFoo>)? onComplete,
  }) async {

    try {
         
               final jsonResponse = await _channel.invokeMethod('getExoticFoo');
      final json = jsonDecode(jsonResponse);
      final value = ExoticFoo.fromJson(json);
      final AdapterResponse<ExoticFoo> response = 
          AdapterResponse.success(value);

      if(caller.mounted) {
        onComplete?.call(response);      
          onSuccess?.call(value);
      }

      return response;
      
    } catch (e) {
      
      final exception = e is Error 
          ? Exception(e.stackTrace) 
          : e as Exception;
      
      final AdapterResponse<ExoticFoo> response = 
          AdapterResponse.failure(exception);

      if(caller.mounted) {
        onComplete?.call(response);
        onFailure?.call(exception);
      }

      return response;
    }
  }

  static Future<AdapterResponse<List<String>>> manyFooBars(State caller, {
    void Function(List<String>)? onSuccess,
    void Function(Exception)? onFailure,
    void Function()? onNullValue,
    void Function(AdapterResponse<List<String>>)? onComplete,
  }) async {

    try {
         
               final jsonResponse = await _channel.invokeMethod('manyFooBars');
      final json = jsonDecode(jsonResponse);
      final value = List<String>.from(json.map((o) => o.toString()));
      final AdapterResponse<List<String>> response = 
          AdapterResponse.success(value);

      if(caller.mounted) {
        onComplete?.call(response);      
          onSuccess?.call(value);
      }

      return response;
      
    } catch (e) {
      
      final exception = e is Error 
          ? Exception(e.stackTrace) 
          : e as Exception;
      
      final AdapterResponse<List<String>> response = 
          AdapterResponse.failure(exception);

      if(caller.mounted) {
        onComplete?.call(response);
        onFailure?.call(exception);
      }

      return response;
    }
  }

  static Future<AdapterResponse<List<String>?>> maybeFoos(State caller, {
    void Function(List<String>?)? onSuccess,
    void Function(Exception)? onFailure,
    void Function()? onNullValue,
    void Function(AdapterResponse<List<String>?>)? onComplete,
  }) async {

    try {
         
               final jsonResponse = await _channel.invokeMethod('maybeFoos');
      final json = jsonDecode(jsonResponse);
      final value = List<String>.from(json?.map((o) => o.toString()));
      final AdapterResponse<List<String>?> response = 
          AdapterResponse.success(value);

      if(caller.mounted) {
        onComplete?.call(response);        if(value == null) {
          onNullValue?.call();
        } else {
          onSuccess?.call(value!);
        }      }

      return response;
      
    } catch (e) {
      
      final exception = e is Error 
          ? Exception(e.stackTrace) 
          : e as Exception;
      
      final AdapterResponse<List<String>?> response = 
          AdapterResponse.failure(exception);

      if(caller.mounted) {
        onComplete?.call(response);
        onFailure?.call(exception);
      }

      return response;
    }
  }

}


class Foo {
  
  Foo({
required this.field1,
  });
  
factory Foo.fromJson(dynamic json) {
   return Foo (
      field1: json['field1'].toString(),
   );
 }   

 final String field1;

 Map<String, dynamic> toJson() {
   return {
     'field1': field1    
   };
 }     
}


class FooMessage {
  
  FooMessage({
required this.field2,
required this.field4,
required this.field1,
required this.field3,
this.field6,
this.field8,
this.field5,
this.field7,
  });
  
factory FooMessage.fromJson(dynamic json) {
   return FooMessage (
      field6: Foo.fromJson(json['field6']),
      field8: json['field8'] == null ? [] : List<Foo>.from(json['field8']?.map((o) => Foo.fromJson(o))),
      field5: json['field5']?.toString(),
      field7: json['field7'] == null ? [] : List<String>.from(json['field7']?.map((o) => o.toString())),
      field2: Foo.fromJson(json['field2']),
      field4: List<Foo>.from(json['field4'].map((o) => Foo.fromJson(o))),
      field1: json['field1'].toString(),
      field3: List<String>.from(json['field3'].map((o) => o.toString())),
   );
 }   

 final Foo field2;
 final List<Foo> field4;
 final String field1;
 final List<String> field3;
 Foo? field6;
 List<Foo>? field8;
 String? field5;
 List<String>? field7;

 Map<String, dynamic> toJson() {
   return {
     'field6': field6?.toJson(),
     'field8': field8?.map((o) => o.toJson()).toList(),
     'field5': field5,
     'field7': field7?.toList(),
     'field2': field2.toJson(),
     'field4': field4.map((o) => o.toJson()).toList(),
     'field1': field1,
     'field3': field3.toList()    
   };
 }     
}

class FooEnum {
final String string;

const FooEnum._(this.string);

  static const foo1 = FooEnum._('FOO1');
  static const foo2 = FooEnum._('FOO2');
  static const foo3 = FooEnum._('FOO3');
  static const none = FooEnum._('none');
  
  static const values = [foo1,foo2,foo3];
  
  @override
  String toString() {
      return 'FooEnum.$string';
  }
  
    static FooEnum fromJson(String value) {
    switch(value) {
          case "FOO1": return FooEnum.foo1;
      case "FOO2": return FooEnum.foo2;
      case "FOO3": return FooEnum.foo3;
      default: return FooEnum.none;
    }
 }

  String? toJson() {
    switch(this) { 
          case FooEnum.foo1: return "FOO1";
      case FooEnum.foo2: return "FOO2";
      case FooEnum.foo3: return "FOO3";
      default: return null;
    }
  }

  
}

class BarEnum {
final String string;

const BarEnum._(this.string);

  static const bar1 = BarEnum._('bar');
  static const bar2 = BarEnum._('barry');
  static const bar3 = BarEnum._('bardon');
  static const none = BarEnum._('none');
  
  static const values = [bar1,bar2,bar3];
  
  @override
  String toString() {
      return 'BarEnum.$string';
  }
  
    static BarEnum fromJson(String value) {
    switch(value) {
          case "bar": return BarEnum.bar1;
      case "barry": return BarEnum.bar2;
      case "bardon": return BarEnum.bar3;
      default: return BarEnum.none;
    }
 }

  String? toJson() {
    switch(this) { 
          case BarEnum.bar1: return "bar";
      case BarEnum.bar2: return "barry";
      case BarEnum.bar3: return "bardon";
      default: return null;
    }
  }

  
}

    '''
}
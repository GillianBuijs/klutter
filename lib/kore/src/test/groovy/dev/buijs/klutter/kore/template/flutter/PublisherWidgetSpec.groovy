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
package dev.buijs.klutter.kore.template.flutter

import dev.buijs.klutter.kore.ast.CustomType
import dev.buijs.klutter.kore.ast.FlutterSyncChannel
import dev.buijs.klutter.kore.ast.StringType
import dev.buijs.klutter.kore.templates.flutter.*
import dev.buijs.klutter.kore.test.TestUtil
import spock.lang.Specification

class PublisherWidgetSpec extends Specification {

    def "Verify a PublisherWidget without message and standard response type"() {
        given:
        def widget = new PublisherWidget(
                new FlutterSyncChannel("klutter.test/publisher"),
                new FlutterEvent("sayWhat"),
                new FlutterExtension("SayWhatNow"),
                null,
                new FlutterMessageType(new StringType()),
                new FlutterMethod("getSayWhat"),
                false
        )

        expect:
        TestUtil.verify(PublisherWidgetKt.print(widget), noRequestStringResponseWidget)
    }

    def "Verify a PublisherWidget without message and custom response type"() {
        given:
        def widget = new PublisherWidget(
                new FlutterSyncChannel("klutter.test/publisher"),
                new FlutterEvent("sayWhat"),
                new FlutterExtension("SayWhatNow"),
                null,
                new FlutterMessageType(new CustomType("MyType", "foo.example", [])),
                new FlutterMethod("getSayWhat"),
                false
        )

        expect:
        TestUtil.verify(PublisherWidgetKt.print(widget), noRequestCustomTypeResponseWidget)
    }

    def "Verify a PublisherWidget with standard message and standard response type"() {
        given:
        def widget = new PublisherWidget(
                new FlutterSyncChannel("klutter.test/publisher"),
                new FlutterEvent("sayWhat"),
                new FlutterExtension("SayWhatNow"),
                new FlutterMessageType(new StringType()),
                new FlutterMessageType(new StringType()),
                new FlutterMethod("getSayWhat"),
                false
        )

        expect:
        TestUtil.verify(PublisherWidgetKt.print(widget), stringRequestStringResponseWidget)
    }

    def "Verify a PublisherWidget with custom message and custom response type"() {
        given:
        def widget = new PublisherWidget(
                new FlutterSyncChannel("klutter.test/publisher"),
                new FlutterEvent("sayWhat"),
                new FlutterExtension("SayWhatNow"),
                new FlutterMessageType(new CustomType("MyType", "foo.example", [])),
                new FlutterMessageType(new CustomType("MyOtherType", "foo.example", [])),
                new FlutterMethod("getSayWhat"),
                false
        )

        expect:
        TestUtil.verify(PublisherWidgetKt.print(widget), customTypeRequestCustomTypeResponseWidget)
    }

    def "Verify a PublisherWidget with custom message and standard response type"() {
        given:
        def widget = new PublisherWidget(
                new FlutterSyncChannel("klutter.test/publisher"),
                new FlutterEvent("sayWhat"),
                new FlutterExtension("SayWhatNow"),
                new FlutterMessageType(new StringType()),
                new FlutterMessageType(new CustomType("MyType", "foo.example", [])),
                new FlutterMethod("getSayWhat"),
                false
        )

        expect:
        TestUtil.verify(PublisherWidgetKt.print(widget), customTypeRequestStringResponseWidget)
    }

    def "Verify a PublisherWidget with standard message and custom response type"() {
        given:
        def widget = new PublisherWidget(
                new FlutterSyncChannel("klutter.test/publisher"),
                new FlutterEvent("sayWhat"),
                new FlutterExtension("SayWhatNow"),
                new FlutterMessageType(new CustomType("MyType", "foo.example", [])),
                new FlutterMessageType(new StringType()),
                new FlutterMethod("getSayWhat"),
                false
        )

        expect:
        TestUtil.verify(PublisherWidgetKt.print(widget), stringRequestCustomTypeResponseWidget)
    }

    def noRequestStringResponseWidget =
              '''import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:klutter_ui/klutter_ui.dart';
            

const MethodChannel _channel =
  MethodChannel('klutter.test/publisher');

extension SayWhatNow on State {
  void getSayWhat({
    void Function(String)? onSuccess,
    void Function(Exception)? onFailure,
    void Function()? onNullValue,
    void Function(AdapterResponse<String>)? onComplete,
}) => doEvent<String>(
    state: this,
    event: "sayWhat",
    channel: _channel,
    onSuccess: onSuccess,
    onFailure: onFailure,
    onNullValue: onNullValue,
    onComplete: onComplete,
            
  );
}

void getSayWhat({
        
    State? state, 
    void Function(String)? onSuccess,
    void Function(Exception)? onFailure,
    void Function()? onNullValue,
    void Function(AdapterResponse<String>)? onComplete,
}) => doEvent<String>(
    state: state,
    event: "sayWhat",
    channel: _channel,
    onSuccess: onSuccess,
    onFailure: onFailure,
    onNullValue: onNullValue,
    onComplete: onComplete,
            
);
                '''

    def noRequestCustomTypeResponseWidget =
            '''import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:klutter_ui/klutter_ui.dart';
            
import '../my_type_dataclass.dart';
import '../my_type_extensions.dart';
                

const MethodChannel _channel =
  MethodChannel('klutter.test/publisher');

extension SayWhatNow on State {
  void getSayWhat({
    void Function(MyType)? onSuccess,
    void Function(Exception)? onFailure,
    void Function()? onNullValue,
    void Function(AdapterResponse<MyType>)? onComplete,
}) => doEvent<MyType>(
    state: this,
    event: "sayWhat",
    channel: _channel,
    onSuccess: onSuccess,
    onFailure: onFailure,
    onNullValue: onNullValue,
    onComplete: onComplete,
            decode: (String json) => json.toMyType,
  );
}

void getSayWhat({
        
    State? state, 
    void Function(MyType)? onSuccess,
    void Function(Exception)? onFailure,
    void Function()? onNullValue,
    void Function(AdapterResponse<MyType>)? onComplete,
}) => doEvent<MyType>(
    state: state,
    event: "sayWhat",
    channel: _channel,
    onSuccess: onSuccess,
    onFailure: onFailure,
    onNullValue: onNullValue,
    onComplete: onComplete,
            decode: (String json) => json.toMyType,
);
                '''

    def stringRequestStringResponseWidget =
            '''import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:klutter_ui/klutter_ui.dart';
            

const MethodChannel _channel =
  MethodChannel('klutter.test/publisher');

extension SayWhatNow on State {
  void getSayWhat({    required String message,
    void Function(String)? onSuccess,
    void Function(Exception)? onFailure,
    void Function()? onNullValue,
    void Function(AdapterResponse<String>)? onComplete,
}) => doEvent<String>(
    state: this,
    event: "sayWhat",
    channel: _channel,
    onSuccess: onSuccess,
    onFailure: onFailure,
    onNullValue: onNullValue,
    onComplete: onComplete,
            
  );
}

void getSayWhat({
    required String message,
    State? state, 
    void Function(String)? onSuccess,
    void Function(Exception)? onFailure,
    void Function()? onNullValue,
    void Function(AdapterResponse<String>)? onComplete,
}) => doEvent<String>(
    state: state,
    event: "sayWhat",
    channel: _channel,
    onSuccess: onSuccess,
    onFailure: onFailure,
    onNullValue: onNullValue,
    onComplete: onComplete,
    message: message,
);
                '''

    def customTypeRequestCustomTypeResponseWidget = '''import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:klutter_ui/klutter_ui.dart';
            
import '../my_type_dataclass.dart';
import '../my_type_extensions.dart';
                
import '../my_other_type_dataclass.dart';
import '../my_other_type_extensions.dart';
                

const MethodChannel _channel =
  MethodChannel('klutter.test/publisher');

extension SayWhatNow on State {
  void getSayWhat({    required MyType message,
    void Function(MyOtherType)? onSuccess,
    void Function(Exception)? onFailure,
    void Function()? onNullValue,
    void Function(AdapterResponse<MyOtherType>)? onComplete,
}) => doEvent<MyOtherType>(
    state: this,
    event: "sayWhat",
    channel: _channel,
    onSuccess: onSuccess,
    onFailure: onFailure,
    onNullValue: onNullValue,
    onComplete: onComplete,
    encode: (dynamic data) => (data as MyType).toJson,
    decode: (String json) => json.toMyOtherType,
  );
}

void getSayWhat({
    required MyType message,
    State? state, 
    void Function(MyOtherType)? onSuccess,
    void Function(Exception)? onFailure,
    void Function()? onNullValue,
    void Function(AdapterResponse<MyOtherType>)? onComplete,
}) => doEvent<MyOtherType>(
    state: state,
    event: "sayWhat",
    channel: _channel,
    onSuccess: onSuccess,
    onFailure: onFailure,
    onNullValue: onNullValue,
    onComplete: onComplete,
    message: message,
    encode: (dynamic data) => (data as MyType).toJson,
    decode: (String json) => json.toMyOtherType,
);
    '''

    def customTypeRequestStringResponseWidget = '''
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:klutter_ui/klutter_ui.dart';
            
import '../my_type_dataclass.dart';
import '../my_type_extensions.dart';
                

const MethodChannel _channel =
  MethodChannel('klutter.test/publisher');

extension SayWhatNow on State {
  void getSayWhat({    required String message,
    void Function(MyType)? onSuccess,
    void Function(Exception)? onFailure,
    void Function()? onNullValue,
    void Function(AdapterResponse<MyType>)? onComplete,
}) => doEvent<MyType>(
    state: this,
    event: "sayWhat",
    channel: _channel,
    onSuccess: onSuccess,
    onFailure: onFailure,
    onNullValue: onNullValue,
    onComplete: onComplete,
            decode: (String json) => json.toMyType,
  );
}

void getSayWhat({
    required String message,
    State? state, 
    void Function(MyType)? onSuccess,
    void Function(Exception)? onFailure,
    void Function()? onNullValue,
    void Function(AdapterResponse<MyType>)? onComplete,
}) => doEvent<MyType>(
    state: state,
    event: "sayWhat",
    channel: _channel,
    onSuccess: onSuccess,
    onFailure: onFailure,
    onNullValue: onNullValue,
    onComplete: onComplete,
    message: message,decode: (String json) => json.toMyType,
);
    '''

    def stringRequestCustomTypeResponseWidget = '''import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:klutter_ui/klutter_ui.dart';
            
import '../my_type_dataclass.dart';
import '../my_type_extensions.dart';
                

const MethodChannel _channel =
  MethodChannel('klutter.test/publisher');

extension SayWhatNow on State {
  void getSayWhat({    required MyType message,
    void Function(String)? onSuccess,
    void Function(Exception)? onFailure,
    void Function()? onNullValue,
    void Function(AdapterResponse<String>)? onComplete,
}) => doEvent<String>(
    state: this,
    event: "sayWhat",
    channel: _channel,
    onSuccess: onSuccess,
    onFailure: onFailure,
    onNullValue: onNullValue,
    onComplete: onComplete,
    encode: (dynamic data) => (data as MyType).toJson,
  );
}

void getSayWhat({
    required MyType message,
    State? state, 
    void Function(String)? onSuccess,
    void Function(Exception)? onFailure,
    void Function()? onNullValue,
    void Function(AdapterResponse<String>)? onComplete,
}) => doEvent<String>(
    state: state,
    event: "sayWhat",
    channel: _channel,
    onSuccess: onSuccess,
    onFailure: onFailure,
    onNullValue: onNullValue,
    onComplete: onComplete,
    message: message,
    encode: (dynamic data) => (data as MyType).toJson,
);
    '''
}
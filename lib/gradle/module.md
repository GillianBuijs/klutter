# Klutter Gradle Plugin

The main purpose of the Klutter Gradle Plugin is to connect a Flutter frontend with a Kotlin Multiplatform backend.
This plugin provides a set of Gradle tasks which will generate anything from settings files to
Kotlin/Dart/Groovy code needed to make Flutter and KMP work together. 

<B>Important:</B> For using and/or creating Flutter plugins with Klutter you should use the pub [plugin](https://github.com/buijs-dev/klutter-dart).

## Installation
Preferred way of installing/using this plugin is by starting a new project with one of:
- [Intellij](https://buijs.dev/klutter-3/) plugin
- [Android Studio](https://buijs.dev/klutter-4/) plugin
- [Kradle](https://buijs.dev/kradle-1/) cli tool

## Tasks
The gradle plugin defines tasks for compiling the Kotlin Multiplatform module, 
generating Dart code and more. Some tasks are executed automatically before (compiler plugin) 
and after running a gradle build.

1. [klutterCompileProtoSchemas](#Task:%20klutterCompileProtoSchemas)
2. [klutterCopyAarFile](#Task:%20klutterCopyAarFile)
3. [klutterCopyFramework](#Task:%20klutterCopyFramework)
4. [klutterGenerateFlutterLib](#Task:%20klutterGenerateFlutterLib)
5. [klutterGenerateProtoSchemas](#Task:%20klutterGenerateProtoSchemas)
6. [klutterGetDartProtoc](#Task:%20klutterDartProtoc)
7. [klutterGetKradle](#Task:%20klutterGetKradle)
8. [klutterGetProtoc](#Task:%20klutterGetProtoc)

# Task: klutterCompileProtoSchemas
Compile protocol buffer schemas which generates boilerplate code for both Kotlin and Dart.
This task does nothing when the protobuf feature is not enabled.

# Task: klutterCopyAarFile
Copy the root/platform aar file to root/android folder.

# Task: klutterCopyFramework
Copy the root/platform iOS Framework to root/ios folder.

# Task: klutterGenerateFlutterLib
Generate the Dart library file in the root/lib folder.

# Task: klutterGenerateProtoSchemas
Generate protocol buffer schemas which can be used by [klutterCompileProtoSchemas](#Task:%20klutterCompileProtoSchemas).
This task does nothing when the protobuf feature is not enabled.

# Task: klutterGetDartProtoc
Download the dart protobuf plugin.

# Task: klutterGetKradle
Download the kradle cli tool.

# Task: klutterGetProtoc
Download the protoc executable tool. The distribution url can be specified 
in the kradle.env file:

```properties
protoc.url=https://github.com/protocolbuffers/protobuf/releases/download/v25.3/protoc-25.3-osx-universal_binary.zip
```

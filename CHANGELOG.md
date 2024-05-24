# CHANGELOG

## v2024.1.3.beta
- Use klutter (dart) 3.0.2.

## v2024.1.2.beta
- Bugfix: Typo in the Swift Plugin template.
- Use klutter (dart) 3.0.1.

## v2024.1.1.beta
- Remove kradle module from project (kradle is the default executable since [kradle](..%2Fklutter-dart%2Fkradle) 3.0.0).
- Add support for protocol buffers.

## v2023.3.1.beta
- Refactor task module to package in kore.
- Remove task module from BOM e.a.
- Rename kommand to kradle.

## v2023.2.2.beta
- Remove example/integration_test folder on project init from example app.
- Skip iOS setup during project init on Windows platform.

## v2023.2.1.beta
- Remove example/integration_test folder on project init.
- Bugfix: EventSink received incorrect variable on error.
- Uses [klutter-dart](https://pub.dev/packages/klutter) version 1.0.0.
- Uses [klutter-dart-ui](https://pub.dev/packages/klutter_ui) version 1.0.0.

## v2023.1.1.beta
- Added support for request parameters.
- Added support for streaming data from platform to ui (using EventChannel).
- Uses [klutter-dart](https://pub.dev/packages/klutter) version 0.3.0 (only required as dev_dependency now).
- Uses [klutter-dart-ui](https://pub.dev/packages/klutter_ui) version 0.0.3 (split from klutter to contain only Flutter code).
- Uses [squint_json](https://pub.dev/packages/squint_json) version 0.1.2 for generating dart classes.
- Introduces bill-of-materials maven dependency for easier dependency management.
- Introduces compiler plugin which handles build, code generation, etc.
- Removed all Gradle tasks bar klutterCopyAarFile and klutterCopyFramework because compiler plugin handles all tasks pre-build.
- Added embedded dependency function for use in build.gradle.kts.
- Android build.gradle uses Klutter Gradle plugin to load all embedded and/or klutter dependencies.
- Removed @KlutterAdapter annotation because it is no longer used.
- Renamed @KlutterResponse to @Response.
- Renamed @KlutterAdaptee to @Event.
- Renamed KlutterJSON to JSON.
- Code generated in example/consumer app uses flutter-engine-kmp utilities (less boilerplate code).

## v2022.r6-9.alpha
- Ignore all unknown properties in pubspec.yaml.

## v2022.r6-8.alpha
- Merged annotations-kmp and annotations-jvm to klutter-annotations.
- Renamed package core to kore.
- Renamed package plugin.gradle to gradle.
- Renamed Gradle ID to dev.buijs.klutter.
- Added dependency handler for easier dependency management.
- Added task klutterBuildAndroid which builds a flutter debug .apk for Android.
- Added task klutterBuildAndroidIos which executes klutterBuildAndroid and klutterBuildAndroidIos.
- Added task klutterBuildIos which builds a flutter debug Runner.app for iOS.
- Added task klutterCopyAarFile which copies the platform .aar file to android folder.
- Added task klutterIosFramework which copies the platform Framework to ios folder.
- Added iosSimulatorArm64 to platform build.gradle.kts for running app on Mac M1/M2 devices.
- Added module klutter-jetbrains to build Intellij and Android Studio plugin.
- Added module klutter-tasks.
- Added new Klutter logo to README (hooray).

## v2022.r6-7.alpha
- [Bugfix] Fixed adding exclusions to Podfile when they are already partially present.

## v2022.r6-5.alpha
- Improved generated Flutter code to handle null values.
- Removed AdapterResponse class from generated Flutter code and added klutter-dart import.
- Added AndroidContext annotation to pass Android Context.
- [Bugfix] Fixed adding exclusions to Podfile when they are already added.

## v2022.r6-2.alpha
- [Bugfix] Fixed missing dart:convert import in generated flutter adapter class.

## v2022.r6-1.alpha
- [Bugfix] Fixed standard nullable fields to be incorrectly flagged as custom datatype.

## v2022.r6.alpha
- [Bugfix] klutterExcludeArchsPlatformPodspec: A warning is now logged when adding exclusion fails.
- [Bugfix] klutterGeneratedAdapters: Do not append Dart Adapter class with 'Plugin'.

## v2022-alpha-5
- Removed all support for a Klutter project.
- Removed redundant tasks.
- Removed redundant DSL for Gradle build file.
- Renamed generateAdapters task to klutterGenerateAdapters.
- Add new task klutterExcludeArchsPlatformPodspec.
- Refactored core internals for easier maintenance.

## v2022-alpha-3
- Add support for generating boilerplate code in a Flutter project.
- Separate AndroidManifest deserializer from the AndroidManifestVisitor.
- Platform applicationId is changed to include 'platform'. Having the same applicationId for the platform module 
and app module causes issues when building for Android.
- Renamed klutter.properties to klutter.secrets to better highlight it's use.
- Klutter secrets utility looks in env variables for a variable name if not found in klutter.secrets.
- Add signingConfig for release build in android/app build.gradle

## v2022-pre-alpha-5
- Extract all task functionality from the plugins module and add it to klutter-core. CLI and Gradle plugin can now both use the same functionality.
- Move annotations-processor to klutter-core because there is no requirement to use the processor on any other place.
- Add buildSrc to the template.

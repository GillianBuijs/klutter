package dev.buijs.klutter.plugins.gradle.tasks.adapter.flutter

import dev.buijs.klutter.core.Android
import dev.buijs.klutter.core.Root
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import java.nio.file.Files
import kotlin.io.path.createDirectories

/**
 * @author Gillian Buijs
 */
class AndroidBuildGradleGeneratorTest: WordSpec({

    "Using the AndroidBuildGradleGenerator" should {
        "create a valid and fully configured android build.gradle file" {
            val projectDir = Files.createTempDirectory("")
            projectDir.createDirectories()

            val androidDir = projectDir.resolve("flutter/android/").toAbsolutePath()
            androidDir.createDirectories()

            val androidAppDir = projectDir.resolve("flutter/android/app").toAbsolutePath()
            androidAppDir.createDirectories()

            val flutterDir = projectDir.resolve("flutter/lib").toAbsolutePath()
            flutterDir.createDirectories()

            val klutterDir = projectDir.resolve(".klutter").toAbsolutePath().toFile()
            klutterDir.mkdirs()

            val sut = AndroidBuildGradleGenerator(
                Android(
                    file = androidDir.toFile(),
                    root = Root(projectDir.toFile()),
                ),
            )

            sut.generate()

            val dollar = "$"
            val generatedGradleFile = androidAppDir.resolve("build.gradle").toFile()
            generatedGradleFile.exists() shouldBe true
            generatedGradleFile.readText().filter { !it.isWhitespace() } shouldBe """
                // Autogenerated by Klutter
                // Do not edit directly
                // See: https://buijs.dev/klutter
                
                import dev.buijs.klutter.core.*
                
                def localProperties = new Properties()
                def localPropertiesFile = rootProject.file('local.properties')
                if (localPropertiesFile.exists()) {
                    localPropertiesFile.withReader('UTF-8') { reader ->
                        localProperties.load(reader)
                    }
                }
                
                def flutterRoot = localProperties.getProperty('flutter.sdk')
                if (flutterRoot == null) {
                    throw new GradleException("Flutter SDK not found. Define location with flutter.sdk in the local.properties file.")
                }
                
                apply plugin: 'com.android.application'
                apply plugin: 'kotlin-android'
                apply from: "${dollar}flutterRoot/packages/flutter_tools/gradle/flutter.gradle"
                
                def propertiesFile = new File("$dollar{projectDir}/../../buildSrc/buildsrc.properties")
                if (!propertiesFile.exists()) {
                    throw new GradleException("File not found ${dollar}propertiesFile")
                }
                
                def properties = new Properties()
                propertiesFile.withReader('UTF-8') { reader ->
                    properties.load(reader)
                }
                
                def appId = properties.getProperty('app.id')
                if (appId == null) {
                    throw new GradleException("ApplicationId not found. Define app.id in buildSrc/buildsrc.properties")
                }
                
                def minSdk = properties.getProperty('android.sdk.min')
                if (minSdk == null) {
                    throw new GradleException("Android min SDK version not found. Define android.sdk.min in buildSrc/buildsrc.properties")
                }
                
                def targetSdk = properties.getProperty('android.sdk.target')
                if (targetSdk == null) {
                    throw new GradleException("Android target SDK version not found. Define android.target.sdk in buildSrc/buildsrc.properties")
                }
                
                def compileSdk = properties.getProperty('android.sdk.compile')
                if (compileSdk == null) {
                    throw new GradleException("Android compile SDK version not found. Define android.compile.sdk in buildSrc/buildsrc.properties")
                }
                
                def appVersionCode = properties.getProperty('app.version.code')
                if (appVersionCode == null) {
                    throw new GradleException("App version code not found. Define app.version.code in buildSrc/buildsrc.properties")
                }
                
                def appVersionName = properties.getProperty('app.version.name')
                if (appVersionName == null) {
                    throw new GradleException("App version name not found. Define app.version.name in buildSrc/buildsrc.properties")
                }
                
                def klutter = properties.getProperty('klutter')
                if (klutter == null) {
                    throw new GradleException("Klutter version not found. Define klutter in buildSrc/buildsrc.properties")
                }
                
                def kotlin = properties.getProperty('kotlin')
                if (kotlin == null) {
                    throw new GradleException("Kotlin version not found. Define kotlin in buildSrc/buildsrc.properties")
                }
                
                def kotlinxCoroutinesVersion = properties.getProperty('kotlinxCoroutinesVersion')
                if (kotlinxCoroutinesVersion == null) {
                    throw new GradleException("Kotlinx Coroutines version not found. Define kotlinxCoroutinesVersion in buildSrc/buildsrc.properties")
                }      
                
                
                def secrets = Klutter.secrets(project)
                
                android {
                    compileSdkVersion compileSdk.toInteger()
                
                    compileOptions {
                        sourceCompatibility JavaVersion.VERSION_1_8
                        targetCompatibility JavaVersion.VERSION_1_8
                    }
                
                    kotlinOptions {
                        jvmTarget = '1.8'
                    }
                
                    sourceSets {
                        main.java.srcDirs += 'src/main/kotlin'
                    }
                
                    defaultConfig {
                        applicationId appId
                        minSdkVersion minSdk.toInteger()
                        targetSdkVersion targetSdk.toInteger()
                        versionCode appVersionCode.toInteger()
                        versionName appVersionName
                    }
                
                    signingConfigs {
                        release {
                            storeFile file(secrets.get("store.file.uri") ?: project.projectDir)
                            storePassword secrets.get("store.password") ?: ""
                            keyAlias secrets.get("key.alias") ?: ""
                            keyPassword secrets.get("key.password") ?: ""
                        }
                    }
                
                    buildTypes {
                
                        release {
                            minifyEnabled true
                        }
                
                        debug {
                            signingConfig signingConfigs.debug
                            debuggable true
                        }
                
                    }
                }
                
                flutter {
                    source '../..'
                }
                
                dependencies {
                    runtimeOnly "org.jetbrains.kotlinx:kotlinx-coroutines-android:${dollar}kotlinxCoroutinesVersion"
                    implementation files("../.klutter/platform-release.aar")
                    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${dollar}kotlin"
                    implementation "dev.buijs.klutter:core:${dollar}klutter"
                    implementation "dev.buijs.klutter:annotations-kmp-android:${dollar}klutter"
                }
                """.filter { !it.isWhitespace() }
        }
    }
})
# Klutter Annotations

This module contains annotations for the Klutter Framework.

### Use annotations
The klutterGenerateAdapters task will scan for the following annotations:
- AndroidContext
- Event
- Response

**AndroidContext**\
This annotation is complementary to the Event annotation. Adding this annotation to a method in the KMP module
enables the usage of Android Context in your platform code. See the battery app [tutorial](https://buijs.dev/klutter-2/)
for an example of its usage.

**Event**\
All corresponding methods in the KMP module should be annotated with **@Event** and given a corresponding name.
All methods annotated with this annotation are added to the GeneratedKlutterAdapter. In other words: Adding this annotation
to a method in KMP will make it visible for the Flutter.


For example this method in your KMP module:

```kotlin

package dev.foo.bar
        
class MyClass {
    @Event(name = "doPlatformCall")
    fun somePlatformMethod(): String {
        return doSomething().getSomeValue
    }
}

```
<br />

Will generate this code and add it to the GeneratedKlutterAdapter class:

```kotlin

    if (call.method == "doPlatformCall") {
        result.success("${dev.foo.bar.MyClass().somePlatformMethod()}")
    }

```
<br />

**Response**\
This annotation enables KMP and Flutter to communicate using data transfer objects instead of Strings.
The Response can be used to annotate a simple DTO after which Klutter will generate an equivalent
Dart DTO with all boilerplate code to (de)serialize.

The annotated class should comply with the following rules:

1. Must be an open class
2. Fields must be immutable
3. Must implement JSON class
4. No additional functionality implemented in body
5. Any field type should comply with the same rules

**Note:** Extending the KlutterJSON class might be no longer needed if a compiler plugin is created.

A Response acts as an interface between Flutter and KMP. These rules are designed to adhere to that function.

Open classes can be extended so the DTO can be used as interface between KMP and Flutter and you can extend it
to add behaviour designed for frontend or backend respectively. All fields must be immutable. The generated code includes
builders to create a new instance of the DTO if needed. Make sure to declare fields in the DTO as <i>val</i> and not var.
Any behaviour should be written in subclasses. To avoid any unnecessary complexity it may not inherit any fields/behaviour from other classes.
This is a functional design choise, not a technical limitation.

**Supported Kotlin datatypes**
1. Int
2. Double
3. Boolean
4. List
5. Map

**Enumerations?**\
Enumerations can be used as datatype but only if the enumeration itself has a no-args constructor.
Values should be defined in UPPER_SNAKE_CASE. Klutter will convert it to lowerCamelCase for usage in Dart/Flutter.
The value "none" is a reserved value used to represent null.

**Custom data types?**\
Any field declaration may use another DTO as type but that DTO should comply with before mentioned rules as well.

**What could possibly go wrong?**\
Any class annotated with Response that does not comply will be logged as error and ignored for processing.
Any other dependent class will also be ignored as result.

**Requirements**\
To serialize the Response kotlinx serialization is used. Add the plugin to the KMP build.gradle.kts:

````kotlin
plugins {
    kotlin("plugin.serialization") version "<use-project-kotlin-version>"
}
````
<br />
<br />

Also add the json dependency to the commonMain sourceset:

```kotlin
 val commonMain by getting {
    dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    }
}
```
<br />
<br />

**Examples**

Example of valid declaration:

```kotlin

    @Serializable
    @Response
    open class Something(
        val x: String?,
        val y: SomethingElse
    ): JSON<Something>() {

        override fun data() = this

        override fun strategy() = serializer()

    }

    @Serializable
    @Response
    open class SomethingElse(
        val a: Int?,
        val b: List<Boolean>
    ): JSON<SomethingElse>() {

        override fun data() = this

        override fun strategy() = serializer()

    }

```
<br />

Example of invalid declaration (Mutability):

```kotlin

    @Serializable
    @Response
    open class Something(
        var x: String?,
        var y: Int,
    ): JSON<SomethingElse>() {

        override fun data() = this

        override fun strategy() = serializer()

    }

```
<br />

Example of invalid declaration (SomethingElse class should not have a body):

```kotlin

    @Serializable
    @Response
    open class Something(
        val x: String?,
        var y: SomethingElse
    ): JSON<SomethingElse>() {

        override fun data() = this

        override fun strategy() = serializer()

    }

    @Serializable
    @Response
    open class SomethingElse(
        val a: Int?,
        val b: List<Boolean>
    ): JSON<SomethingElse>() {

        val bodyNotAllowed: Boolean = true
        
        override fun data() = this

        override fun strategy() = serializer()

    }

```
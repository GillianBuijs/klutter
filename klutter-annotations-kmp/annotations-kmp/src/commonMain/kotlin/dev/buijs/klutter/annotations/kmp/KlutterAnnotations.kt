package dev.buijs.klutter.annotations.kmp

/**
 * Annotation which denotes the annotated function as part
 * of the Flutter - KMP interface. When a function with this
 * annotation is scanned a method channel call delegation
 * will be generated by the Klutter Plugin.
 *
 * @author Gillian Buijs
 *
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
expect annotation class KlutterAdaptee(val name: String)

/**
 * Annotation which denotes the annotated class as the adapter
 * for Flutter and KMP. Should be used on the MainActivity in
 * flutter/android/app folder.
 *
 * @author Gillian Buijs
 *
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
expect annotation class KlutterAdapter


/**
 * Annotation for data classes in the KMP module which are possibly used
 * by functons annotated with [dev.buijs.klutter.annotations.kmp.KlutterAdaptee].
 *
 * The Klutter Plugin will generate Dart classes to enable typesafe communication
 * between Flutter and KMP.
 *
 * @author Gillian Buijs
 *
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
expect annotation class KlutterResponse
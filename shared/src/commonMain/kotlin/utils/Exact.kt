package utils

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class ExactType<T : Any>(val kClass: KClass<T>)

val <T : Any> KClass<T>.exact get() = ExactType(this)

class ExactKProperty<V>(val kProperty: KProperty<V>)

val <T> KProperty<T>.exact get() = ExactKProperty(this)
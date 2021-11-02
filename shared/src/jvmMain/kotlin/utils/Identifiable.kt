package utils

import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

class Identifiable<ID, T>(
    val pack: (T) -> ID,
    val unpack: (ID) -> T,
    private var value: T
) {
    constructor(kProperty1: KProperty1<T, ID>, unpack: (ID) -> T, value: T) : this(
        { kProperty1.get(it) },
        unpack,
        value
    )

    operator fun setValue(node: Any, property: KProperty<*>, newValue: T) {
        value = newValue
    }

    operator fun getValue(node: Any, property: KProperty<*>): T {
        return value
    }
}
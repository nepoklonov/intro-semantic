package utils.delegates

import kotlin.reflect.KProperty

class Once<T>(val get: () -> T?) {
    var field: T? = null
    operator fun getValue(any: Any, kProperty1: KProperty<*>): T? {
        return field ?: get()?.also { field = it }
    }
}

class OnceNotNull<T>(
    val get: () -> T?,
    private val errorMessage: String = ""
) {
    var field: T? = null
    operator fun getValue(any: Any, kProperty1: KProperty<*>): T {
        return field ?: get()?.also { field = it } ?: error(errorMessage)
    }
}

fun <T> once(get: () -> T?) = Once(get)

fun <T> Once<T>.notNull(message: String = "") = OnceNotNull(get)
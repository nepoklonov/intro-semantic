package utils

inline fun <reified T> Any.unsafeCast() : T = this as T
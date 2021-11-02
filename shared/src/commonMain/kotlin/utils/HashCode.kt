package utils

fun hashCodeBy(vararg vals: Any): Int {
    var result = 0
    vals.forEach { result = result * 31 + it.hashCode() }
    return result
}
package utils.collections

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun <T, R, V> Iterable<T>.foldWithStep(initial: V, step: (V) -> R, operation: (acc: R, T) -> V): V {
    var result = initial
    var accumulator = step(initial)
    forEach { element ->
        result = operation(accumulator, element)
        accumulator = step(result)
    }
    return result
}

inline fun <T, R, V> List<T>.foldRightWithStep(initial: V, step: (V) -> R, operation: (T, acc: R) -> V): V {
    var result = initial
    var accumulator = step(initial)
    if (!isEmpty()) {
        val iterator = listIterator(size)
        while (iterator.hasPrevious()) {
            result = operation(iterator.previous(), accumulator)
            accumulator = step(result)
        }
    }
    return result
}

@OptIn(ExperimentalContracts::class)
inline fun <T> Collection<T>.forEachAtLeastOnce(action: (T) -> Unit) {
    contract {
        callsInPlace(action, InvocationKind.AT_LEAST_ONCE)
    }
    if (isEmpty()) throw NoSuchElementException("Collection is empty.")
    for (element in this) action(element)
}

fun <T, R, V> Collection<T>.foldWithNextStep(initial: R, step: (V) -> R, operation: (acc: R, T) -> V): V {
    var accumulator = initial
    var result: V
    forEachAtLeastOnce { element ->
        result = operation(accumulator, element)
        accumulator = step(result)
    }
    return result
}

inline fun <T, R, V> List<T>.foldRightWithNextStep(initial: R, step: (V) -> R, operation: (T, acc: R) -> V): V {
    var accumulator = initial
    var result: V
    asReversed().forEachAtLeastOnce { element ->
        result = operation(element, accumulator)
        accumulator = step(result)
    }
    return result
}

package utils.collections

interface KeyElement<K> {
    val key: K
}

fun <K, V : KeyElement<K>> Collection<V>.toKeySet(): KeySet<K, V> = toKeySet { it.key }

fun <K, V : KeyElement<K>> Collection<V>.toMutableKeySet(): MutableKeySet<K, V> = toMutableKeySet { it.key }

fun <K, V : KeyElement<K>> keySetOf(vararg elements: V): KeySet<K, V> = elements.toList().toKeySet { it.key }

fun <K, V : KeyElement<K>> mutableKeySetOf(vararg elements: V): MutableKeySet<K, V> = elements.toList().toMutableKeySet { it.key }
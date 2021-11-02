package utils.collections

interface KeySet<K, V> : Set<V> {
    override fun iterator(): MutableIterator<V>

    fun containsKey(key: K): Boolean

    fun containsValue(value: V): Boolean

    operator fun get(key: K): V?

    val entries: MutableSet<MutableMap.MutableEntry<K, V>>

    val keys: MutableSet<K>

    val values: MutableCollection<V>

    fun toMap(): Map<K, V>

    fun toMutableMap(): MutableMap<K, V>
}

interface MutableKeySet<K, V> : KeySet<K, V>, MutableSet<V> {
    fun removeByKey(key: K): V?
}

inline fun <K, V> MutableKeySet<K, V>.getOrPut(key: K, defaultValue: () -> V): V = get(key) ?: defaultValue().also { add(it) }

fun <K, V> Collection<V>.toKeySet(keyFun: (V) -> K): KeySet<K, V> = SimpleKeySet(this, keyFun)

fun <K, V> Collection<V>.toMutableKeySet(keyFun: (V) -> K): MutableKeySet<K, V> = SimpleKeySet(this, keyFun)

operator fun <K, V: KeyElement<K>> KeySet<K, V>.plus(elements: Iterable<V>): KeySet<K, V> {
    val result = mutableKeySetOf<K, V>()
    result.addAll(this)
    result.addAll(elements)
    return result
}
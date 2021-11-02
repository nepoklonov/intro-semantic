package utils.collections

class SimpleKeySet<K, V> (
    elements: Collection<V> = emptySet(),
    val keyFun: (V) -> K,
) : MutableKeySet<K, V> {

    private val map: MutableMap<K, V> = mutableMapOf()

    init {
        addAll(elements)
    }

    override fun add(element: V): Boolean {
        val key = keyFun(element)
        if (map[key] == element) return false
        map[key] = element
        return true
    }

    override fun addAll(elements: Collection<V>): Boolean = elements.fold(false) { acc, it -> add(it) || acc }

    override fun clear() = map.clear()

    override fun iterator(): MutableIterator<V> = map.values.iterator()

    override fun remove(element: V): Boolean {
        val key = keyFun(element)
        if (!map.containsKey(key)) return false
        map.remove(key)
        return true
    }

    override fun removeAll(elements: Collection<V>): Boolean = elements.fold(false) { acc, it -> remove(it) || acc }

    override fun retainAll(elements: Collection<V>): Boolean {
        val toRemove = map.values.also { it.removeAll(elements) }
        return removeAll(toRemove)
    }

    override val size: Int
        get() = map.size

    override fun contains(element: V): Boolean = map[keyFun(element)] == element

    override fun containsAll(elements: Collection<V>): Boolean = elements.all { contains(it) }

    override fun isEmpty(): Boolean = map.isEmpty()

    override fun containsKey(key: K): Boolean = map.containsKey(key)

    override fun containsValue(value: V): Boolean = contains(value)

    override fun get(key: K): V? = map[key]

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = map.entries

    override val keys: MutableSet<K>
        get() = map.keys

    override val values: MutableCollection<V>
        get() = map.values

    override fun removeByKey(key: K): V? = map.remove(key)

    override fun toMap(): Map<K, V> = map.toMap()

    override fun toMutableMap(): MutableMap<K, V> = map.toMutableMap()

    override fun toString(): String {
        return map.toString()
    }

    override fun equals(other: Any?): Boolean {
        //TODO: strange ska, -- deal with it
        return other is Set<*> && (toHashSet() == other.toHashSet())
    }

    override fun hashCode(): Int = map.hashCode()
}
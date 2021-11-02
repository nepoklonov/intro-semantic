package utils

class OneWayPacker<T, ID>(
    val pack: (T) -> ID
)

class ReversiblePacker<T, ID>(
    val pack: (T) -> ID,
    val unpack: (ID) -> T
)

class Default<T>(
    val value: T
)

class OneWayPackerWithDefault<T, ID>(
    val pack: (T) -> ID,
    val value: T
)

class PackerBuilder<T, ID>

fun <T, ID> PackerBuilder<T, ID>.setPack(block: (T) -> ID) = OneWayPacker(block)

fun <T, ID> OneWayPacker<T, ID>.setUnpack(block: (ID) -> T) = ReversiblePacker(pack, block)

fun <T, ID> ReversiblePacker<T, ID>.setDefault(value: T) = Identifiable(pack, unpack, value)

fun <T> withDefault(value: T) = Default(value)

fun <T, ID> Default<T>.pack(block: (T) -> ID) = OneWayPackerWithDefault(block, value)

fun <T, ID> OneWayPackerWithDefault<T, ID>.unpack(block: (ID) -> T) = Identifiable(pack, block, value)
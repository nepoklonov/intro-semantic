package specifications

import kotlin.reflect.KClass

data class FullType(
    val dataType: DataType,
    val cardinality: Cardinality
)

enum class DataType(val kClass: KClass<*>) {
    STRING(String::class),
    CHARACTER(Char::class),
    BOOLEAN(Boolean::class),
    INTEGER(Int::class),
    LONG(Long::class),
    SHORT(Short::class),
    BYTE(Byte::class),
    DOUBLE(Double::class),
    DATE(Nothing::class),
    GEOSHAPE(String::class),
    UUID(Nothing::class)
}

enum class Multiplicity {
    MULTI,
    SIMPLE,
    ONE2MANY,
    MANY2ONE,
    ONE2ONE
}

enum class Cardinality {
    SINGLE, SET, LIST
}

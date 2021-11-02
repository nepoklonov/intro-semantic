package elements.schema.fundamental

import dto.ConvertibleToDto
import dto.PropertyClassDto
import dto.transform
import elements.*
import elements.schema.PropertyClassType
import org.janusgraph.core.Cardinality
import org.janusgraph.core.PropertyKey
import org.janusgraph.core.schema.JanusGraphManagement
import specifications.DataType
import structure.Fundamental
import utils.*
import java.util.*

actual class PropertyClass actual constructor(
    override var label: String,

    dataType: DataType,

    cardinality: specifications.Cardinality,

    @JanusProperty(Level.INTERNAL, "name", saveFlag = false)
    override val id: String,

    @JanusProperty(Level.CUSTOM, TYPE_PROPERTY_NAME)
    actual override val type: PropertyClassType

) : ElementClass, ConvertibleToDto<PropertyClassDto> {
    @JanusProperty(Level.INTERNAL)
    actual val dataType: DataType by dataTypePacker.setDefault(dataType)

    @JanusProperty(Level.INTERNAL)
    actual val cardinality: specifications.Cardinality by cardinalityPacker.setDefault(cardinality)

    override var fundamental: Fundamental? = null

    override fun hashCode(): Int = hashCodeBy(id, label, type, cardinality, dataType)

    override fun equals(other: Any?): Boolean {
        if (other !is PropertyClass) return false
        return label == other.label
                && id == other.id
                && type == other.type
                && cardinality == other.cardinality
                && dataType == other.dataType
    }

    override fun toString(): String {
        return listOf(::label, ::id, ::type, ::cardinality, ::dataType)
            .joinToString(", ", prefix = "[", postfix = "]") { "${it.name}=${it()}" }
    }

    companion object : ElementClassStatic<PropertyKey> {
        override fun getAllElements(mgmt: JanusGraphManagement): Iterable<PropertyKey> =
            mgmt.getRelationTypes(PropertyKey::class.java)

        override fun getById(mgmt: JanusGraphManagement, id: String): PropertyKey? = mgmt.getPropertyKey(id)
    }

    override fun convert(): PropertyClassDto = transform()

    @JanusConstructor
    constructor(
        label: String,
        dataType: Class<*>,
        cardinality: Cardinality,
        id: String,
        type: PropertyClassType
    ) : this(
        label = label,
        dataType = dataTypePacker.unpack(dataType),
        cardinality = cardinalityPacker.unpack(cardinality),
        id = id,
        type = type
    )
}


private val dataTypePacker =
    PackerBuilder<DataType, Class<*>>()
        .setPack { it.jvmClass }
        .setUnpack { DataType.values().first { type -> type.jvmClass == it } }
//TODO раньше здесь было single.

private val cardinalityPacker =
    PackerBuilder<specifications.Cardinality, Cardinality>()
        .setPack { Cardinality.valueOf(it.name) }
        .setUnpack { specifications.Cardinality.valueOf(it.name) }


val DataType.jvmClass get() = when(this) {
    DataType.DATE -> Date::class.java
    DataType.UUID -> java.util.UUID::class.java
    else -> kClass.javaObjectType
}
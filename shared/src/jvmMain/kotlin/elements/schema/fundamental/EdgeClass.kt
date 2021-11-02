package elements.schema.fundamental

import dto.*
import elements.*
import elements.schema.EdgeClassType
import elements.schema.model.EdgeRelation
import elements.schema.model.PropertyRelation
import org.janusgraph.core.EdgeLabel
import org.janusgraph.core.Multiplicity
import org.janusgraph.core.schema.JanusGraphManagement
import structure.Fundamental
import structure.schemaInfo
import utils.*
import utils.collections.MutableKeySet
import utils.collections.mutableKeySetOf

const val TYPE_PROPERTY_NAME = "type"


actual class EdgeClass actual constructor(
    override var label: String,

    @JanusProperty(Level.INTERNAL, "name", saveFlag = false)
    override val id: String,

    @JanusProperty(Level.CUSTOM, TYPE_PROPERTY_NAME)
    actual override val type: EdgeClassType,

    multiplicity: specifications.Multiplicity
) : ElementClassPropertyHolder, ConvertibleToDto<EdgeClassDto> {

    override fun hashCode(): Int = hashCodeBy(label, type, multiplicity)

    override fun equals(other: Any?): Boolean {
        if (other !is EdgeClass) return false
        return label == other.label && type == other.type && multiplicity == other.multiplicity
    }

    override fun toString(): String {
        return "id: $id label: $label"
    }

    @JanusProperty(level = Level.INTERNAL)
    actual val multiplicity by multiplicityPacker.setDefault(multiplicity)

    override val properties: MutableKeySet<String, PropertyRelation> = mutableKeySetOf()

    override var fundamental: Fundamental? = null

    actual val relations: Set<EdgeRelation>
        get() = schemaInfo?.relations ?: mutableSetOf()

    override fun convert(): EdgeClassDto = transform {
        EdgeClassDto::properties.exact from properties.map { it.convert() }.toSet()
    }

    companion object : ElementClassStatic<EdgeLabel> {
        override fun getAllElements(mgmt: JanusGraphManagement): Iterable<EdgeLabel> =
            mgmt.getRelationTypes(EdgeLabel::class.java)

        override fun getById(mgmt: JanusGraphManagement, id: String): EdgeLabel? = mgmt.getEdgeLabel(id)
    }

    @JanusConstructor
    constructor(
        label: String,
        id: String,
        type: EdgeClassType,
        multiplicity: Multiplicity
    ) : this(
        label = label,
        id = id,
        type = type,
        multiplicity = multiplicityPacker.unpack(multiplicity)
    )
}

val multiplicityPacker = PackerBuilder<specifications.Multiplicity, Multiplicity>()
    .setPack { Multiplicity.valueOf(it.name) }
    .setUnpack { specifications.Multiplicity.valueOf(it.name) }
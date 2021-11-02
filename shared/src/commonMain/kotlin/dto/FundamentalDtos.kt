package dto

import elements.schema.EdgeClassType
import elements.schema.ElementClassType
import elements.schema.NodeClassType
import elements.schema.PropertyClassType
import kotlinx.serialization.Serializable
import specifications.Cardinality
import specifications.DataType
import specifications.Multiplicity

interface ElementClassDto : SchemaElementDto {
    val label: String
    val type: ElementClassType<*>
}

@Serializable
data class NodeClassDto(
    override val label: String,
    override val type: NodeClassType,
    val properties: Set<PropertyRelationDto>
) : ElementClassDto/*, NodeDto*/

@Serializable
data class EdgeClassDto(
    override val label: String,
    override val type: EdgeClassType,
    val properties: Set<PropertyRelationDto>,
    val multiplicity: Multiplicity,
) : ElementClassDto/*, EdgeDto*/

@Serializable
data class PropertyClassDto(
    override val label: String,
    override val type: PropertyClassType,
    val cardinality: Cardinality,
    val dataType: DataType
) : ElementClassDto/*, PropertyDto*/

@Serializable
data class FundamentalDto(
    val nodeClasses: Set<NodeClassDto>,
    val edgeClasses: Set<EdgeClassDto>,
    val propertyClasses: Set<PropertyClassDto>
) : StructureDto


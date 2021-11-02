package dto

import kotlinx.serialization.Serializable

@Serializable
data class ComplexId(
    val label: String,
    val sourceKey: String,
    val targetKey: String
)

interface ModelElementDto : SchemaElementDto

@Serializable
data class ModelNodeDto(
    override val id: String, //TODO разобраться, что это и зачем оно нужно.
    val label: String
) : ModelElementDto, NodeDto, GraphElementDto

@Serializable
data class EdgeRelationDto(
    override val id: ComplexId //TODO понять, почему так.
) : ModelElementDto, EdgeDto, GraphElementDto

@Serializable
data class PropertyRelationDto(
    override val key: String, //TODO разобраться, что это и зачем оно нужно.
    override val label: String,
    val isPrimary: Boolean
) : ModelElementDto, PropertyDto

@Serializable
data class ModelGraphDto(
    override val nodes: Set<ModelNodeDto>,
    override val edges: Set<EdgeRelationDto>
) : GraphDto
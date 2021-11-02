package dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

//TODO rename
interface InstanceDto : OverallElementDto {
    val label: String
}

@Serializable
data class NodeInstanceDto(
    override val id: String,
    override val label: String,
    val properties: Set<@Contextual PropertyInstanceDto>
) : InstanceDto, NodeDto, GraphElementDto

@Serializable
data class EdgeInstanceDto(
    override val id: String,
    override val label: String,
    val properties: Set<@Contextual PropertyInstanceDto>,
    val source: String,
    val target: String,
) : InstanceDto, EdgeDto, GraphElementDto

@Serializable
data class PropertyInstanceDto(
    override val key: String,
    override val label: String,
    val value: String?,
): InstanceDto, PropertyDto

@Serializable
data class DataGraphDto(
    override val nodes: Set<NodeInstanceDto>,
    override val edges: Set<EdgeInstanceDto>
) : GraphDto









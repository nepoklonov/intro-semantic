package structure

import dto.ConvertibleToDto
import dto.ModelGraphDto
import elements.schema.model.EdgeRelation
import elements.schema.model.ModelNode
import utils.collections.MutableKeySet
import utils.collections.mutableKeySetOf

class ModelGraph(
    nodes: Collection<ModelNode> = mutableKeySetOf(),
    edges: Collection<EdgeRelation> = mutableKeySetOf(),
) : Graph<ModelNode, EdgeRelation>(nodes, edges), ConvertibleToDto<ModelGraphDto> {

    override val graphType = GraphType.Model

    override fun convert() = ModelGraphDto(
        nodes.map { it.convert() }.toSet(),
        edges.map { it.convert() }.toSet()
    )

    override fun equals(other: Any?): Boolean {
        if (other !is ModelGraph) return false
        return nodes == other.nodes && edges == other.edges
    }

    override fun hashCode(): Int {
        return this::class.hashCode()
    }

    override fun toString(): String = "nodes: $nodes\nedges: $edges"
}
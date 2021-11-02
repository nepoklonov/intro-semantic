package structure

import dto.ConvertibleToDto
import dto.DataGraphDto
import dto.ModelGraphDto
import elements.data.EdgeInstance
import elements.data.NodeInstance
import utils.collections.MutableKeySet
import utils.collections.mutableKeySetOf

class DataGraph(
    override var schema: Schema?,
    nodes: Collection<NodeInstance> = mutableKeySetOf(),
    edges: Collection<EdgeInstance> = mutableKeySetOf(),
) : Graph<NodeInstance, EdgeInstance>(nodes, edges), ConvertibleToDto<DataGraphDto> {

    override val graphType = GraphType.Data

    override fun convert() = DataGraphDto(
        nodes.map { it.convert() }.toSet(),
        edges.map { it.convert() }.toSet()
    )

    override fun equals(other: Any?): Boolean {
        if (other !is DataGraph) return false
        return nodes == other.nodes && edges == other.edges
    }

    override fun hashCode(): Int {
        return this::class.hashCode()
    }

    override fun toString(): String {
        val nodesString = nodes.joinToString(",\n") { "$it" }
        val edgesString = edges.joinToString(",\n") { "$it" }
        return "{nodes:\n$nodesString\nedges:\n$edgesString\n}}"
    }

    fun copy() = DataGraph(schema, nodes, edges)
}
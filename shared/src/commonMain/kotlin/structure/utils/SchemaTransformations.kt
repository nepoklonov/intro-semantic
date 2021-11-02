package structure.utils

import elements.schema.model.ModelNode
import elements.utils.copy
import structure.Fundamental
import structure.Schema

fun Fundamental.updateIdsBy(other: Fundamental) = Fundamental(
    nodeClasses = nodeClasses.map { it.copy(id = (other.nodeClasses[it.label] ?: it).id) },
    edgeClasses = edgeClasses.map { it.copy(id = (other.edgeClasses[it.label] ?: it).id) },
    propertyClasses = propertyClasses.map { it.copy(id = (other.propertyClasses[it.label] ?: it).id) },
)

fun Schema.updateIdsBy(other: Schema): Schema {
    val updatedFundamental = fundamental.updateIdsBy(other.fundamental)

    val updatedModelGraph = this.modelGraph.transform(
        transformNodes = { node: ModelNode ->
            val nodeClass = node.elementClass?.let { oldNodeClass ->
                updatedFundamental.nodeClasses[oldNodeClass.label]
            } ?: return@transform node
            node.copy(elementClass = nodeClass)
        }
    ) { edge ->
        val edgeClass = edge.elementClass?.let { oldEdgeClass ->
            updatedFundamental.edgeClasses[oldEdgeClass.label]
        } ?: return@transform edge
        edge.copy(elementClass = edgeClass)
    }
    return Schema(updatedFundamental, updatedModelGraph)
}
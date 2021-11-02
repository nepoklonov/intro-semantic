package structure

import elements.schema.fundamental.EdgeClass
import elements.schema.fundamental.NodeClass
import elements.schema.model.EdgeRelation
import elements.schema.model.ModelNode

data class NodeClassInfo(
    var modelNode: ModelNode? = null
) {
    val hasModelNode: Boolean get() = modelNode != null
}

data class EdgeClassInfo(
    val relations: MutableSet<EdgeRelation> = mutableSetOf()
)

//TODO создать listener, вынести в него функции
data class SchemaMatrix (
    val nodeClassInfo: MutableMap<String, NodeClassInfo> = mutableMapOf(),
    val edgeClassInfo: MutableMap<String, EdgeClassInfo> = mutableMapOf(),
) {
    fun onAddNodeClass(nodeClass: NodeClass) {
        nodeClassInfo[nodeClass.key] = NodeClassInfo()
    }
    fun onAddEdgeClass(edgeClass: EdgeClass) {
        edgeClassInfo[edgeClass.key] = EdgeClassInfo()
    }
    fun onRemoveNodeClass(nodeClass: NodeClass) {
        nodeClassInfo.remove(nodeClass.key)
    }
    fun onRemoveEdgeClass(edgeClass: EdgeClass) {
        edgeClassInfo.remove(edgeClass.key)
    }

    fun onAddModelNode(modelNode: ModelNode) {
        nodeClassInfo[modelNode.elementClass?.key]?.modelNode = modelNode
    }
    fun onAddEdgeRelation(edgeRelation: EdgeRelation) {
        edgeClassInfo[edgeRelation.elementClass?.key]?.relations?.add(edgeRelation)
    }
    fun onRemoveModelNode(modelNode: ModelNode) {
        nodeClassInfo[modelNode.elementClass?.key]?.modelNode = null
    }
    fun onRemoveEdgeRelation(edgeRelation: EdgeRelation) {
        edgeClassInfo[edgeRelation.elementClass?.key]?.relations?.remove(edgeRelation)
    }
}
package structure

import elements.data.EdgeInstance
import elements.data.NodeInstance
import elements.inheritanceEdgeClass
import utils.collections.mutableKeySetOf
import utils.collections.toMutableKeySet

class NodeInstanceInheritanceChain (
    override var schema: Schema?,
    nodes: List<NodeInstance>,
    edges: List<EdgeInstance>,
) : Graph<NodeInstance, EdgeInstance>(nodes.toMutableKeySet(), edges.toMutableKeySet()) {

    override val graphType = GraphType.Data

    init {
        check(edges.size == nodes.size - 1)
        nodes.asSequence()
            .zipWithNext()
            .all { (prev, current) -> current == prev.parent }
            .also(::check)
    }

    val idChain = NodeInstanceIdInheritanceChain(nodes.map { it.id })
}

class NodeInstanceIdInheritanceChain(val idList: List<String>){
    val lowestNodeId: String = idList.first()
    val tailIds: List<String> = idList.subList(1, idList.size)
}

fun NodeInstance.unfold(): NodeInstanceInheritanceChain {
    val propertyMap = properties
        .groupBy { it.modelElement.holderElementClass }
        .mapValues { it.value.toMutableKeySet() }
        .withDefault { mutableKeySetOf() }
    val nodeChain = generateSequence(modelElement) { it.parent }.map { modelNode ->
        NodeInstance(
            elementClass = modelNode.elementClass,
            properties = propertyMap.getValue(modelNode.elementClass!!),
            tangibility = tangibility
        )
    }.toList()
    val edgeChain = nodeChain.asSequence().zipWithNext().map { (source, target) ->
        EdgeInstance(source, target, inheritanceEdgeClass)
    }.toList()
    return NodeInstanceInheritanceChain(modelElement?.graph?.schema, nodeChain, edgeChain)
}

//fun NodeInstanceInheritanceChain.fold() = NodeInstance(
//    elementClass = nodes.first().elementClass,
//    properties = nodes.flatMap { it.properties }.toMutableKeySet()
//)
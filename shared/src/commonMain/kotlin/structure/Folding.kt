package structure

import elements.abstract.incomingEdges
import elements.abstract.outgoingEdges
import elements.data.EdgeInstance
import elements.data.NodeInstance
import elements.data.copy
import elements.inheritanceEdgeClass
import utils.collections.mutableKeySetOf
import utils.collections.toMutableKeySet

fun Graph<NodeInstance, EdgeInstance>.addInheritanceChain(
    chain: NodeInstanceInheritanceChain
): Pair<List<NodeInstance>, List<EdgeInstance>> {
    check(chain.schema == schema)
    val nodes = mutableListOf<NodeInstance>()
    val edges = mutableListOf<EdgeInstance>()
    val nodesMap = mutableMapOf<NodeInstance, NodeInstance>()
    //TODO нужна какая-то функция, которая могла бы это обобщить...
    chain.nodes.forEach { node -> nodes += addNode(node.copy())?.also { nodesMap[node] = it } ?: return@forEach }
    chain.edges.forEach { edge ->
        edges += addEdge(
            edge.copy(
                source = nodesMap.getValue(edge.source),
                target = nodesMap.getValue(edge.target)
            )
        ) ?: return@forEach
    }
    return nodes to edges
}

fun DataGraph.unfold(): UnfoldingResult {
    check(schema != null)
    //TODO проверять nodes.all { it.modelElement.schema == this.schema } (аналогично для рёбер)
    //TODO написать общую функцию для проверки согласованности
    val graph = DataGraph(schema)
    val sources = mutableMapOf<EdgeInstance, NodeInstance>()
    val targets = mutableMapOf<EdgeInstance, NodeInstance>()
    val nodeIdMap = mutableMapOf<String, NodeInstanceIdInheritanceChain>()
    val edgeIdMap = mutableMapOf<String, String>()
    nodes.forEach { node ->
        val chain = node.unfold()
        nodeIdMap[node.id] = chain.idChain
        val chainNodes = graph.addInheritanceChain(chain).first.associateBy { it.modelElement!! }
        node.outgoingEdges?.forEach { edge ->
            val modelEdge = edge.modelElement!!
            val source = chainNodes.getValue(modelEdge.source)
            sources[edge] = source
        }
        node.incomingEdges?.forEach { edge ->
            val modelEdge = edge.modelElement!!
            val target = chainNodes.getValue(modelEdge.target)
            targets[edge] = target
        }
    }
    edges.forEach { edge ->
        val newEdge = edge.copy(
            source = sources.getValue(edge),
            target = targets.getValue(edge)
        )
        graph.addEdge(
            edge = newEdge
        )
        edgeIdMap[edge.id] = newEdge.id
    }

    return UnfoldingResult(dataGraph = graph, idMap = IdMap(nodeIdMap, edgeIdMap))
}

fun DataGraph.fold(): DataGraph {
    val nodesToAdd = nodes.toMutableKeySet()
    val resultNodes = mutableKeySetOf<String, NodeInstance>()
    val sources = mutableMapOf<EdgeInstance, NodeInstance>()
    val targets = mutableMapOf<EdgeInstance, NodeInstance>()
    while (nodesToAdd.isNotEmpty()) {
        val startNode = nodesToAdd.first()
        val older = generateSequence(startNode.parent) { it.parent }
        val younger = generateSequence(startNode) { it.children?.singleOrNull() }
        val last: NodeInstance = younger.last()
        val properties = (older + younger).map { node ->
            nodesToAdd.remove(node)
            node.properties
        }.flatten()
        val foldedNode = NodeInstance(
            id = last.id,
            elementClass = last.elementClass,
            properties = properties.toList().toMutableKeySet()
        )
        (older + younger).forEach {
            it.outgoingEdges
                ?.asSequence()
                ?.filter { e -> e.elementClass != inheritanceEdgeClass }
                ?.forEach { edge -> sources[edge] = foldedNode }

            it.incomingEdges
                ?.asSequence()
                ?.filter { e -> e.elementClass != inheritanceEdgeClass }
                ?.forEach { edge -> targets[edge] = foldedNode }
        }
        resultNodes += foldedNode
    }
    val resultEdges = edges
        .asSequence()
        .filter { it.elementClass != inheritanceEdgeClass } //TODO вынести в отдельный метод
        .map { edge ->
            edge.copy(
                source = sources.getValue(edge),
                target = targets.getValue(edge)
            )
        }.toList()
        .toMutableKeySet()
    return DataGraph(schema, resultNodes, resultEdges)
}

class UnfoldingResult(
    val dataGraph: DataGraph,
    val idMap: IdMap
)

class IdMap(
    nodeIdMap: Map<String, NodeInstanceIdInheritanceChain>,
    edgeIdMap: Map<String, String>
) {
    private val _nodeIdMap: MutableMap<String, NodeInstanceIdInheritanceChain> = nodeIdMap.toMutableMap()
    private val _edgeIdMap: MutableMap<String, String> = edgeIdMap.toMutableMap()

    val nodeIdMap get() = _nodeIdMap.toMap()
    val edgeIdMap get() = _edgeIdMap.toMap()

    fun putAllEdgeIds(map: Map<String, String>) = _edgeIdMap.putAll(map)
}
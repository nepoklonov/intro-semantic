package structure.utils

import elements.abstract.Edge
import elements.abstract.Node
import elements.data.EdgeInstance
import elements.data.NodeInstance
import elements.data.copy
import elements.schema.fundamental.EdgeClass
import elements.schema.fundamental.NodeClass
import elements.schema.model.EdgeRelation
import elements.schema.model.ModelNode
import elements.utils.copy
import structure.DataGraph
import structure.Graph
import structure.ModelGraph

@Suppress("UNCHECKED_CAST")
fun <N : Node<N, E>, E : Edge<N, E>> N.copy(
    elementClass: NodeClass? = this.elementClass
): N = when (this) {
    is ModelNode -> copy(elementClass)
    is NodeInstance -> copy(elementClass)
    else -> error("Wrong graph element type")
} as N

@Suppress("UNCHECKED_CAST")
fun <N : Node<N, E>, E : Edge<N, E>> E.copy(
    source: N = this.source,
    target: N = this.target,
    elementClass: EdgeClass? = this.elementClass
): E = when (this) {
    is EdgeRelation -> copy(source as ModelNode, target as ModelNode, elementClass)
    is EdgeInstance -> copy(source as NodeInstance, target as NodeInstance, elementClass)
    else -> error("Wrong graph element type")
} as E

@Suppress("UNCHECKED_CAST")
fun <G : Graph<N, E>, N : Node<N, E>, E : Edge<N, E>> G.transform(
    transformNodes: (N) -> N? = { it },
    transformEdges: (E) -> E? = { it }
): G {
    val nodesMap = nodes.associateWith { transformNodes(it.copy()) }
    val transformedGraph = when (this) {
        is ModelGraph -> ModelGraph(nodes = nodesMap.values as Collection<ModelNode>)
        is DataGraph -> DataGraph(
            schema,
            nodes = nodesMap.values.filterNotNull() as Collection<NodeInstance>
        )
        else -> error("Wrong graph type")
    } as G

    val transformedEdges = edges.mapNotNull { edge ->
        val newEdge = transformEdges(edge.copy()) ?: return@mapNotNull null
        val newSource = nodesMap.getValue(newEdge.source) ?: return@mapNotNull null
        val newTarget = nodesMap.getValue(newEdge.target) ?: return@mapNotNull null
        newEdge.copy(source = newSource, target = newTarget)
    }

    transformedGraph.addEdges(transformedEdges)
    return transformedGraph
}


//TODO вероятно, здесь нужно генерировать событие
inline fun <N : Node<N, E>, E : Edge<N, E>> Graph<N, E>.transformNode(original: N, transform: (N) -> N?): N? {
    val connectedEdges = original.connectedEdges?.toList() ?: emptyList()
    removeNode(original)
    val transformedNode = transform(original) ?: return null
    addNode(transformedNode)
    val edgesToRestore = connectedEdges.map { edge ->
        val newSource = if (edge.source == original) transformedNode else edge.source
        val newTarget = if (edge.target == original) transformedNode else edge.target
        edge.copy(source = newSource, target = newTarget)
    }
    addEdges(edgesToRestore)
    return transformedNode
}

//TODO здесь, вероятно, тоже
inline fun <N : Node<N, E>, E : Edge<N, E>> Graph<N, E>.transformEdge(original: E, transform: (E) -> E?): E? {
    removeEdge(original)
    val transformedEdge = transform(original) ?: return null
    addEdge(transformedEdge)
    return transformedEdge
}
package structure

import elements.abstract.Edge
import elements.abstract.Node

//maybe add default values
class NodeInfo<N : Node<N, E>, E : Edge<N, E>>(
    val connectedEdges: MutableSet<E> = mutableSetOf(),
    var parent: N? = null,
    val children: MutableSet<N> = mutableSetOf()
)

class EdgeInfo<N : Node<N, E>, E : Edge<N, E>>

class GraphMatrix<N : Node<N, E>, E : Edge<N, E>>(
    val nodeInfo: MutableMap<String, NodeInfo<N, E>> = mutableMapOf(),
    val edgeInfo: MutableMap<String, EdgeInfo<N, E>> = mutableMapOf(),
)
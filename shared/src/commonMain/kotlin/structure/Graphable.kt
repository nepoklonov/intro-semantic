package structure

import elements.abstract.Edge
import elements.abstract.Node

interface Graphable<N : Node<N, E>, E : Edge<N, E>> {

    fun addNode(node: N): N?

    fun addEdge(edge: E): E?

    fun addNodes(nodes: Collection<N>): Collection<N> {
        return nodes.mapNotNull { addNode(it) }
    }

    fun addEdges(edges: Collection<E>): Collection<E> {
        return edges.mapNotNull { addEdge(it) }
    }

    fun removeNode(node: N): GraphElementSet<N, E>?

    fun removeEdge(edge: E): E?

    fun reverseEdge(edge: E): E?
}
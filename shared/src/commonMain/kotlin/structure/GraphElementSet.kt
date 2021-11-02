package structure

import elements.abstract.Edge
import elements.abstract.Node
import utils.collections.KeySet
import utils.collections.toKeySet

interface GraphElementSet<N : Node<N, E>, E : Edge<N, E>> {
    val nodes: KeySet<String, N>
    val edges: KeySet<String, E>
}

private class GraphElementSetImpl<N : Node<N, E>, E : Edge<N, E>>(
    override val nodes: KeySet<String, N>,
    override val edges: KeySet<String, E>
) : GraphElementSet<N, E>

fun <N : Node<N, E>, E : Edge<N, E>> graphElementSetOf(nodes: List<N>, edges: List<E>): GraphElementSet<N, E> =
    GraphElementSetImpl(nodes.toKeySet(), edges.toKeySet())
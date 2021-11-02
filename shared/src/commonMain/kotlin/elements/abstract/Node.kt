package elements.abstract

import elements.GraphElement
import elements.PlatformNode
import elements.inheritanceEdgeClass
import elements.schema.fundamental.NodeClass
import structure.graphInfo

interface Node<N : Node<N, E>, E : Edge<N, E>> : PlatformNode<N, E>, GraphElement<N, E> {
    override val elementClass: NodeClass?
    val connectedEdges: Set<E>? get() = graphInfo?.connectedEdges
    val parent: N? get() = graphInfo?.parent
    val children: Set<N>? get() = graphInfo?.children
    override fun relatedEntities(): Pair<Set<N>, Set<E>>? {
        return adjacentNodes?.to(connectedEdges ?: return null)
    }
}

val <N : Node<N, E>, E: Edge<N, E>> Node<N, E>.parentEdge: E?
    get() = outgoingEdges?.single { it.elementClass == inheritanceEdgeClass }

val <N : Node<N, E>, E : Edge<N, E>> Node<N, E>.outgoingEdges: Set<E>?
    get() = connectedEdges?.filter { it.source == this }?.toSet()

val <N : Node<N, E>, E : Edge<N, E>> Node<N, E>.incomingEdges: Set<E>?
    get() = connectedEdges?.filter { it.target == this }?.toSet()

val <N : Node<N, E>, E : Edge<N, E>> Node<N, E>.outgoingNodes: Set<N>?
    get() = outgoingEdges?.map { it.target }?.toSet()

val <N : Node<N, E>, E : Edge<N, E>> Node<N, E>.incomingNodes: Set<N>?
    get() = incomingEdges?.map { it.source }?.toSet()

val <N : Node<N, E>, E : Edge<N, E>> Node<N, E>.adjacentNodes: Set<N>?
    get() {
        return outgoingNodes?.plus(incomingNodes ?: return null)
    }


package elements.abstract

import elements.GraphElement
import elements.PlatformEdge
import elements.inheritanceEdgeClass
import elements.schema.fundamental.EdgeClass

interface Edge<N : Node<N, E>, E: Edge<N, E>> : PlatformEdge<N, E>, GraphElement<N, E> {
    override val elementClass: EdgeClass?
    val source: N
    val target: N
    fun createReversed(): E

    override fun relatedEntities(): Pair<Set<N>, Set<E>> =
        setOf(source, target) to emptySet()
}

val Edge<*, *>.hasLabel get() = elementClass != inheritanceEdgeClass

val <N : Node<N, E>, E: Edge<N, E>> Edge<N, E>.coDirectedEdges: Set<E>?
    get() = source.outgoingEdges?.filter { it.target == target }?.toSet()

val <N : Node<N, E>, E: Edge<N, E>> Edge<N, E>.counterDirectedEdges: Set<E>?
    get() = target.outgoingEdges?.filter { it.target == source }?.toSet()

val <N : Node<N, E>, E: Edge<N, E>> Edge<N, E>.parallelEdges: Set<E>?
    get() {
        return coDirectedEdges?.plus(counterDirectedEdges ?: return null)
    }

//TODO check the need
val Edge<*, *>.coDirectedIndex: Int
    get() = coDirectedEdges?.indexOf(this) ?: 0


//TODO: remove shadowing
val Edge<*, *>.isSimple: Boolean
    get() = (parallelEdges?.count() ?: 1) == 1
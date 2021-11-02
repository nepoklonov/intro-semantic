package events.appliers

import elements.abstract.Edge
import elements.abstract.Node
import events.*
import structure.Graph

abstract class EventApplier<N : Node<N, E>, E : Edge<N, E>> {
    abstract val graph: Graph<N, E>

    fun applyEvent(event: Event): Boolean =
        if (event is CompositeEvent) {
            !event.atomicEvents.map { applyAtomicEvent(it) }.contains(false)
        } else {
            applyAtomicEvent(event as AtomicEvent)
        }

    protected abstract fun applyAtomicEvent(event: AtomicEvent): Boolean

    protected fun <T: ReverseEdgeEvent> applyReverseEdgeEvent(event: T) =
        graph.edges[event.elementKey]?.let { edge ->
            graph.reverseEdge(edge)
            true
        } ?: false

    protected fun removeFromGraph (event: RemoveEvent): Boolean {
        val id = event.elementKey
        return when (event.elementForm) {
            ElementForm.NODE -> graph.nodes[id]?.let {
                graph.removeNode(it)
                true
            } ?: false
            ElementForm.EDGE -> graph.edges[id]?.let {
                graph.removeEdge(it)
                true
            } ?: false
            else -> throw IllegalArgumentException()
        }
    }
}
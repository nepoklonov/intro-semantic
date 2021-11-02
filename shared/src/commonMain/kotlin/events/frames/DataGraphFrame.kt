package events.frames

import elements.DataGraphElement
import elements.data.EdgeInstance
import elements.data.NodeInstance
import elements.data.PropertyInstance
import events.*
import events.appliers.DataGraphEventApplier
import events.eventlines.DataGraphEventLine
import events.eventmakers.DataGraphEventMaker
import events.reversers.DataGraphEventReverser
import structure.DataGraph

class DataGraphFrame(override val structure: DataGraph) : Frame<NodeInstance, EdgeInstance> {

    override val eventLine: DataGraphEventLine = DataGraphEventLine(DataGraphEventApplier(structure))

    private val eventMaker: DataGraphEventMaker = DataGraphEventMaker()

    override fun addNode(node: NodeInstance) = structure.addNode(node)?.also {
        val event = eventMaker.makeAddEvent(it)
        putEvent(event)
    }

    override fun addEdge(edge: EdgeInstance) = structure.addEdge(edge)?.also {
        val event = eventMaker.makeAddEvent(it)
        putEvent(event)
    }

    override fun removeNode(node: NodeInstance) = structure.removeNode(node)?.also { removedElements ->
        val event = eventMaker.makeRemoveEvent(removedElements)
        if (event != null) putEvent(event)
    }

    override fun removeEdge(edge: EdgeInstance) = structure.removeEdge(edge)?.also {
        val event = eventMaker.makeRemoveEvent(it)
        putEvent(event)
    }

    override fun reverseEdge(edge: EdgeInstance) = structure.reverseEdge(edge)?.also {
        val event = eventMaker.makeReverseEvent(it)
        putEvent(event)
    }

    fun <T : DataGraphElement> changeElement(
        element: T,
        propertiesToAdd: List<PropertyInstance> = listOf(),
        propertiesToRemove: List<PropertyInstance> = listOf()
    ) {
        val event = eventMaker.makeChangeEvent(element, propertiesToAdd, propertiesToRemove)
        eventLine.addAndApply(event)
    }

    private fun putEvent(eventPair: EventPair<*, *>) = eventLine.addEvent(eventPair)
}
package events.eventmakers

import dto.EdgeInstanceDto
import dto.NodeInstanceDto
import elements.DataGraphElement
import elements.data.EdgeInstance
import elements.data.NodeInstance
import elements.data.PropertyInstance
import events.*
import events.reversers.DataGraphEventReverser
import structure.GraphElementSet

class DataGraphEventMaker {

    private val reverser = DataGraphEventReverser()

    fun makeAddEvent(element: NodeInstance): EventPair<AddEvent<NodeInstance, NodeInstanceDto>, RemoveDataElementEvent> {
        val originalEvent = AddEvent(element)
        val reversedEvent = reverser.reverseAddEvent(originalEvent)
        return EventPair(originalEvent, reversedEvent)
    }

    fun makeAddEvent(element: EdgeInstance): EventPair<AddEvent<EdgeInstance, EdgeInstanceDto>, RemoveDataElementEvent> {
        val originalEvent = AddEvent(element)
        val reversedEvent = reverser.reverseAddEvent(originalEvent)
        return EventPair(originalEvent, reversedEvent)
    }

    fun makeRemoveEvent(elementSet: GraphElementSet<NodeInstance, EdgeInstance>): EventPair<CompositeEvent, CompositeEvent>? {
        val edgeEvents = elementSet.edges.map { RemoveDataElementEvent(it) }
        val nodeEvents = elementSet.nodes.map { RemoveDataElementEvent(it) }
        val reversedEdgeEvents = elementSet.edges.map { reverser.reverseRemoveEvent(it) }
        val reversedNodeEvents = elementSet.nodes.map { reverser.reverseRemoveEvent(it) }

        val originalComposite = CompositeEvent(edgeEvents + nodeEvents)
        val reversedComposite = CompositeEvent(reversedNodeEvents + reversedEdgeEvents)

        return if (originalComposite.atomicEvents.isNotEmpty()) EventPair(
            originalComposite,
            reversedComposite
        ) else null
    }

    fun makeRemoveEvent(edgeInstance: EdgeInstance): EventPair<RemoveDataElementEvent, AddEvent<EdgeInstance, EdgeInstanceDto>> {
        val originalEvent = RemoveDataElementEvent(edgeInstance)
        val reversedEvent = reverser.reverseRemoveEvent(edgeInstance)
        return EventPair(originalEvent, reversedEvent)
    }

    fun makeReverseEvent(edgeInstance: EdgeInstance): EventPair<ReverseEdgeInstanceEvent, ReverseEdgeInstanceEvent> {
        val originalEvent = ReverseEdgeInstanceEvent(edgeInstance)
        val reversedEvent = reverser.reverseReverseEvent(originalEvent)
        return EventPair(originalEvent, reversedEvent)
    }


    fun <T : DataGraphElement> makeChangeEvent(
        element: T,
        propertiesToAdd: List<PropertyInstance>,
        propertiesToRemove: List<PropertyInstance>
    ): EventPair<ChangeDataElementEvent, ChangeDataElementEvent> {
        val originalEvent = ChangeDataElementEvent(
            element = element,
            addedProperties = propertiesToAdd,
            removedProperties = propertiesToRemove,
        )
        val reversedEvent = reverser.reverseChangeEvent(originalEvent, element)
        return EventPair(originalEvent, reversedEvent)
    }

}
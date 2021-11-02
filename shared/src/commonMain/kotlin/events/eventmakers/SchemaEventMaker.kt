package events.eventmakers

import dto.ConvertibleToDto
import dto.EdgeRelationDto
import dto.ElementClassDto
import dto.ModelElementDto
import elements.ElementClass
import elements.ElementClassPropertyHolder
import elements.ModelElement
import elements.SchemaElement
import elements.schema.fundamental.PropertyClass
import elements.schema.model.EdgeRelation
import elements.schema.model.ModelNode
import elements.schema.model.PropertyRelation
import events.*
import events.reversers.SchemaEventReverser
import structure.*

class SchemaEventMaker {
    private val reverser = SchemaEventReverser()

    fun <T, X> makeAddEvent(elementClass: T): EventPair<AddEvent<T, X>, RemoveElementClassEvent> where
            T : ElementClass, T : ConvertibleToDto<X>, X : ElementClassDto {
        val original = AddEvent(elementClass)
        val reversed = reverser.reverseAddEvent(original) as RemoveElementClassEvent
        return EventPair(original, reversed)
    }

    fun makeAddEvent(result: AddModelElementResult<*, *, *, *>): EventPair<CompositeEvent, CompositeEvent>? {
        val eventLists = addResultToEventListPair(result)
        return if (eventLists.originalList.isNotEmpty()) {
            val original = CompositeEvent(eventLists.originalList)
            val reversed = CompositeEvent(eventLists.reversedList)
            return EventPair(original, reversed)
        } else null
    }

    fun makeRemoveEvent(result: RemoveNodeClassResult): EventPair<CompositeEvent, CompositeEvent>? {
        val originalComposite = CompositeEvent()
        val reversedComposite = CompositeEvent()

        val modelElementListPair = result.modelElements
            ?.let { removedElementSetToEventListPair(it) }
            ?.also { originalComposite.addEvents(it.originalList) }

        //TODO убрать дублирование кода
        result.nodeClass
            ?.let { removedElementClassToEventPair(it) }
            ?.also {
                originalComposite.addEvent(it.originalEvent)
                reversedComposite.addEvent(it.reversedEvent)
            }

        if (modelElementListPair != null) reversedComposite.addEvents(modelElementListPair.reversedList)

        return if (originalComposite.atomicEvents.isNotEmpty()) EventPair(
            originalComposite,
            reversedComposite
        ) else null
    }

    fun makeRemoveEvent(result: RemoveEdgeClassResult): EventPair<CompositeEvent, CompositeEvent>? {
        val originalComposite = CompositeEvent()
        val reversedComposite = CompositeEvent()

        val edgeRelationEventListPair = result.edgeRelations
            ?.let { removedEdgeRelationsToEventListPair(it) }
            ?.also { originalComposite.addEvents(it.originalList) }

        result.edgeClass
            ?.let { removedElementClassToEventPair(it) }
            ?.also {
                originalComposite.addEvent(it.originalEvent)
                reversedComposite.addEvent(it.reversedEvent)
            }

        if (edgeRelationEventListPair != null) reversedComposite.addEvents(edgeRelationEventListPair.reversedList)

        return if (originalComposite.atomicEvents.isNotEmpty()) EventPair(
            originalComposite,
            reversedComposite
        ) else null
    }

    private fun removedEdgeRelationsToEventListPair(list: List<EdgeRelation>)
            : EventListPair<RemoveModelElementEvent, AddEvent<EdgeRelation, EdgeRelationDto>> {
        val originalEvents = list.map { RemoveModelElementEvent(it) }
        val reversedEvents = list.map { reverser.reverseRemoveModelElementEvent(it) }
        return EventListPair(originalEvents, reversedEvents)
    }

    fun makeRemoveEvent(result: RemovePropertyClassResult): EventPair<CompositeEvent, CompositeEvent>? {
        val originalComposite = CompositeEvent()
        val reversedComposite = CompositeEvent()

        val propertyRelationEventListPair = result.propertyRelations
            ?.let { removedPropertyRelationsToEventListPair(it) }
            ?.also { originalComposite.addEvents(it.originalList) }

        result.propertyClass
            ?.let { removedElementClassToEventPair(it) }
            ?.also {
                originalComposite.addEvent(it.originalEvent)
                reversedComposite.addEvent(it.reversedEvent)
            }

        if (propertyRelationEventListPair != null) reversedComposite.addEvents(propertyRelationEventListPair.reversedList)

        return if (originalComposite.atomicEvents.isNotEmpty()) EventPair(
            originalComposite,
            reversedComposite
        ) else null
    }

    private fun removedPropertyRelationsToEventListPair(list: List<PropertyRelation>)
            : EventListPair<ChangeElementClassPropertyHolderEvent, ChangeElementClassPropertyHolderEvent> {
        val originalEvents = mutableListOf<ChangeElementClassPropertyHolderEvent>()
        val reversedEvents = mutableListOf<ChangeElementClassPropertyHolderEvent>()

        list.forEach {
            val holder = it.holderElementClass
            val event = ChangeElementClassPropertyHolderEvent(
                element = holder,
                removedProperties = listOf(it)
            )
            val reversedEvent = reverser.reverseChangeElementClassPropertyHolderEvent(event, holder)
            originalEvents.add(event)
            reversedEvents.add(reversedEvent)
        }

        return EventListPair(originalEvents, reversedEvents)
    }

    private fun <T, X> removedElementClassToEventPair(elementClass: T): EventPair<RemoveElementClassEvent, AddEvent<T, X>>
            where T : ElementClass, T : ConvertibleToDto<X>, X : ElementClassDto {
        val originalEvent = RemoveElementClassEvent(elementClass)
        val reversedEvent = reverser.reverseRemoveElementClassEvent(elementClass)
        return EventPair(originalEvent, reversedEvent)
    }

    fun makeRemoveEvent(elementSet: GraphElementSet<ModelNode, EdgeRelation>): EventPair<CompositeEvent, CompositeEvent>? {
        val sortedEvents = removedElementSetToEventListPair(elementSet)
        return if (sortedEvents.originalList.isNotEmpty()) EventPair(
            CompositeEvent(sortedEvents.originalList),
            CompositeEvent(sortedEvents.reversedList)
        ) else null
    }

    fun makeRemoveEvent(edgeRelation: EdgeRelation): EventPair<RemoveModelElementEvent, AddEvent<EdgeRelation, EdgeRelationDto>> {
        val original = RemoveModelElementEvent(edgeRelation)
        val reversed = reverser.reverseRemoveModelElementEvent(edgeRelation)
        return EventPair(original, reversed)
    }

    fun makeReverseEvent(edgeRelation: EdgeRelation): EventPair<ReverseEdgeRelationEvent, ReverseEdgeRelationEvent> {
        val original = ReverseEdgeRelationEvent(edgeRelation)
        val reversed = reverser.reverseReverseEvent(original)
        return EventPair(original, reversed)
    }

    fun makeChangeEvent(
        element: SchemaElement,
        customProperties: Map<String, Any?>
    ): EventPair<ChangeSchemaElementEvent, ChangeSchemaElementEvent> {
        val event = when (element) {
            is PropertyClass -> ChangeElementClassEvent(element, customProperties)
            is PropertyRelation -> ChangePropertyRelationEvent(element, customProperties)
            is ModelElement -> ChangeModelElementEvent(element, customProperties)
            else -> throw IllegalArgumentException()
        }
        val reversed = reverser.reverseChangeEvent(event, element)
        return EventPair(event, reversed)
    }


    fun makeChangeElementClassPropertyHolderEvent(
        element: ElementClassPropertyHolder,
        propertiesToAdd: List<PropertyRelation>,
        propertiesToRemove: List<PropertyRelation>,
        customProperties: Map<String, Any?>
    ): EventPair<ChangeElementClassPropertyHolderEvent, ChangeElementClassPropertyHolderEvent> {
        val original = ChangeElementClassPropertyHolderEvent(
            element = element,
            addedProperties = propertiesToAdd,
            removedProperties = propertiesToRemove,
            customProperties = customProperties
        )
        val reversed = reverser.reverseChangeElementClassPropertyHolderEvent(original, element)
        return EventPair(original, reversed)
    }

    private fun <R : AddModelElementResult<F, FD, M, MD>, F, FD : ElementClassDto, M, MD : ModelElementDto>
            addResultToEventListPair(result: R): EventListPair<AddEvent<*, *>, RemoveEvent>
            where F : ConvertibleToDto<FD>, F : ElementClass, M : ConvertibleToDto<MD>, M : ModelElement {
        val originalEventList = mutableListOf<AddEvent<*, *>>()
        val reversedEventList = mutableListOf<RemoveEvent>()
        result.elementClass?.let {
            val originalEvent = AddEvent(it)
            val reversedEvent = reverser.reverseAddEvent(originalEvent)
            originalEventList.add(originalEvent)
            reversedEventList.add(reversedEvent)
        }
        result.modelElement?.let {
            val originalEvent = AddEvent(it)
            val reversedEvent = reverser.reverseAddEvent(originalEvent)
            originalEventList.add(originalEvent)
            reversedEventList.add(reversedEvent)
        }
        return EventListPair(originalEventList, reversedEventList.reversed())
    }

    private fun removedElementSetToEventListPair(elementSet: GraphElementSet<ModelNode, EdgeRelation>):
            EventListPair<RemoveModelElementEvent, AddEvent<*, *>> {
        val edgeEvents = elementSet.edges.map { RemoveModelElementEvent(it) }
        val nodeEvents = elementSet.nodes.map { RemoveModelElementEvent(it) }
        val reversedEdgeEvents = elementSet.edges.map { reverser.reverseRemoveModelElementEvent(it) }
        val reversedNodeEvents = elementSet.nodes.map { reverser.reverseRemoveModelElementEvent(it) }
        return EventListPair(edgeEvents + nodeEvents, reversedNodeEvents + reversedEdgeEvents)
    }
}

private data class EventListPair<O : Event, R : Event>(
    val originalList: List<O>,
    val reversedList: List<R>
)
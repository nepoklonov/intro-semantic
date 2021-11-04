package events

import dto.*
import elements.*
import elements.abstract.Edge
import elements.abstract.Node
import elements.data.EdgeInstance
import elements.data.NodeInstance
import elements.data.PropertyInstance
import elements.schema.fundamental.EdgeClass
import elements.schema.fundamental.NodeClass
import elements.schema.fundamental.PropertyClass
import elements.schema.model.EdgeRelation
import elements.schema.model.ModelNode
import elements.schema.model.PropertyRelation
import events.ElementForm.*
import kotlinx.serialization.Serializable

interface Event

interface AtomicEvent : Event {
    val elementKey: String
    val elementForm: ElementForm
}

class CompositeEvent(events: List<AtomicEvent> = emptyList()) : Event {

    private val _atomicEvents: MutableList<AtomicEvent> = events.toMutableList()
    val atomicEvents: List<AtomicEvent>
        get() = _atomicEvents

    fun addEvent(atomicEvent: AtomicEvent) = _atomicEvents.add(atomicEvent)
    fun removeEvent(atomicEvent: AtomicEvent) = _atomicEvents.remove(atomicEvent)

    fun addEvents(events: Collection<AtomicEvent>) = events.forEach { addEvent(it) }
}

data class EventPair<O : Event, R : Event>(
    val originalEvent: O,
    val reversedEvent: R
)

//TODO keep it optimized
class Mutation(mixedEvents: List<Event>) {
    private val optimizedEvents = mutableMapOf<String, AtomicEvent>()

    init {
        mixedEvents.mapNotNull {
            when (it) {
                is CompositeEvent -> it.atomicEvents
                is AtomicEvent -> listOf(it)
                else -> null
            }
        }.flatten().forEach {
            if (optimizedEvents.containsKey(it.elementKey)) {
                when (val oldEvent = optimizedEvents[it.elementKey]) {
                    is AddEvent<*, *> -> when (it) {
                        is RemoveEvent -> optimizedEvents.remove(it.elementKey)
                        is ChangeEvent -> {
                            val changeEvent = it as ChangeDataElementEvent
                            when (oldEvent.elementForm) {
                                NODE -> {
                                    // Mutation works only with DataGraph Events, so cast is safe
                                    @Suppress("UNCHECKED_CAST")
                                    val addNodeEvent = oldEvent as AddEvent<NodeInstance, NodeInstanceDto>

                                    val newProperties = addNodeEvent.dto.properties.filter { property ->
                                        !changeEvent.propertyChanges.propertyKeysToRemove.contains(property.key)
                                    }.union(changeEvent.propertyChanges.propertiesToAdd).toSet()

                                    val newAddNodeEvent = AddEvent<NodeInstance, NodeInstanceDto>(
                                        elementKey = addNodeEvent.elementKey,
                                        elementForm = addNodeEvent.elementForm,
                                        dto = NodeInstanceDto(
                                            id = addNodeEvent.dto.id,
                                            label = addNodeEvent.dto.label,
                                            properties = newProperties
                                        )
                                    )

                                    optimizedEvents[it.elementKey] = newAddNodeEvent
                                }
                                EDGE -> {
                                    // Mutation works only with DataGraph Events, so cast is safe
                                    @Suppress("UNCHECKED_CAST")
                                    val addEdgeEvent = oldEvent as AddEvent<EdgeInstance, EdgeInstanceDto>

                                    val newProperties = addEdgeEvent.dto.properties.filter { property ->
                                        !changeEvent.propertyChanges.propertyKeysToRemove.contains(property.key)
                                    }.union(changeEvent.propertyChanges.propertiesToAdd).toSet()

                                    val newAddEdgeEvent = AddEvent<NodeInstance, NodeInstanceDto>(
                                        elementKey = addEdgeEvent.elementKey,
                                        elementForm = addEdgeEvent.elementForm,
                                        dto = NodeInstanceDto(
                                            id = addEdgeEvent.dto.id,
                                            label = addEdgeEvent.dto.label,
                                            properties = newProperties
                                        )
                                    )

                                    optimizedEvents[it.elementKey] = newAddEdgeEvent
                                }
                                PROPERTY -> { } // TODO
                            }

                        }
                    }
                    is ChangeEvent -> optimizedEvents[it.elementKey] = it
                }
            } else {
                optimizedEvents[it.elementKey] = it
            }
        }
    }

    val events // Only atomic events
        get() = optimizedEvents.values.toList()

    val addEvents = events.filterIsInstance<AddEvent<*, *>>()
    val removeEvents = events.filterIsInstance<RemoveEvent>()
    val changeEvents = events.filterIsInstance<ChangeEvent>()
    val reverseEdgeEvents = events.filterIsInstance<ReverseEdgeEvent>()

}

private fun getElementForm(element: OverallElement) =
    when (element) {
        is ElementClass -> when (element) {
            is NodeClass -> NODE
            is EdgeClass -> EDGE
            is PropertyClass -> PROPERTY
            else -> throw IllegalArgumentException()
        }
        is Node<*, *> -> NODE
        is Edge<*, *> -> EDGE
        else -> throw IllegalArgumentException()
    }

data class AddEvent<T, X>(
    override val elementKey: String,
    override val elementForm: ElementForm,
    val dto: X,
) : AtomicEvent where T : ConvertibleToDto<X>, T : OverallElement, X : OverallElementDto {
    constructor(element: T) : this(
        elementKey = element.key,
        elementForm = getElementForm(element),
        dto = element.convert()
    )
}

interface RemoveEvent : AtomicEvent

data class RemoveElementClassEvent(
    override val elementKey: String,
    override val elementForm: ElementForm
) : RemoveEvent {
    constructor(element: ElementClass) : this(
        elementKey = element.key,
        elementForm = getElementForm(element)
    )
}

data class RemoveModelElementEvent(
    override val elementKey: String,
    override val elementForm: ElementForm
) : RemoveEvent {
    constructor(element: GraphElement<ModelNode, EdgeRelation>) : this(
        elementKey = element.key,
        elementForm = getElementForm(element)
    )
}

data class RemoveDataElementEvent(
    override val elementKey: String,
    override val elementForm: ElementForm
) : RemoveEvent {
    constructor(element: GraphElement<NodeInstance, EdgeInstance>) : this(
        elementKey = element.key,
        elementForm = getElementForm(element)
    )
}

interface ChangeEvent : AtomicEvent

interface ChangePropertyHolderEvent<X : PropertyDto> : ChangeEvent {
    val propertyChanges: PropertyChanges<X>
}

interface ChangeSchemaElementEvent : ChangeEvent {
    val customProperties: Map<String, Any?>
}

open class ChangeElementClassEvent(
    override val elementKey: String,
    override val elementForm: ElementForm,
    override val customProperties: Map<String, Any?>,
) : ChangeSchemaElementEvent {
    constructor(element: ElementClass, customProperties: Map<String, Any?> = mapOf()) : this(
        elementKey = element.key,
        elementForm = getElementForm(element),
        customProperties = customProperties
    )
}

data class ChangeElementClassPropertyHolderEvent(
    override val elementKey: String,
    override val elementForm: ElementForm,
    override val propertyChanges: PropertyChanges<PropertyRelationDto> = PropertyChanges(),
    override val customProperties: Map<String, Any?> = mapOf(),
) : ChangeElementClassEvent(elementKey, elementForm, customProperties),
    ChangePropertyHolderEvent<PropertyRelationDto> {
    constructor(
        element: ElementClassPropertyHolder,
        addedProperties: Collection<PropertyRelation> = emptyList(),
        removedProperties: Collection<PropertyRelation> = emptyList(),
        customProperties: Map<String, Any?> = mapOf()
    ) : this(
        elementKey = element.key,
        elementForm = getElementForm(element),
        propertyChanges = PropertyChanges(
            addedProperties.map { it.convert() },
            removedProperties.map { it.key }
        ),
        customProperties = customProperties
    )
}

open class ChangeModelElementEvent(
    override val elementKey: String,
    override val elementForm: ElementForm,
    override val customProperties: Map<String, Any?>,
) : ChangeSchemaElementEvent {
    constructor(element: ModelElement, customProperties: Map<String, Any?> = mapOf()) : this(
        elementKey = element.key,
        elementForm = getElementForm(element),
        customProperties = customProperties
    )
}

//???
data class ChangePropertyRelationEvent(
    override val elementKey: String,
    val holderKey: String,
    val holderForm: ElementForm,
    override val customProperties: Map<String, Any?>,
) : ChangeModelElementEvent(elementKey, PROPERTY, customProperties) {
    override val elementForm: ElementForm = PROPERTY

    constructor(element: PropertyRelation, customProperties: Map<String, Any?> = mapOf()) : this(
        elementKey = element.key,
        holderKey = element.holderElementClass.key,
        holderForm = if (element.holderElementClass is NodeClass) NODE else EDGE,
        customProperties = customProperties
    )
}

data class ChangeDataElementEvent(
    override val elementKey: String,
    override val elementForm: ElementForm,
    override val propertyChanges: PropertyChanges<PropertyInstanceDto> = PropertyChanges()
) : ChangePropertyHolderEvent<PropertyInstanceDto> {
    constructor(
        element: GraphElement<NodeInstance, EdgeInstance>,
        addedProperties: Collection<PropertyInstance> = emptyList(),
        removedProperties: Collection<PropertyInstance> = emptyList(),
    ) : this(
        elementKey = element.key,
        elementForm = getElementForm(element),
        propertyChanges = PropertyChanges(
            addedProperties.map { it.convert() },
            removedProperties.map { it.key }
        )
    )
}

abstract class ReverseEdgeEvent : AtomicEvent {
    override val elementForm = EDGE
}

class ReverseEdgeRelationEvent(override val elementKey: String) : ReverseEdgeEvent() {
    constructor(edgeRelation: EdgeRelation) : this(edgeRelation.id)
}

class ReverseEdgeInstanceEvent(override val elementKey: String) : ReverseEdgeEvent() {
    constructor(edgeInstance: EdgeInstance) : this(edgeInstance.id)
}

enum class ElementForm {
    NODE,
    EDGE,
    PROPERTY
}

data class PropertyChanges<X : PropertyDto>(
    val propertiesToAdd: List<X> = emptyList(),
    val propertyKeysToRemove: List<String> = emptyList()
) {
    val isEmpty get() = propertiesToAdd.isEmpty() && propertyKeysToRemove.isEmpty()
}

fun <T : AtomicEvent> List<T>.nodeEvents() = filter { it.elementForm == NODE }
fun <T : AtomicEvent> List<T>.edgeEvents() = filter { it.elementForm == EDGE }



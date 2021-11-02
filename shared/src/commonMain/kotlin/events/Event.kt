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
class Mutation(
    val events: List<Event>,
) {
    private val atomicEvents
        get() = events.mapNotNull {
            when (it) {
                is CompositeEvent -> it.atomicEvents
                is AtomicEvent -> listOf(it)
                else -> null
            }
        }.flatten()

    val addEvents = atomicEvents.filterIsInstance<AddEvent<*, *>>()
    val removeEvents = atomicEvents.filterIsInstance<RemoveEvent>()
    val changeEvents = atomicEvents.filterIsInstance<ChangeEvent>()
    val reverseEdgeEvents = atomicEvents.filterIsInstance<ReverseEdgeEvent>()

}

private fun getElementForm(element: OverallElement) =
    when (element) {
        is ElementClass -> when (element) {
            is NodeClass -> ElementForm.NODE
            is EdgeClass -> ElementForm.EDGE
            is PropertyClass -> ElementForm.PROPERTY
            else -> throw IllegalArgumentException()
        }
        is Node<*, *> -> ElementForm.NODE
        is Edge<*, *> -> ElementForm.EDGE
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
) : ChangeModelElementEvent(elementKey, ElementForm.PROPERTY, customProperties) {
    override val elementForm: ElementForm = ElementForm.PROPERTY

    constructor(element: PropertyRelation, customProperties: Map<String, Any?> = mapOf()) : this(
        elementKey = element.key,
        holderKey = element.holderElementClass.key,
        holderForm = if (element.holderElementClass is NodeClass) ElementForm.NODE else ElementForm.EDGE,
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
    override val elementForm = ElementForm.EDGE
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

fun <T : AtomicEvent> List<T>.nodeEvents() = filter { it.elementForm == ElementForm.NODE }
fun <T : AtomicEvent> List<T>.edgeEvents() = filter { it.elementForm == ElementForm.EDGE }



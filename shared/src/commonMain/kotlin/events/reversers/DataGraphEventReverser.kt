package events.reversers

import dto.ConvertibleToDto
import dto.OverallElementDto
import dto.PropertyDto
import elements.DataGraphElement
import elements.OverallElement
import elements.data.EdgeInstance
import elements.data.NodeInstance
import events.*

class DataGraphEventReverser : EventReverser<NodeInstance, EdgeInstance>() {
    fun reverseAddEvent(event: AddEvent<*, *>) = RemoveDataElementEvent(event.elementKey, event.elementForm)

    fun <T, X> reverseRemoveEvent(element: T): AddEvent<T, X>
            where T : ConvertibleToDto<X>, T : OverallElement, X : OverallElementDto = AddEvent(element)

    fun reverseReverseEvent(event: ReverseEdgeInstanceEvent): ReverseEdgeInstanceEvent = event

    fun reverseChangeEvent(event: ChangeDataElementEvent, element: OverallElement): ChangeDataElementEvent {
        val propertyChanges = revertPropertyChanges(event.propertyChanges, element)
        return event.copy(propertyChanges = propertyChanges)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : OverallElement, X : PropertyDto> getPropertyDtoByKeyFromElement(element: T, key: String): X? =
        (element as DataGraphElement).getPropertyByKey(key)?.convert() as X?

    private fun DataGraphElement.getPropertyByKey(key: String) = properties[key]

}
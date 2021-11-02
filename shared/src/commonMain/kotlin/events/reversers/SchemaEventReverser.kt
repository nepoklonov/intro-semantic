package events.reversers

import dto.*
import elements.*
import elements.schema.model.EdgeRelation
import elements.schema.model.ModelNode
import events.*

class SchemaEventReverser : EventReverser<ModelNode, EdgeRelation>() {

    fun reverseAddEvent(event: AddEvent<*, *>): RemoveEvent {
        val key = event.elementKey
        val form = event.elementForm
        return when (event.dto) {
            is ElementClassDto -> RemoveElementClassEvent(key, form)
            is ModelNodeDto -> RemoveModelElementEvent(key, form)
            is EdgeRelationDto -> RemoveModelElementEvent(key, form)
            else -> throw IllegalArgumentException()
        }
    }

    fun <T, X> reverseRemoveElementClassEvent(element: T): AddEvent<T, X>
            where T : ElementClass, T : ConvertibleToDto<X>, X : ElementClassDto = AddEvent(element)

    fun <T, X> reverseRemoveModelElementEvent(element: T): AddEvent<T, X>
            where T : ModelElement, T : ConvertibleToDto<X>, X : ModelElementDto = AddEvent(element)

    fun reverseReverseEvent(event: ReverseEdgeRelationEvent): ReverseEdgeRelationEvent = event

    fun reverseChangeElementClassPropertyHolderEvent(
        event: ChangeElementClassPropertyHolderEvent, element: ElementClassPropertyHolder
    ): ChangeElementClassPropertyHolderEvent {
        val propertyInfo = revertPropertyChanges(event.propertyChanges, element as OverallElement)
        val customProperties = reverseCustomProperties(event.customProperties, element)
        return event.copy(
            propertyChanges = propertyInfo,
            customProperties = customProperties
        )

    }

    fun reverseChangeEvent(event: ChangeSchemaElementEvent, element: SchemaElement): ChangeSchemaElementEvent {
        val key = event.elementKey
        val form = event.elementForm
        val customProperties = reverseCustomProperties(event.customProperties, element)

        return if (form == ElementForm.PROPERTY) {
            ChangeElementClassEvent(key, form, customProperties)
        } else {
            ChangeModelElementEvent(key, form, customProperties)
        }
    }

    private fun reverseCustomProperties(customProperties: Map<String, Any?>, element: SchemaElement): Map<String, Any?> {
        return getPropertiesFromMap(customProperties, element) //TODO проверить корректность
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : OverallElement, X : PropertyDto> getPropertyDtoByKeyFromElement(element: T, key: String): X? =
        (element as ElementClassPropertyHolder).getPropertyById(key)?.convert() as X?

    private fun ElementClassPropertyHolder.getPropertyById(id: String) = properties[id]

}
package elements.data

import dto.ConvertibleToDto
import dto.PropertyInstanceDto
import dto.transform
import elements.DataElement
import elements.abstract.Property
import elements.schema.fundamental.PropertyClass
import elements.schema.model.PropertyRelation
import utils.exact

actual data class PropertyInstance actual constructor(
    actual override val modelElement: PropertyRelation,
    actual var value: Any?,
    override val id: String,
) : DataElement, Property<NodeInstance, EdgeInstance>,
    ConvertibleToDto<PropertyInstanceDto> {
    override val elementClass: PropertyClass get() = modelElement.elementClass

    override val label: String get() = elementClass.label

    override val key: String get() = elementClass.label

    override fun convert(): PropertyInstanceDto = transform {
        PropertyInstanceDto::value.exact from value.toString()
    }

    override fun toString(): String {
        return "$label: $value"
    }
}
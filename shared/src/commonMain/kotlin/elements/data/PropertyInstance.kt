package elements.data

import dto.ConvertibleToDto
import dto.PropertyInstanceDto
import elements.DataElement
import elements.IdGenerator
import elements.abstract.Property
import elements.schema.model.PropertyRelation

expect class PropertyInstance(
    modelElement: PropertyRelation,
    value: Any?,
    id: String = modelElement.elementClass.id, //TODO подумать над идентификаторами свойств; хранятся ли они в JG?
) : DataElement, Property<NodeInstance, EdgeInstance>, ConvertibleToDto<PropertyInstanceDto> {
    override val modelElement: PropertyRelation
    var value: Any?
}

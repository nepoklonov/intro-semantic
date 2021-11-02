package elements.schema.model

import dto.ConvertibleToDto
import dto.PropertyRelationDto
import elements.ModelElement
import elements.ElementClassPropertyHolder
import elements.JanusProperty
import elements.Level
import elements.abstract.Property
import elements.schema.fundamental.PropertyClass

expect class PropertyRelation(
    elementClass: PropertyClass,
    holderElementClass: ElementClassPropertyHolder,
    isPrimary: Boolean = false
) : ModelElement, Property<ModelNode, EdgeRelation>, ConvertibleToDto<PropertyRelationDto> {
    var isPrimary: Boolean
    val holderElementClass: ElementClassPropertyHolder
}
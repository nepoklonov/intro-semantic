package elements.schema.model

import dto.ConvertibleToDto
import dto.PropertyRelationDto
import elements.*
import elements.abstract.Property
import elements.schema.fundamental.PropertyClass
import dto.transform

actual data class PropertyRelation actual constructor(
    override val elementClass: PropertyClass,
    actual val holderElementClass: ElementClassPropertyHolder,

    @JanusProperty(Level.CUSTOM)
    actual var isPrimary: Boolean,
) : ModelElement, Property<ModelNode, EdgeRelation>, ConvertibleToDto<PropertyRelationDto> {
    override val id: String = "${elementClass.label}.${holderElementClass.label}"

    override val label: String get() = elementClass.label

    override val key: String get() = elementClass.label

    override fun convert(): PropertyRelationDto = transform()
}
package elements.schema.fundamental

import dto.ConvertibleToDto
import dto.PropertyClassDto
import elements.ElementClass
import elements.IdGenerator
import elements.schema.PropertyClassType
import specifications.Cardinality
import specifications.DataType

expect class PropertyClass(
    label: String,
    dataType: DataType,
    cardinality: Cardinality = Cardinality.SINGLE,
    id: String = IdGenerator.generateId(),
    type: PropertyClassType = PropertyClassType.SEMANTIC_ELEMENT
) : ElementClass, ConvertibleToDto<PropertyClassDto> {
    val dataType: DataType
    val cardinality: Cardinality
    override val type: PropertyClassType
}
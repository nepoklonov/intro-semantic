package elements.schema.fundamental

import dto.ConvertibleToDto
import dto.EdgeClassDto
import specifications.Multiplicity
import elements.ElementClassPropertyHolder
import elements.IdGenerator
import elements.schema.EdgeClassType
import elements.schema.model.EdgeRelation

expect class EdgeClass(
    label: String,
    id: String = IdGenerator.generateId(),
    type: EdgeClassType = EdgeClassType.SEMANTIC_ELEMENT,
    multiplicity: Multiplicity = Multiplicity.MULTI
) : ElementClassPropertyHolder, ConvertibleToDto<EdgeClassDto> {
    override val type: EdgeClassType
    val relations: Set<EdgeRelation>
    val multiplicity: Multiplicity
}
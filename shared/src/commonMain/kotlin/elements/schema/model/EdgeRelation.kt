package elements.schema.model

import dto.ConvertibleToDto
import dto.EdgeRelationDto
import elements.ModelGraphElement
import elements.Tangibility
import elements.abstract.Edge
import elements.schema.fundamental.EdgeClass
import utils.collections.MutableKeySet

expect class EdgeRelation(
    source: ModelNode,
    target: ModelNode,
    elementClass: EdgeClass? = null,
    label: String = elementClass?.label ?: "",
    tangibility: Tangibility = Tangibility.Real,
) : ModelGraphElement, Edge<ModelNode, EdgeRelation>, ConvertibleToDto<EdgeRelationDto> {
    override val properties: MutableKeySet<String, PropertyRelation>
}
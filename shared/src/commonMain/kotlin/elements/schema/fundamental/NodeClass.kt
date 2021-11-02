package elements.schema.fundamental

import dto.ConvertibleToDto
import dto.NodeClassDto
import elements.ElementClassPropertyHolder
import elements.IdGenerator
import elements.schema.ElementClassType
import elements.schema.NodeClassType
import elements.schema.model.ModelNode

expect class NodeClass(
    label: String,
    id: String = IdGenerator.generateId(),
    type: NodeClassType = NodeClassType.SEMANTIC_ELEMENT,
) : ElementClassPropertyHolder, ConvertibleToDto<NodeClassDto> {
    val modelNode: ModelNode?
    val hasModelNode: Boolean?
    override val type: NodeClassType
}
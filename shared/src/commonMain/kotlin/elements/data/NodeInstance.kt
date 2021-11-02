package elements.data

import dto.ConvertibleToDto
import dto.NodeInstanceDto
import elements.DataGraphElement
import elements.IdGenerator
import elements.Tangibility
import elements.abstract.Node
import elements.schema.fundamental.NodeClass
import elements.schema.model.ModelNode
import utils.collections.MutableKeySet
import utils.collections.mutableKeySetOf

expect class NodeInstance (
    elementClass: NodeClass? = null,
    id: String = IdGenerator.generateId(),
    properties: MutableKeySet<String, PropertyInstance> = mutableKeySetOf(),
    tangibility: Tangibility = Tangibility.Real,
): DataGraphElement, Node<NodeInstance, EdgeInstance>, ConvertibleToDto<NodeInstanceDto> {
    override val modelElement: ModelNode?
    override val properties: MutableKeySet<String, PropertyInstance>
}

fun NodeInstance.copy(
    elementClass: NodeClass? = this.elementClass,
    id: String = this.id,
    properties: MutableKeySet<String, PropertyInstance> = this.properties
) = NodeInstance(elementClass, id, properties)
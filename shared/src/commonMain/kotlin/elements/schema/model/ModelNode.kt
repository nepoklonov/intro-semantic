package elements.schema.model

import dto.ConvertibleToDto
import dto.ModelNodeDto
import elements.ModelGraphElement
import elements.Tangibility
import elements.abstract.Node
import elements.schema.fundamental.NodeClass
import utils.collections.*

expect class ModelNode(
    elementClass: NodeClass? = null,
    tangibility: Tangibility = Tangibility.Real,
) : ModelGraphElement, Node<ModelNode, EdgeRelation>, ConvertibleToDto<ModelNodeDto> {
    override val properties: MutableKeySet<String, PropertyRelation>
}

val ModelNode.inheritedProperties: KeySet<String, PropertyRelation>
    get() = parent?.run { inheritedProperties + properties } ?: keySetOf()

val ModelNode.allProperties: KeySet<String, PropertyRelation> get() = properties + inheritedProperties

val ModelNode.ancestors get() = generateSequence(this) { it.parent }
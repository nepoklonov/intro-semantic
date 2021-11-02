package elements.schema.model

import dto.ConvertibleToDto
import dto.ModelNodeDto
import dto.transform
import elements.IdGenerator
import elements.ModelGraphElement
import elements.Tangibility
import elements.abstract.Node
import elements.schema.fundamental.NodeClass
import structure.Graph
import utils.collections.MutableKeySet
import utils.collections.mutableKeySetOf

//TODO fix strange key generation
actual data class ModelNode actual constructor(
    override var elementClass: NodeClass?,
    override val tangibility: Tangibility,
) : ModelGraphElement, Node<ModelNode, EdgeRelation>, ConvertibleToDto<ModelNodeDto> {

    override val id: String = elementClass?.id ?: IdGenerator.generateId()

    override var graph: Graph<ModelNode, EdgeRelation>? = null

    private var _label: String = ""

    override var label: String
        get() = elementClass?.label ?: _label
        set(value) {
            _label = value
//            elementClass = graph?.host?.nodeClasses?.get(value)
        }

    override val key: String get() = elementClass?.label ?: id

    actual override val properties: MutableKeySet<String, PropertyRelation>
        get() = elementClass?.properties ?: mutableKeySetOf()

    override fun convert(): ModelNodeDto = transform()
}
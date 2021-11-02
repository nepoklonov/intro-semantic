package elements.data

import dto.ConvertibleToDto
import dto.NodeInstanceDto
import dto.transform
import elements.DataGraphElement
import elements.IrregularDataProperty
import elements.Tangibility
import elements.abstract.Node
import elements.schema.fundamental.NodeClass
import elements.schema.model.ModelNode
import org.apache.tinkerpop.gremlin.structure.Vertex
import structure.Graph
import utils.collections.MutableKeySet
import utils.exact

actual data class NodeInstance actual constructor(
    override var elementClass: NodeClass?,
    override val id: String,
    actual override val properties: MutableKeySet<String, PropertyInstance>,
    override val tangibility: Tangibility,
) : DataGraphElement, Node<NodeInstance, EdgeInstance>, ConvertibleToDto<NodeInstanceDto> {
    override var graph: Graph<NodeInstance, EdgeInstance>? = null

    private var _label: String = ""

    override var label: String
        get() = elementClass?.label ?: _label
        set(value) {
            _label = value
            //TODO: опасная хрень ниже:
//            elementClass = graph?.schema?.fundamental?.nodeClasses?.get(value)
        }

    actual override val modelElement: ModelNode?
        get() = elementClass?.modelNode

    override fun convert(): NodeInstanceDto = transform {
        NodeInstanceDto::properties.exact from properties.map { it.convert() }.toSet()
    }

    override fun toString(): String {
        return listOf("id: $id", "label: $label", "parent: $parent", "graph: ${graph != null}", "ecId: ${elementClass?.id}", "properties: $properties").joinToString("\n", prefix = "{", postfix = "}\n")
    }
}
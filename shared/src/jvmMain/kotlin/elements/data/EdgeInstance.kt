package elements.data

import dto.ConvertibleToDto
import dto.EdgeInstanceDto
import dto.transform
import elements.DataGraphElement
import elements.Tangibility
import elements.abstract.Edge
import elements.schema.fundamental.EdgeClass
import elements.schema.model.EdgeRelation
import structure.Graph
import utils.collections.MutableKeySet
import utils.exact

actual data class EdgeInstance actual constructor(
    override val source: NodeInstance,
    override val target: NodeInstance,
    override var elementClass: EdgeClass?,
    override val id: String,
    actual override val properties: MutableKeySet<String, PropertyInstance>,
    override val tangibility: Tangibility,
) : DataGraphElement, Edge<NodeInstance, EdgeInstance>,
    ConvertibleToDto<EdgeInstanceDto> {

    override var graph: Graph<NodeInstance, EdgeInstance>? = null

    //TODO check:
    private var _label: String = ""

    override var label: String
        get() = elementClass?.label ?: _label
        set(value) {
            _label = value
            //TODO опасная хрень ниже:
//            elementClass = graph?.schema?.fundamental?.edgeClasses?.get(value)
        }

    actual override val modelElement: EdgeRelation? get() = modelElementExpect

    override fun createReversed(): EdgeInstance = copy(target = source, source = target)

    override fun convert(): EdgeInstanceDto = transform {
        EdgeInstanceDto::properties.exact from properties.map { it.convert() }.toSet()
        EdgeInstanceDto::source.exact from source.id
        EdgeInstanceDto::target.exact from target.id
    }


    override fun toString(): String {
        return listOf(
            "id: $id", "source: ${source.id}", "target: ${target.id}", "label: $label", "ecId: ${elementClass?.id}",
            "properties: ${properties.toList()}"
        ).joinToString("\n", prefix = "{", postfix = "}")
    }

}
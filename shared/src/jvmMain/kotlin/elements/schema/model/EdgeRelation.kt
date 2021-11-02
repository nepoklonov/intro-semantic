package elements.schema.model

import dto.ComplexId
import dto.ConvertibleToDto
import dto.EdgeRelationDto
import elements.abstract.Edge
import elements.schema.fundamental.EdgeClass
import dto.transform
import elements.*
import structure.Graph
import utils.collections.MutableKeySet
import utils.collections.mutableKeySetOf
import utils.exact

actual data class EdgeRelation actual constructor(
    override val source: ModelNode,
    override val target: ModelNode,
    override val elementClass: EdgeClass?,
    override val label: String,
    override val tangibility: Tangibility,
) : ModelGraphElement, Edge<ModelNode, EdgeRelation>,
    ConvertibleToDto<EdgeRelationDto> {

    override val id: String = elementClass?.let {
        generateEdgeRelationKey(it, source, target)
    } ?: IdGenerator.generateId()

    init {
        check(elementClass?.label?.equals(label) ?: true) //TODO мб сделать два конструктора
    }

    override val key: String get() = /* elementClass?.let { generateEdgeRelationKey(it, source, target) } ?: */ id

    override var graph: Graph<ModelNode, EdgeRelation>? = null

    actual override val properties: MutableKeySet<String, PropertyRelation>
        get() = elementClass?.properties ?: mutableKeySetOf()

    fun isInheritanceEdgeRelation() = elementClass == inheritanceEdgeClass

    override fun convert(): EdgeRelationDto = transform {
        EdgeRelationDto::id.exact from ComplexId(
            label = label,
            sourceKey = source.key,
            targetKey = target.key
        )
    }

    override fun createReversed(): EdgeRelation = copy(target = source, source = target)
}
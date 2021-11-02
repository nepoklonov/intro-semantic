package elements.data

import dto.ConvertibleToDto
import dto.EdgeInstanceDto
import dto.NodeInstanceDto
import elements.*
import elements.abstract.Edge
import elements.abstract.parentEdge
import elements.schema.fundamental.EdgeClass
import elements.schema.fundamental.NodeClass
import elements.schema.model.EdgeRelation
import elements.schema.model.ModelNode
import elements.schema.model.ancestors
import utils.collections.MutableKeySet
import utils.collections.mutableKeySetOf

expect class EdgeInstance(
    source: NodeInstance,
    target: NodeInstance,
    elementClass: EdgeClass? = null,
    id: String = IdGenerator.generateId(),
    properties: MutableKeySet<String, PropertyInstance> = mutableKeySetOf(),
    tangibility: Tangibility = Tangibility.Real,
) : DataGraphElement, Edge<NodeInstance, EdgeInstance>, ConvertibleToDto<EdgeInstanceDto> {
    override val modelElement: EdgeRelation?
    override val properties: MutableKeySet<String, PropertyInstance>
}

//TODO убрать подальше
internal val EdgeInstance.modelElementExpect: EdgeRelation?
    get() {
        return findEdgeRelation(
            source.modelElement ?: return null,
            target.modelElement ?: return null,
            elementClass ?: return null
        )
    }

fun findEdgeRelation(sourceModelNode: ModelNode, targetModelNode: ModelNode, edgeClass: EdgeClass): EdgeRelation? {
    return if (edgeClass == inheritanceEdgeClass) {
        sourceModelNode.parentEdge?.also { check(it.target == targetModelNode) }
    } else edgeClass.relations.firstOrNull { relation ->
        (relation.source in sourceModelNode.ancestors) and (relation.target in targetModelNode.ancestors)
    }
}

fun EdgeInstance.copy(
    source: NodeInstance = this.source,
    target: NodeInstance = this.target,
    elementClass: EdgeClass? = this.elementClass,
    id: String = this.id,
    properties: MutableKeySet<String, PropertyInstance> = this.properties
) = EdgeInstance(source, target, elementClass, id, properties)
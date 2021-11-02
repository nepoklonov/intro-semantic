package structure

import dto.*
import elements.ElementClass
import elements.ModelElement
import elements.schema.fundamental.EdgeClass
import elements.schema.fundamental.NodeClass
import elements.schema.fundamental.PropertyClass
import elements.schema.model.EdgeRelation
import elements.schema.model.ModelNode
import elements.schema.model.PropertyRelation
import kotlinx.serialization.Serializable

//TODO add rollback
data class Schema(
    var fundamental: Fundamental = Fundamental(),
    var modelGraph: ModelGraph = ModelGraph()
) : Structure, ConvertibleToDto<SchemaDto> {

    val matrix: SchemaMatrix = SchemaMatrix()

    val size get() = fundamental.size + modelGraph.size

    init {
        fundamental.schema = this
        modelGraph.schema = this
        fundamental.nodeClasses.forEach { nodeClass ->
            matrix.onAddNodeClass(nodeClass)
        }
        fundamental.edgeClasses.forEach { edgeClass ->
            matrix.onAddEdgeClass(edgeClass)
        }
        modelGraph.nodes.forEach { modelNode ->
            matrix.onAddModelNode(modelNode)
        }
        modelGraph.edges.forEach { edgeRelation ->
            matrix.onAddEdgeRelation(edgeRelation)
        }
    }


    fun addNodeClass(nodeClass: NodeClass): NodeClass? {
        return fundamental.addNodeClass(nodeClass)?.also {
            matrix.onAddNodeClass(it)
        }
    }

    fun addEdgeClass(edgeClass: EdgeClass): EdgeClass? {
        return fundamental.addEdgeClass(edgeClass)?.also {
            matrix.onAddEdgeClass(it)
        }
    }

    fun addPropertyClass(propertyClass: PropertyClass): PropertyClass? {
        return fundamental.addPropertyClass(propertyClass)
    }

    fun addElementClass(elementClass: ElementClass) = fundamental.add(elementClass)?.also {
        when (it) {
            is NodeClass -> matrix.onAddNodeClass(it)
            is EdgeClass -> matrix.onAddEdgeClass(it)
        }
    }

    fun addModelNode(modelNode: ModelNode): AddModelNodeResult {
        val addModelNodeResult = AddModelNodeResult()
        modelGraph.addNode(modelNode)?.let { addedNode ->
            addModelNodeResult.modelElement = addedNode
            modelNode.elementClass?.let { nodeClass ->
                addModelNodeResult.elementClass = addNodeClass(nodeClass)
                matrix.onAddModelNode(modelNode)
            }
        }
        return addModelNodeResult
    }

    fun addEdgeRelation(edgeRelation: EdgeRelation): AddEdgeRelationResult {
        val addEdgeRelationResult = AddEdgeRelationResult()
        modelGraph.addEdge(edgeRelation)?.let { addedEdge ->
            addEdgeRelationResult.modelElement = addedEdge
            edgeRelation.elementClass?.let { edgeClass ->
                addEdgeRelationResult.elementClass = addEdgeClass(edgeClass)
                matrix.onAddEdgeRelation(edgeRelation)
            }
        }
        return addEdgeRelationResult
    }

    fun removeNodeClass(nodeClass: NodeClass): RemoveNodeClassResult {
        //TODO check nodeClass.schema == this
        val removeNodeClassResult = RemoveNodeClassResult()
        val removedModelElements = nodeClass.modelNode?.let { modelGraph.removeNode(it) }

        fundamental.removeNodeClass(nodeClass)?.let { removedClass ->
            removeNodeClassResult.nodeClass = removedClass
            removeNodeClassResult.modelElements = removedModelElements
            matrix.onRemoveNodeClass(removedClass)
        } ?: rollbackNodeClassRemoval(removedModelElements)

        return removeNodeClassResult
    }

    fun removeEdgeClass(edgeClass: EdgeClass): RemoveEdgeClassResult {
        val removeEdgeClassResult = RemoveEdgeClassResult()
        val removedRelations = edgeClass.relations.mapNotNull { modelGraph.removeEdge(it) }

        fundamental.removeEdgeClass(edgeClass)?.let { removedClass ->
            removeEdgeClassResult.edgeClass = removedClass
            removeEdgeClassResult.edgeRelations = removedRelations
            matrix.onRemoveEdgeClass(removedClass)
        } ?: rollbackEdgeClassRemoval(removedRelations)

        return removeEdgeClassResult
    }

    fun removePropertyClass(propertyClass: PropertyClass): RemovePropertyClassResult {
        val removedPropertyClass = fundamental.removePropertyClass(propertyClass)
        val removedRelations = (modelGraph.nodes + modelGraph.edges).mapNotNull { element ->
            element.properties.removeByKey(propertyClass.label)
        }
        return RemovePropertyClassResult(removedPropertyClass, removedRelations)
    }
    //TODO will be correctly implemented after property matrix

    fun removeModelNode(modelNode: ModelNode): GraphElementSet<ModelNode, EdgeRelation>? =
        modelGraph.removeNode(modelNode)?.also {
            matrix.onRemoveModelNode(modelNode)
        }

    fun removeEdgeRelation(edgeRelation: EdgeRelation): EdgeRelation? =
        modelGraph.removeEdge(edgeRelation)?.also {
            matrix.onRemoveEdgeRelation(edgeRelation)
        }

    fun reverseEdgeRelation(oldEdge: EdgeRelation): EdgeRelation? = modelGraph.reverseEdge(oldEdge)?.also { newEdge ->
        val edgeClassKey = oldEdge.elementClass?.key
        if (edgeClassKey != null) {
            matrix.edgeClassInfo[edgeClassKey]?.relations?.remove(oldEdge)
            matrix.edgeClassInfo[edgeClassKey]?.relations?.add(newEdge)
        }
    }

    private fun rollbackNodeClassRemoval(removedModelElements: GraphElementSet<ModelNode, EdgeRelation>?) {
        if (removedModelElements != null) modelGraph.rollbackNodeRemoval(removedModelElements)
    }

    private fun rollbackEdgeClassRemoval(removedEdgeRelations: List<EdgeRelation>) {
        removedEdgeRelations.forEach { modelGraph.addEdge(it) }
    }

    fun getNodeClass(label: String) = fundamental.nodeClasses[label]
    fun getEdgeClass(label: String) = fundamental.edgeClasses[label]
    fun getPropertyClass(label: String) = fundamental.propertyClasses[label]

    override fun convert(): SchemaDto = SchemaDto(fundamental.convert(), modelGraph.convert())

}

@Serializable
class SchemaDto(
    val fundamentalDto: FundamentalDto,
    val modelGraphDto: ModelGraphDto
) : Dto

val NodeClass.schemaInfo get() = fundamental?.schema?.matrix?.nodeClassInfo?.get(key)
val EdgeClass.schemaInfo get() = fundamental?.schema?.matrix?.edgeClassInfo?.get(key)

interface AddModelElementResult<F, FD : ElementClassDto, M, MD : ModelElementDto>
        where F : ConvertibleToDto<FD>, F : ElementClass, M : ConvertibleToDto<MD>, M : ModelElement {
    val elementClass: F?
    val modelElement: M?
}

class AddModelNodeResult(
    override var elementClass: NodeClass? = null,
    override var modelElement: ModelNode? = null
) : AddModelElementResult<NodeClass, NodeClassDto, ModelNode, ModelNodeDto>

class AddEdgeRelationResult(
    override var elementClass: EdgeClass? = null,
    override var modelElement: EdgeRelation? = null
) : AddModelElementResult<EdgeClass, EdgeClassDto, EdgeRelation, EdgeRelationDto>

class RemoveNodeClassResult(
    var nodeClass: NodeClass? = null,
    var modelElements: GraphElementSet<ModelNode, EdgeRelation>? = null
)

class RemoveEdgeClassResult(
    var edgeClass: EdgeClass? = null,
    var edgeRelations: List<EdgeRelation>? = null
)

class RemovePropertyClassResult(
    var propertyClass: PropertyClass? = null,
    var propertyRelations: List<PropertyRelation>? = null
)


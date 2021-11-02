package events.appliers

import dto.*
import elements.ElementClassPropertyHolder
import elements.SchemaElement
import elements.schema.model.EdgeRelation
import elements.schema.model.ModelNode
import elements.setPropertiesFromMap
import events.*
import structure.Fundamental
import structure.ModelGraph
import structure.Schema

class SchemaEventApplier(
    val schema: Schema,
) : EventApplier<ModelNode, EdgeRelation>() {
    override val graph: ModelGraph = schema.modelGraph
    private val fundamental: Fundamental = schema.fundamental
    private val fundamentalConverter = FundamentalDtoConverter()
    private val modelConverter = ModelGraphDtoConverter(fundamental)

    override fun applyAtomicEvent(event: AtomicEvent): Boolean =
        when (event) {
            is AddEvent<*, *> -> applyAddEvent(event)
            is RemoveElementClassEvent -> applyRemoveElementClassEvent(event)
            is RemoveModelElementEvent -> applyRemoveModelElementEvent(event)
            is ChangeElementClassPropertyHolderEvent -> applyChangeElementClassPropertyHolderEvent(event)
            is ChangeSchemaElementEvent -> applyChangeSchemaElementEvent(event)
            is ReverseEdgeRelationEvent -> applyReverseEdgeEvent(event)
            else -> throw IllegalArgumentException()
        }

    private fun applyAddEvent(event: AddEvent<*, *>) = when (event.dto) {
        is ElementClassDto -> applyAddElementClassEvent(event)
        is ModelElementDto -> applyAddModelElementEvent(event)
        else -> false
    }

    private fun applyAddElementClassEvent(event: AddEvent<*, *>): Boolean {
        return when (val dto = event.dto) {
            is NodeClassDto -> addNodeClassDto(dto)
            is EdgeClassDto -> addEdgeClassDto(dto)
            is PropertyClassDto -> addPropertyClassDto(dto)
            else -> throw IllegalArgumentException()
        }
    }

    private fun addNodeClassDto(dto: NodeClassDto): Boolean {
        val nodeClass = fundamentalConverter.run { dto.convert(fundamental) }
        schema.addElementClass(nodeClass)
        return true
    }

    private fun addEdgeClassDto(dto: EdgeClassDto): Boolean {
        val edgeClass = fundamentalConverter.run { dto.convert(fundamental) }
        schema.addElementClass(edgeClass)
        return true
    }

    private fun addPropertyClassDto(dto: PropertyClassDto): Boolean {
        val propertyClass = fundamentalConverter.run { dto.convert() }
        schema.addElementClass(propertyClass)
        return true
    }

    private fun applyAddModelElementEvent(event: AddEvent<*, *>): Boolean {
        return when (val dto = event.dto) {
            is ModelNodeDto -> addModelNodeDto(dto)
            is EdgeRelationDto -> addEdgeRelation(dto)
            else -> throw IllegalArgumentException()
        }
    }

    private fun addModelNodeDto(dto: ModelNodeDto): Boolean {
        val modelNode = modelConverter.run { dto.convert() }
        schema.addModelNode(modelNode)
        return true
    }

    private fun addEdgeRelation(dto: EdgeRelationDto): Boolean =
        modelConverter.run { dto.convert(graph) }?.let {
            schema.addEdgeRelation(it)
            true
        } ?: false

    private fun applyRemoveElementClassEvent(event: RemoveElementClassEvent): Boolean {
        val key = event.elementKey
        return when (event.elementForm) {
            ElementForm.NODE -> fundamental.nodeClasses[key]
            ElementForm.EDGE -> fundamental.edgeClasses[key]
            ElementForm.PROPERTY -> fundamental.propertyClasses[key]
        }?.let {
            fundamental.remove(it)
            true
        } ?: false
    }

    private fun applyRemoveModelElementEvent(event: RemoveModelElementEvent) = removeFromGraph(event)

    private fun applyChangeElementClassPropertyHolderEvent(event: ChangeElementClassPropertyHolderEvent): Boolean {
        val key = event.elementKey
        return when (event.elementForm) {
            ElementForm.NODE -> fundamental.nodeClasses[key]
            ElementForm.EDGE -> fundamental.edgeClasses[key]
            else -> throw IllegalArgumentException()
        }?.applyChangeEvent(event) ?: false
    }

    private fun applyChangeSchemaElementEvent(event: ChangeSchemaElementEvent): Boolean {
        val key = event.elementKey
        return when (event.elementForm) {
            ElementForm.NODE -> graph.nodes[key]
            ElementForm.EDGE -> graph.edges[key]
            ElementForm.PROPERTY -> getProperty(event)
        }?.setCustomProps(event.customProperties) ?: false
    }

    private fun ElementClassPropertyHolder.applyChangeEvent(event: ChangeElementClassPropertyHolderEvent): Boolean {
        val propertyInfo = event.propertyChanges
        propertyInfo.propertyKeysToRemove.forEach { properties.removeByKey(it) }
        val convertedProperties = propertyInfo.propertiesToAdd.mapNotNull {
            fundamentalConverter.run { it.convert(this@applyChangeEvent, fundamental!!) }
        }
        properties.addAll(convertedProperties)
        setCustomProps(event.customProperties)
        return true
    }

    private fun SchemaElement.setCustomProps(properties: Map<String, Any?>): Boolean {
        setPropertiesFromMap(properties, this)
        return true
    }

    private fun getProperty(event: ChangeSchemaElementEvent): SchemaElement? =
        if (event is ChangePropertyRelationEvent) {
            getPropertyRelation(event)
        } else {
            fundamental.propertyClasses[event.elementKey]
        }

    private fun getPropertyRelation(event: ChangePropertyRelationEvent) =
        when (event.holderForm) {
            ElementForm.NODE -> fundamental.nodeClasses[event.holderKey]
            ElementForm.EDGE -> fundamental.edgeClasses[event.holderKey]
            else -> throw IllegalArgumentException()
        }?.let { element -> element.properties[event.elementKey] }

}
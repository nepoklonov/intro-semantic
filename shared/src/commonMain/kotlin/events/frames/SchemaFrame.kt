package events.frames

import dto.ConvertibleToDto
import dto.ElementClassDto
import elements.ElementClass
import elements.ElementClassPropertyHolder
import elements.SchemaElement
import elements.schema.fundamental.EdgeClass
import elements.schema.fundamental.NodeClass
import elements.schema.fundamental.PropertyClass
import elements.schema.model.EdgeRelation
import elements.schema.model.ModelNode
import elements.schema.model.PropertyRelation
import events.*
import events.appliers.SchemaEventApplier
import events.eventlines.SchemaEventLine
import events.reversers.SchemaEventReverser
import events.eventmakers.SchemaEventMaker
import structure.*

class SchemaFrame(override val structure: Schema) : Frame<ModelNode, EdgeRelation> {

    override val eventLine: SchemaEventLine = SchemaEventLine(SchemaEventApplier(structure))
    private val eventMaker: SchemaEventMaker = SchemaEventMaker()

    override fun addNode(node: ModelNode): ModelNode? {
        val result = structure.addModelNode(node)
        eventMaker.makeAddEvent(result)?.let { putEvent(it) }
        return result.modelElement
    }

    override fun addEdge(edge: EdgeRelation): EdgeRelation? {
        val result = structure.addEdgeRelation(edge)
        eventMaker.makeAddEvent(result)?.let { putEvent(it) }
        return result.modelElement
    }

    override fun removeNode(node: ModelNode) = structure.removeModelNode(node)?.also { removedEles ->
        val event = eventMaker.makeRemoveEvent(removedEles)
        if (event != null) putEvent(event)
    }

    override fun removeEdge(edge: EdgeRelation) = structure.removeEdgeRelation(edge)?.also {
        val event = eventMaker.makeRemoveEvent(it)
        putEvent(event)
    }

    override fun reverseEdge(edge: EdgeRelation) = structure.reverseEdgeRelation(edge)?.also {
        val event = eventMaker.makeReverseEvent(it)
        putEvent(event)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T, X> addElementClass(elementClass: T): T? where T : ElementClass, T : ConvertibleToDto<X>, X : ElementClassDto =
        (structure.addElementClass(elementClass) as T?)?.also {
            val event = eventMaker.makeAddEvent(it)
            putEvent(event)
        }

    fun removeNodeClass(nodeClass: NodeClass): NodeClass? {
        val result = structure.removeNodeClass(nodeClass)
        eventMaker.makeRemoveEvent(result)?.let { putEvent(it) }
        return result.nodeClass
    }

    fun removeEdgeClass(edgeClass: EdgeClass): EdgeClass? {
        val result = structure.removeEdgeClass(edgeClass)
        eventMaker.makeRemoveEvent(result)?.let { putEvent(it) }
        return result.edgeClass
    }

    fun removePropertyClass(propertyClass: PropertyClass): PropertyClass? {
        val result = structure.removePropertyClass(propertyClass)
        eventMaker.makeRemoveEvent(result)?.let { putEvent(it) }
        return result.propertyClass
    }

    fun changeSchemaElementWithoutProps(element: SchemaElement, customProperties: Map<String, Any?>) {
        val event = eventMaker.makeChangeEvent(element, customProperties)
        eventLine.addAndApply(event)
    }

    fun changeElementClassPropertyHolder(
        element: ElementClassPropertyHolder,
        propertiesToAdd: List<PropertyRelation>,
        propertiesToRemove: List<PropertyRelation>,
        customProperties: Map<String, Any?>
    ) {
        val event = eventMaker.makeChangeElementClassPropertyHolderEvent(
            element,
            propertiesToAdd,
            propertiesToRemove,
            customProperties
        )
        eventLine.addAndApply(event)
    }

    private fun putEvent(eventPair: EventPair<*, *>) = eventLine.addEvent(eventPair)
}
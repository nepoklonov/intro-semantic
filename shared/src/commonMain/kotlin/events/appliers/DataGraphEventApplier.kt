package events.appliers

import dto.DataGraphDtoConverter
import dto.EdgeInstanceDto
import dto.NodeInstanceDto
import elements.DataGraphElement
import elements.ElementClassPropertyHolder
import elements.data.EdgeInstance
import elements.data.NodeInstance
import events.*
import structure.DataGraph

class DataGraphEventApplier(
    override val graph: DataGraph,
) : EventApplier<NodeInstance, EdgeInstance>() {
    val dtoConverter = DataGraphDtoConverter(graph.schema!!)

    override fun applyAtomicEvent(event: AtomicEvent): Boolean =
        when (event) {
            is AddEvent<*, *> -> applyAddEvent(event)
            is RemoveDataElementEvent -> applyRemoveEvent(event)
            is ChangeDataElementEvent -> applyChangeEvent(event)
            is ReverseEdgeInstanceEvent -> applyReverseEdgeEvent(event)
            else -> throw IllegalArgumentException()
        }

    private fun applyAddEvent(event: AddEvent<*, *>): Boolean =
        dtoConverter.run {
            when (val dto = event.dto) {
                is NodeInstanceDto -> addNodeDto(dto)
                is EdgeInstanceDto -> addEdgeDto(dto)
                else -> throw IllegalArgumentException()
            }
        }

    private fun addNodeDto(dto: NodeInstanceDto): Boolean {
        dtoConverter.run {
            val node = dto.convert()
            graph.addNode(node)
            return true
        }
    }

    private fun addEdgeDto(dto: EdgeInstanceDto): Boolean {
        dtoConverter.run {
            return dto.convert(graph)?.let {
                graph.addEdge(it)
                true
            } ?: false
        }
    }

    private fun applyRemoveEvent(event: RemoveDataElementEvent) = removeFromGraph(event)

    private fun applyChangeEvent(event: ChangeDataElementEvent): Boolean {
        val id = event.elementKey
        return when (event.elementForm) {
            ElementForm.NODE -> graph.nodes[id]
            ElementForm.EDGE -> graph.edges[id]
            else -> throw IllegalArgumentException()
        }?.applyChangeEvent(event) ?: false
    }

    private fun DataGraphElement.applyChangeEvent(event: ChangeDataElementEvent): Boolean {
        val elementClass = elementClass as ElementClassPropertyHolder?

        return if (elementClass != null) {
            val propertyInfo = event.propertyChanges
            propertyInfo.propertyKeysToRemove.forEach { properties.removeByKey(it) }
            val convertedProperties = propertyInfo.propertiesToAdd.mapNotNull {
                dtoConverter.run { it.convert(elementClass) }
            }
            properties.addAll(convertedProperties)
            true
        } else false
    }

}
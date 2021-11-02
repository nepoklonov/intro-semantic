package structure.utils

import elements.ElementClassPropertyHolder
import elements.data.EdgeInstance
import elements.data.NodeInstance
import elements.data.PropertyInstance
import elements.schema.fundamental.NodeClass
import elements.schema.model.ancestors
import specifications.DataType
import structure.DataGraph
import structure.Schema
import utils.collections.toMutableKeySet

fun DataGraph.superimpose(schema: Schema): DataGraph {
    return DataGraph(schema, nodes, edges).transform(
        transformNodes = { it.superimposeBy(schema) },
        transformEdges = { it.superimposeBy(schema) }
    ).also { dataGraph ->
        //TODO написать парсер нормально, чтобы не было таких проблем:
        dataGraph.edges.toList().asSequence().filter { it.modelElement == null }.forEach { dataGraph.removeEdge(it) }
    }
}

fun NodeInstance.superimposeBy(schema: Schema): NodeInstance? {
    val nodeClass = schema.getNodeClass(label) ?: return null
    return NodeInstance(
        elementClass = nodeClass,
        properties = properties.mapNotNull { property ->
            property.superimposeBy(nodeClass)
        }.toMutableKeySet()
    )
}


fun EdgeInstance.superimposeBy(schema: Schema): EdgeInstance? {
    val edgeClass = schema.getEdgeClass(label) ?: return null
    return EdgeInstance(
        source, target,
        elementClass = edgeClass,
        properties = properties.mapNotNull { property ->
            property.superimposeBy(edgeClass)
        }.toMutableKeySet()
    )
}

fun PropertyInstance.superimposeBy(
    newHolderElementClass: ElementClassPropertyHolder
): PropertyInstance? {
    val exactHolderElementClass = newHolderElementClass.let {
        if (it !is NodeClass) it else it.modelNode?.ancestors?.firstOrNull { modelNode ->
            modelNode.properties.containsKey(elementClass.label)
        }?.elementClass ?: return null
    }
    val propertyRelation = exactHolderElementClass.properties[elementClass.key] ?: return null
    val value = value as? String
    //TODO убрать дублирование кода
    val propertyClass = propertyRelation.elementClass
    val convertedValue = when(propertyClass.dataType) {
        DataType.STRING -> value
        DataType.CHARACTER -> value?.singleOrNull()
        DataType.BOOLEAN -> value?.toBoolean()
        DataType.INTEGER -> value?.toIntOrNull()
        DataType.LONG -> value?.toLongOrNull()
        DataType.SHORT -> value?.toShortOrNull()
        DataType.BYTE -> value?.toByteOrNull()
        DataType.DOUBLE -> value?.toDoubleOrNull()
        DataType.DATE -> TODO()
        DataType.GEOSHAPE -> TODO()
        DataType.UUID -> TODO()
    } ?: return null
    return PropertyInstance(propertyRelation, convertedValue)
}

//fun PropertyRelation.findNearest